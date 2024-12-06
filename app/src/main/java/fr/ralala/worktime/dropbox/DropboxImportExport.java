package fr.ralala.worktime.dropbox;


import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
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

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.sql.SqlConstants;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;

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
  private static final String ERROR = "Error: ";
  private static final String PATH = "";
  private final DropboxHelper mHelper;
  private AlertDialog mDialog = null;
  private File mFile = null;
  private Context mContext = null;
  private DropboxUploaded mDropboxUploaded = null;
  private DropboxDownloaded mDropboxDownloaded = null;


  /**
   * Creates the import/export instance.
   */
  public DropboxImportExport() {
    mHelper = DropboxHelper.helper();
  }

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
    if (mHelper.connect(c, c.getString(R.string.app_key))) {
      if (mDialog == null)
        mDialog = UIHelper.showProgressDialog(c, R.string.data_transfer);
      mDialog.show();
      new ListFolderTask(mHelper.getClient(), this).execute(PATH);
    }
  }

  /**
   * Exports the database.
   *
   * @param c               The Android context.
   * @param displayDialog   True to display a dialog box.
   * @param dropboxUploaded The export listener.
   * @return boolean
   */
  public boolean exportDatabase(final Context c, boolean displayDialog, final DropboxUploaded dropboxUploaded) {
    mContext = c;
    mDropboxUploaded = dropboxUploaded;
    if (mDialog == null && displayDialog)
      mDialog = UIHelper.showProgressDialog(c, R.string.data_transfer);
    if (mHelper.connect(c, c.getString(R.string.app_key))) {
      if (mDialog != null)
        mDialog.show();
      try {
        File path = AndroidHelper.getAppPath(c);
        mFile = new File(SqlHelper.copyDatabase(c, SqlConstants.DB_NAME, path.getAbsolutePath()));
        new UploadFileTask(c, mHelper.getClient(), this).execute(Arrays.asList(Uri.fromFile(mFile).toString(), PATH));
        return true;
      } catch (Exception e) {
        safeRemove();
        if (mDialog != null)
          mDialog.dismiss();
        UIHelper.toastLong(c, c.getString(R.string.error) + ": " + e.getMessage());
        Log.e(getClass().getSimpleName(), ERROR + e.getMessage(), e);
      }
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
        Log.e(getClass().getSimpleName(), ERROR + e.getMessage(), e);
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
    if (mDialog != null)
      mDialog.dismiss();
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
    if (mDialog != null)
      mDialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to upload file.", e);
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
    if (mDialog != null)
      mDialog.dismiss();
    try {
      loadDb(mContext, result);
    } catch (Exception e) {
      UIHelper.toastLong(mContext, mContext.getString(R.string.error) + ": " + e.getMessage());
      Log.e(getClass().getSimpleName(), ERROR + e.getMessage(), e);
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
    if (mDialog != null)
      mDialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to download file.", e);
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
    if (mDialog != null)
      mDialog.dismiss();
    List<Metadata> list = result.getEntries();
    list.sort(Comparator.comparing(Metadata::getName));
    if (list.isEmpty())
      UIHelper.toastLong(mContext, mContext.getString(R.string.error_no_files));
    else {
      computeAndLoad(mContext, list, new AlertDialogListListener() {
        @Override
        public void onClick(final Metadata m) {
          try {
            if (mDialog != null)
              mDialog.show();
            new DownloadFileTask(mContext, mHelper.getClient(), DropboxImportExport.this).execute((FileMetadata) m);
          } catch (Exception e) {
            UIHelper.toastLong(mContext, mContext.getString(R.string.error) + ": " + e.getMessage());
            Log.e(getClass().getSimpleName(), ERROR + e.getMessage(), e);
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
    if (mDialog != null)
      mDialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to get the file list.", e);
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
    MainApplication app = (MainApplication) c.getApplicationContext();
    app.getDaysFactory().setSqlFactory(app.getSql());
    app.getProfilesFactory().setSqlFactory(app.getSql());
    app.getPublicHolidaysFactory().setSqlFactory(app.getSql());
    AndroidHelper.restartApplication(c, R.string.import_success);
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
      showAlertDialog(c, R.string.box_select_db_file, files, yes);
    }
  }

  /**
   * Prepares the list.
   *
   * @param list The output list.
   * @return List<String>
   */
  @SuppressWarnings("unchecked")
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
   * @param title The dialog title.
   * @param list  The list to display.
   * @param yes   Yes listener (dialog box)
   * @param <T>   File type.
   */
  @SuppressWarnings("unchecked")
  private static void showAlertDialog(final Context c, final int title, List<Metadata> list, final AlertDialogListListener yes) {
    AlertDialog.Builder builder = new AlertDialog.Builder(c);
    builder.setTitle(c.getResources().getString(title));
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
