package fr.ralala.worktime.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;

import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.dropbox.DownloadFileTask;
import fr.ralala.worktime.dropbox.DropboxListener;
import fr.ralala.worktime.dropbox.ListFolderTask;
import fr.ralala.worktime.dropbox.UploadFileTask;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.dropbox.DropboxHelper;


/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the db import/export
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsImportExportActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener, DropboxListener {
  public static final String       PREFS_KEY_EXPORT_TO_DEVICE                 = "prefExportToDevice";
  public static final String       PREFS_KEY_IMPORT_FROM_DEVICE               = "prefImportFromDevice";
  public static final String       PREFS_KEY_EXPORT_TO_DROPBOX                = "prefExportToDropbox";
  public static final String       PREFS_KEY_IMPORT_FROM_DROPBOX              = "prefImportFromDropbox";
  private static final String      PATH                                       = "";

  private MyPreferenceFragment prefFrag                       = null;
  private ProgressDialog dialog = null;
  private File file = null;
  private DropboxHelper helper = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    AndroidHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    helper = DropboxHelper.helper();
    prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    android.support.v7.app.ActionBar actionBar = AppCompatDelegate.create(this, null).getSupportActionBar();
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    MainApplication app = (MainApplication)getApplicationContext();
    prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DROPBOX).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX).setOnPreferenceClickListener(this);
    dialog = buildProgress();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    AndroidHelper.closeAnimation(this);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId())
    {
      case android.R.id.home:
        NavUtils.navigateUpFromSameTask(this);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private ProgressDialog buildProgress() {
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    dialog.setCancelable(false);
    dialog.setMessage(getString(R.string.data_transfer));
    return dialog;
  }

  private void safeRemove() {
    if(file != null) {
      file.delete();
      file = null;
    }
  }

  @Override
  public void onDropboxUploadComplete(FileMetadata result) {
    dialog.dismiss();
    AndroidHelper.toast(SettingsImportExportActivity.this, getString(R.string.export_success));
    safeRemove();
  }

  @Override
  public void onDropboxUploadError(Exception e) {
    dialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to upload file.", e);
    AndroidHelper.toast(this, getString(R.string.error_dropbox_upload));
    safeRemove();
  }

  @Override
  public void onDroptboxDownloadComplete(File result) {
    dialog.dismiss();
    try {
      loadDb(result);
    } catch(Exception e) {
      AndroidHelper.toast_long(this, getString(R.string.error) + ": " + e.getMessage());
      Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
    }
    safeRemove();
  }

  @Override
  public void onDroptboxDownloadError(Exception e) {
    dialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to download file.", e);
    AndroidHelper.toast(this, getString(R.string.error_dropbox_download));
    safeRemove();
  }


  @Override
  public void onDroptboxListFoderDataLoaded(ListFolderResult result) {
    dialog.dismiss();
    List<Metadata> list = result.getEntries();
    Collections.sort(list, new Comparator<Metadata>() {
      @Override
      public int compare(Metadata m1, Metadata m2) {
        return m1.getName().compareTo(m2.getName());
      }
    });
    if (list.isEmpty())
      AndroidHelper.toast_long(this, getString(R.string.error_no_files));
    else {
      computeAndLoad(list, new AndroidHelper.AlertDialogListListener<Metadata>() {
        @Override
        public void onClick(final Metadata m) {
          try {
            dialog.show();
            new DownloadFileTask(SettingsImportExportActivity.this, helper.getClient(), SettingsImportExportActivity.this).execute((FileMetadata)m);
          } catch (Exception e) {
            AndroidHelper.toast_long(SettingsImportExportActivity.this, getString(R.string.error) + ": " + e.getMessage());
            Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
          }
        }
      });
    }
  }

  @Override
  public void onDroptboxListFoderError(Exception e) {
    dialog.dismiss();
    Log.e(getClass().getSimpleName(), "Failed to get the file list.", e);
    AndroidHelper.toast(this, getString(R.string.error_dropbox_list_directory));
  }

  @Override
  public boolean onPreferenceClick(final Preference preference) {
    if (preference.equals(prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DROPBOX))) {
      if(helper.connect(this, getString(R.string.app_key))) {
        dialog.show();
        try {
          File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
          file = new File(SqlHelper.copyDatabase(this, SqlHelper.DB_NAME, path.getAbsolutePath()));
          new UploadFileTask(this, helper.getClient(), this).execute(Uri.fromFile(file).toString(), PATH);
        } catch(Exception e) {
          safeRemove();
          dialog.dismiss();
          AndroidHelper.toast_long(this, getString(R.string.error) + ": " + e.getMessage());
          Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
        }
      }
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX))) {
      if(helper.connect(this, getString(R.string.app_key))) {
        dialog.show();
        new ListFolderTask(helper.getClient(), this).execute(PATH);
      }
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_export));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_folder) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
      myStartActivity(extra, FileChooserActivity.class, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TYPE_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_TYPE_FILE_AND_DIRECTORY);
      extra.put(AbstractFileChooserActivity.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_import));
      extra.put(AbstractFileChooserActivity.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_file) + ":? ");
      extra.put(AbstractFileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
      extra.put(AbstractFileChooserActivity.FILECHOOSER_SHOW_KEY, "" + AbstractFileChooserActivity.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
      myStartActivity(extra, FileChooserActivity.class, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE);
    }
    return true;
  }

  @Override
  protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    // Check which request we're responding to
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == RESULT_OK) {
        String dir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        try {
          SqlHelper.copyDatabase(this, SqlHelper.DB_NAME, dir);
          AndroidHelper.toast(this, getString(R.string.export_success));
        } catch(Exception e) {
          AndroidHelper.toast_long(this, getString(R.string.error) + ": " + e.getMessage());
          Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
        }
      }
    } else if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_FILE) {
      if (resultCode == RESULT_OK) {
        String file = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        Log.d(getClass().getSimpleName(), "Selected file: '" + file + "'");
        List<File> files = new ArrayList<>();
        for(File f : new File(file).listFiles()) {
          if(f.getName().endsWith(".sqlite3"))
            files.add(f);
        }
        Collections.sort(files, new Comparator<File>() {
          @Override
          public int compare(File f1, File f2) {
            return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()) ;
          }
        });
        computeAndLoad(files, new AndroidHelper.AlertDialogListListener<String>() {
          @Override
          public void onClick(String s) {
            try {
              loadDb(new File(s));
            } catch (Exception e) {
              AndroidHelper.toast_long(SettingsImportExportActivity.this, getString(R.string.error) + ": " + e.getMessage());
              Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
            }
          }
        });
      }
    }
  }

  private void loadDb(File file) throws Exception{
    SqlHelper.loadDatabase(SettingsImportExportActivity.this, SqlHelper.DB_NAME, file);
    MainApplication app = MainApplication.getApp(SettingsImportExportActivity.this);
    app.getDaysFactory().reload(app.getSql());
    app.getProfilesFactory().reload(app.getSql());
    app.getPublicHolidaysFactory().reload(app.getSql());
    AndroidHelper.toast(SettingsImportExportActivity.this, getString(R.string.import_success));
    AndroidHelper.restartApplication(this, -1);
  }

  private <T, V> void computeAndLoad(final List<T> list, AndroidHelper.AlertDialogListListener<V> yes) {
    List<String> files = compute(list);
    if(files.isEmpty())
      AndroidHelper.toast_long(this, getString(R.string.error_no_files));
    else {
      AndroidHelper.showAlertDialog(this, R.string.box_select_db_file, files, yes);
    }
  }

  private <T, V> List<V> compute(final List<T> list) {
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

  private void myStartActivity(Map<String, String> extra, Class<?> c, int code) {
    final Intent i = new Intent(getApplicationContext(), c);
    if(extra != null) {
      Set<String> keysSet = extra.keySet();
      for (String key : keysSet) {
        i.putExtra(key, extra.get(key));
      }
    }
    startActivityForResult(i, code);
  }

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences_import_export);
    }
  }
}
