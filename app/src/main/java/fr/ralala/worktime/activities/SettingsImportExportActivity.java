package fr.ralala.worktime.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;

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
import fr.ralala.worktime.dropbox.DropboxImportExport;
import fr.ralala.worktime.services.DropboxAutoExportService;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.utils.AndroidHelper;


/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the db import/export
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsImportExportActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {
  public static final String       PREFS_KEY_EXPORT_TO_DEVICE                 = "prefExportToDevice";
  public static final String       PREFS_KEY_IMPORT_FROM_DEVICE               = "prefImportFromDevice";
  public static final String       PREFS_KEY_EXPORT_TO_DROPBOX                = "prefExportToDropbox";
  public static final String       PREFS_KEY_IMPORT_FROM_DROPBOX              = "prefImportFromDropbox";
  private static final String      PATH                                       = "";

  private MyPreferenceFragment prefFrag                       = null;
  private MainApplication app = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    AndroidHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    app = MainApplication.getApp(this);
    prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    android.support.v7.app.ActionBar actionBar = AppCompatDelegate.create(this, null).getSupportActionBar();
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DEVICE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DEVICE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DROPBOX).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX).setOnPreferenceClickListener(this);
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


  @Override
  public boolean onPreferenceClick(final Preference preference) {
    if (preference.equals(prefFrag.findPreference(PREFS_KEY_EXPORT_TO_DROPBOX))) {
      DropboxAutoExportService.setNeedUpdate(app, false);
      app.getDropboxImportExport().exportDatabase(this, null);
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX))) {
      app.getDropboxImportExport().importDatabase(this, null);
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
        DropboxImportExport.computeAndLoad(this, files, new AndroidHelper.AlertDialogListListener<String>() {
          @Override
          public void onClick(String s) {
            try {
              DropboxImportExport.loadDb(SettingsImportExportActivity.this, new File(s));
            } catch (Exception e) {
              AndroidHelper.toast_long(SettingsImportExportActivity.this, getString(R.string.error) + ": " + e.getMessage());
              Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
            }
          }
        });
      }
    }
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
