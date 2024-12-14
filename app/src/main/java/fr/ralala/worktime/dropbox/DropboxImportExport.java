package fr.ralala.worktime.dropbox;


import android.app.Service;
import android.content.Context;
import android.net.Uri;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;
import fr.ralala.worktime.dropbox.tasks.DownloadFileTask;
import fr.ralala.worktime.dropbox.tasks.ListFolderTask;
import fr.ralala.worktime.dropbox.tasks.UploadFileTask;
import fr.ralala.worktime.sql.SqlConstants;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.utils.Log;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage exportation and importation using dropbox
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DropboxImportExport implements DropboxListener {
  private static final String PATH = "";
  private AlertDialog mDialog = null;
  private File mFile = null;
  private Context mContext = null;
  private DropboxUploaded mDropboxUploaded = null;
  private DropboxDownloaded mDropboxDownloaded = null;


  public interface DropboxUploaded {
    /**
     * File uploaded to dropbox.
     *
     * @param error True on error.
     */
    void dropboxUploaded(final boolean error);
  }

  public interface DropboxDownloaded {
    /**
     * File downloaded from dropbox.
     *
     * @param error True on error.
     */
    void dropboxDownloaded(final boolean error);
  }

  /**
   * Imports the data base.
   *
   * @param c The Android context.
   */
  public void importDatabase(final AppCompatActivity c) {
    mContext = c;
    mDropboxDownloaded = null;
    ApplicationCtx app = ((ApplicationCtx) mContext.getApplicationContext());
    showDialog(c);
    new ListFolderTask(app.getDropboxHelper(), this).execute(PATH);
  }

  /**
   * Exports the database.
   *
   * @param c               The Android context.
   * @param dropboxUploaded The export listener.
   * @return boolean
   */
  public boolean exportDatabase(final Context c, final DropboxUploaded dropboxUploaded) {
    mContext = c;
    mDropboxUploaded = dropboxUploaded;
    showDialog(c);
    ApplicationCtx app = ((ApplicationCtx) mContext.getApplicationContext());
    try {
      File path = AndroidHelper.getAppPath(c);
      mFile = new File(SqlHelper.copyDatabase(c, SqlConstants.DB_NAME, path.getAbsolutePath()));
      new UploadFileTask(c, app.getDropboxHelper(), this).execute(Arrays.asList(Uri.fromFile(mFile).toString(), PATH));
      return true;
    } catch (Exception e) {
      safeRemove();
      closeDialog();
      String err = c.getString(R.string.error) + e.getMessage();
      UIHelper.toastLong(c, err);
      Log.error(c, "exportDatabase", err, e);
    }
    return false;
  }

  /**
   * Removes safely
   */
  private void safeRemove() {
    if (mFile != null) {
      try {
        Files.delete(mFile.toPath());
      } catch (IOException e) {
        Log.error(mContext, "safeRemove", mContext.getString(R.string.error) + e.getMessage(), e);
      }
      mFile = null;
    }
  }

  /**
   * Called when the upload on dropbox is complete.
   *
   * @param result The upload result.
   */
  @Override
  public void onDropboxUploadComplete(FileMetadata result) {
    closeDialog();
    UIHelper.toast(mContext, mContext.getString(R.string.export_success));
    safeRemove();
    if (mDropboxUploaded != null)
      mDropboxUploaded.dropboxUploaded(false);
  }

  /**
   * Called when the upload on dropbox is finished on error.
   *
   * @param e Error exception.
   */
  @Override
  public void onDropboxUploadError(Exception e) {
    closeDialog();
    Log.error(mContext, "onDropboxUploadError", "Failed to upload file: " + e.getMessage(), e);
    UIHelper.toast(mContext, mContext.getString(R.string.error_dropbox_upload));
    safeRemove();
    if (mDropboxUploaded != null)
      mDropboxUploaded.dropboxUploaded(true);
  }

  /**
   * Called when the download on dropbox is complete.
   *
   * @param result The upload result.
   */
  @Override
  public void onDropboxDownloadComplete(File result) {
    closeDialog();
    try {
      loadDb(mContext, result);
    } catch (Exception e) {
      String err = mContext.getString(R.string.error) + ": " + e.getMessage();
      Log.error(mContext, "onDropboxDownloadComplete", err, e);
      UIHelper.toastLong(mContext, err);
    }
    safeRemove();
    if (mDropboxDownloaded != null)
      mDropboxDownloaded.dropboxDownloaded(false);
  }

  /**
   * Called when the download on dropbox is finished on error.
   *
   * @param e Error exception.
   */
  @Override
  public void onDropboxDownloadError(Exception e) {
    closeDialog();
    Log.error(mContext, "onDropboxDownloadError", "Failed to download file: " + e.getMessage(), e);
    UIHelper.toast(mContext, mContext.getString(R.string.error_dropbox_download));
    safeRemove();
    if (mDropboxDownloaded != null)
      mDropboxDownloaded.dropboxDownloaded(true);
  }


  /**
   * Called when the recovery of the file list of the dropbox is complete.
   *
   * @param result The listing result.
   */
  @Override
  public void onDropboxListFolderDataLoaded(ListFolderResult result) {
    closeDialog();
    List<Metadata> list = result.getEntries();
    list.sort(Comparator.comparing(Metadata::getName));
    if (list.isEmpty())
      UIHelper.toastLong(mContext, mContext.getString(R.string.error_no_files));
    else {
      computeAndLoad(mContext, list, new AlertDialogListListener() {
        @Override
        public void onClick(final Metadata m) {
          try {
            showDialog(mContext);
            ApplicationCtx app = ((ApplicationCtx) mContext.getApplicationContext());
            new DownloadFileTask(mContext, app.getDropboxHelper(), DropboxImportExport.this).execute((FileMetadata) m);
          } catch (Exception e) {
            String err = mContext.getString(R.string.error) + ": " + e.getMessage();
            Log.error(mContext, "onDropboxListFolderDataLoaded", err, e);
            UIHelper.toastLong(mContext, err);
          }
        }
      });
    }
  }

  /**
   * Called when the recovery of the file list of the dropbox is finished on error.
   *
   * @param e Error exception.
   */
  @Override
  public void onDropboxListFolderError(Exception e) {
    closeDialog();
    Log.error(mContext, "onDropboxListFolderError", mContext.getString(R.string.error_dropbox_list_directory) + ": " + e.getMessage(), e);
    UIHelper.toast(mContext, mContext.getString(R.string.error_dropbox_list_directory));
    if (mDropboxDownloaded != null)
      mDropboxDownloaded.dropboxDownloaded(true);
  }

  /**
   * Loads the database.
   *
   * @param c    The Android context.
   * @param file The db file to load.
   * @throws Exception If an exception is thrown.
   */
  public static void loadDb(final Context c, File file) throws Exception {
    SqlHelper.loadDatabase(c, SqlConstants.DB_NAME, file);
    ApplicationCtx app = (ApplicationCtx) c.getApplicationContext();
    app.getDaysFactory().setSqlFactory(app.getSql());
    app.getProfilesFactory().setSqlFactory(app.getSql());
    app.getPublicHolidaysFactory().setSqlFactory(app.getSql());
    AndroidHelper.restartApplication(c, R.string.import_success);
  }

  private void closeDialog() {
    if (mDialog != null)
      mDialog.dismiss();
    mDialog = null;
  }

  private void showDialog(Context context) {
    if (mDialog == null && !(context instanceof Service)) {
      mDialog = UIHelper.showProgressDialog(context, R.string.data_transfer);
      mDialog.show();
    }
  }

  /**
   * Prepares the files retrieved from dropbox and display the content in a dialog box.
   *
   * @param c    The Android context.
   * @param list The file list.
   * @param yes  Yes listener (dialog box)
   */
  public static void computeAndLoad(final Context c, final List<Metadata> list, AlertDialogListListener yes) {
    List<Metadata> files = compute(list);
    if (files.isEmpty())
      UIHelper.toastLong(c, c.getString(R.string.error_no_files));
    else {
      showAlertDialog(c, files, yes);
    }
  }

  /**
   * Prepares the list.
   *
   * @param list The output list.
   * @return List<String>
   */
  private static List<Metadata> compute(final List<Metadata> list) {
    List<Metadata> files = new ArrayList<>();
    for (Metadata t : list) {
      if (t instanceof FileMetadata) {
        FileMetadata f = (FileMetadata) t;
        if (f.getName().endsWith(".sqlite3"))
          files.add(f);
      }
    }
    return files;
  }

  private static class ListItem {
    public final String name;
    Metadata value;

    ListItem(final String name, final Metadata value) {
      this.name = name;
      this.value = value;
    }

    public @NonNull String toString() {
      return name;
    }
  }

  public interface AlertDialogListListener {
    /**
     * Called when the yes button is clicked.
     *
     * @param t The associated object.
     */
    void onClick(Metadata t);
  }

  /**
   * Displays an alert dialog with the files list retrieved from dropbox.
   *
   * @param c     The Android context.
   * @param list  The list to display.
   * @param yes   Yes listener (dialog box)
   */
  private static void showAlertDialog(final Context c, List<Metadata> list, final AlertDialogListListener yes) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setTitle(c.getResources().getString(R.string.box_select_db_file));
    builder.setIcon(android.R.drawable.ic_dialog_alert);
    List<ListItem> items = new ArrayList<>();
    for (Metadata s : list) {
      items.add(new ListItem(s.getName(), s));
    }
    final ArrayAdapter<ListItem> arrayAdapter = new ArrayAdapter<>(c, android.R.layout.select_dialog_item, items);
    builder.setNegativeButton(c.getString(R.string.cancel), (dialog, which) -> dialog.dismiss());

    builder.setAdapter(arrayAdapter, (dialog, which) -> {
      dialog.dismiss();
      ListItem li = arrayAdapter.getItem(which);
      if (yes != null && li != null) yes.onClick(li.value);
    });
    builder.show();
  }
}
