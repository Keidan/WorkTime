package fr.ralala.worktime.dropbox;


import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage exportation and importation using dropbox
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DropboxImportExport implements DropboxListener{
  private static final String PATH = "";
  private DropboxHelper helper = null;
  private ProgressDialog dialog = null;
  private File file = null;
  private Context c = null;
  private DropboxUploaded dropboxUploaded = null;
  private DropboxDownloaded dropboxDownloaded = null;


  public DropboxImportExport() {
    helper = DropboxHelper.helper();
  }

  public interface DropboxUploaded {
    void dropboxUploaded(final boolean error);
  }
  public interface DropboxDownloaded {
    void dropboxDownloaded(final boolean error);
  }

  public boolean importDatabase(final Context c, final DropboxDownloaded dropboxDownloaded) {
    this.c = c;
    this.dropboxDownloaded = dropboxDownloaded;
    if(helper.connect(c, c.getString(R.string.app_key))) {
      dialog.show();
      new ListFolderTask(helper.getClient(), this).execute(PATH);
      return true;
    }
    return false;
  }

  public boolean exportDatabase(final Context c, final DropboxUploaded dropboxUploaded) {
    return exportDatabase(c, true, dropboxUploaded);
  }

  public boolean exportDatabase(final Context c, boolean displayDialog, final DropboxUploaded dropboxUploaded) {
    this.c = c;
    this.dropboxUploaded = dropboxUploaded;
    if(dialog == null && displayDialog)
      dialog = buildProgress(c);
    if(helper.connect(c, c.getString(R.string.app_key))) {
      if(dialog != null)
        dialog.show();
      try {
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        file = new File(SqlHelper.copyDatabase(c, SqlHelper.DB_NAME, path.getAbsolutePath()));
        new UploadFileTask(c, helper.getClient(), this).execute(Uri.fromFile(file).toString(), PATH);
        return true;
      } catch(Exception e) {
        safeRemove();
        if(dialog != null)
          dialog.dismiss();
        AndroidHelper.toast_long(c, c.getString(R.string.error) + ": " + e.getMessage());
        Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
      }
    }
    return false;
  }

  private void safeRemove() {
    if(file != null) {
      file.delete();
      file = null;
    }
  }

  private ProgressDialog buildProgress(final Context c) {
    final ProgressDialog dialog = new ProgressDialog(c);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(false);
    dialog.setMessage(c.getString(R.string.data_transfer));
    return dialog;
  }

  @Override
  public void onDropboxUploadComplete(FileMetadata result) {
    if(dialog != null)
      dialog.dismiss();
    AndroidHelper.toast(c, c.getString(R.string.export_success));
    safeRemove();
    if(dropboxUploaded != null)
      dropboxUploaded.dropboxUploaded(false);
  }

  @Override
  public void onDropboxUploadError(Exception e) {
    if(dialog != null)
      dialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to upload file.", e);
    AndroidHelper.toast(c, c.getString(R.string.error_dropbox_upload));
    safeRemove();
    if(dropboxUploaded != null)
      dropboxUploaded.dropboxUploaded(true);
  }

  @Override
  public void onDroptboxDownloadComplete(File result) {
    if(dialog != null)
      dialog.dismiss();
    try {
      loadDb(c, result);
    } catch(Exception e) {
      AndroidHelper.toast_long(c, c.getString(R.string.error) + ": " + e.getMessage());
      Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
    }
    safeRemove();
    if(dropboxDownloaded != null)
      dropboxDownloaded.dropboxDownloaded(false);
  }

  @Override
  public void onDroptboxDownloadError(Exception e) {
    if(dialog != null)
      dialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to download file.", e);
    AndroidHelper.toast(c, c.getString(R.string.error_dropbox_download));
    safeRemove();
    if(dropboxDownloaded != null)
      dropboxDownloaded.dropboxDownloaded(true);
  }


  @Override
  public void onDroptboxListFoderDataLoaded(ListFolderResult result) {
    if(dialog != null)
      dialog.dismiss();
    List<Metadata> list = result.getEntries();
    Collections.sort(list, new Comparator<Metadata>() {
      @Override
      public int compare(Metadata m1, Metadata m2) {
        return m1.getName().compareTo(m2.getName());
      }
    });
    if (list.isEmpty())
      AndroidHelper.toast_long(c, c.getString(R.string.error_no_files));
    else {
      computeAndLoad(c, list, new AndroidHelper.AlertDialogListListener<Metadata>() {
        @Override
        public void onClick(final Metadata m) {
          try {
            if(dialog != null)
              dialog.show();
            new DownloadFileTask(c, helper.getClient(), DropboxImportExport.this).execute((FileMetadata)m);
          } catch (Exception e) {
            AndroidHelper.toast_long(c, c.getString(R.string.error) + ": " + e.getMessage());
            Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
          }
        }
      });
    }
  }

  @Override
  public void onDroptboxListFoderError(Exception e) {
    if(dialog != null)
      dialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to get the file list.", e);
    AndroidHelper.toast(c, c.getString(R.string.error_dropbox_list_directory));
    if(dropboxDownloaded != null)
      dropboxDownloaded.dropboxDownloaded(true);
  }

  public static void loadDb(final Context c, File file) throws Exception{
    SqlHelper.loadDatabase(c, SqlHelper.DB_NAME, file);
    MainApplication app = MainApplication.getApp(c);
    app.getDaysFactory().reload(app.getSql());
    app.getProfilesFactory().reload(app.getSql());
    app.getPublicHolidaysFactory().reload(app.getSql());
    AndroidHelper.toast(c, c.getString(R.string.import_success));
    AndroidHelper.restartApplication(c, -1);
  }

  public static <T, V> void computeAndLoad(final Context c, final List<T> list, AndroidHelper.AlertDialogListListener<V> yes) {
    List<String> files = compute(list);
    if(files.isEmpty())
      AndroidHelper.toast_long(c, c.getString(R.string.error_no_files));
    else {
      AndroidHelper.showAlertDialog(c, R.string.box_select_db_file, files, yes);
    }
  }

  private static <T, V> List<V> compute(final List<T> list) {
    List<V> files = new ArrayList<>();
    for(T t : list) {
      if(File.class.isInstance(t)) {
        File f = (File)t;
        if (f.getName().endsWith(".sqlite3"))
          files.add((V)f);
      } else if(FileMetadata.class.isInstance(t)) {
        FileMetadata f = (FileMetadata)t;
        if (f.getName().endsWith(".sqlite3"))
          files.add((V)f);
      }
    }
    return files;
  }
}
