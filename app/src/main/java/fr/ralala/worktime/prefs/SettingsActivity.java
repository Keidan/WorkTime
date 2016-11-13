package fr.ralala.worktime.prefs;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import fr.ralala.worktime.AndroidHelper;
import fr.ralala.worktime.MainActivity;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.chooser.FileChooser;
import fr.ralala.worktime.chooser.FileChooserActivity;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.sql.SqlHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{
  public static final String       PREFS_KEY_WORKTIME_BY_DAY      = "prefWorkTimeByDay";
  public static final String       PREFS_KEY_AMOUNT_BY_HOUR       = "prefAmountByHour";
  public static final String       PREFS_KEY_CURRENCY             = "prefCurrency";
  public static final String       PREFS_KEY_EXPORT               = "prefExport";
  public static final String       PREFS_KEY_IMPORT               = "prefImport";
  public static final WorkTimeDay  PREFS_DEFAULT_WORKTIME_BY_DAY  = new WorkTimeDay(0, 0, 0, 8, 0);
  public static final double       PREFS_DEFAULT_AMOUNT_BY_HOUR   = 0.0;
  public static final String       PREFS_DEFAULT_CURRENCY         = "\u0024";
  private MyPreferenceFragment     prefFrag                       = null;
  private SharedPreferences        prefs                          = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    prefFrag.findPreference(PREFS_KEY_EXPORT)
      .setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT)
      .setOnPreferenceClickListener(this);
  }


  @Override
  public boolean onPreferenceClick(final Preference preference) {
    if (preference.equals(prefFrag.findPreference(PREFS_KEY_EXPORT))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(FileChooser.FILECHOOSER_TYPE_KEY, "" + FileChooser.FILECHOOSER_TYPE_DIRECTORY_ONLY);
      extra.put(FileChooser.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_export));
      extra.put(FileChooser.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_folder) + ":? ");
      extra.put(FileChooser.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
      extra.put(FileChooser.FILECHOOSER_SHOW_KEY, "" + FileChooser.FILECHOOSER_SHOW_DIRECTORY_ONLY);
      myStartActivity(extra, FileChooserActivity.class, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_IMPORT))) {
      Map<String, String> extra = new HashMap<>();
      extra.put(FileChooser.FILECHOOSER_TYPE_KEY, "" + FileChooser.FILECHOOSER_TYPE_FILE_AND_DIRECTORY);
      extra.put(FileChooser.FILECHOOSER_TITLE_KEY, getString(R.string.pref_title_import));
      extra.put(FileChooser.FILECHOOSER_MESSAGE_KEY, getString(R.string.use_file) + ":? ");
      extra.put(FileChooser.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
      extra.put(FileChooser.FILECHOOSER_SHOW_KEY, "" + FileChooser.FILECHOOSER_SHOW_FILE_AND_DIRECTORY);
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
        try {
          SqlHelper.loadDatabase(this, SqlHelper.DB_NAME, new File(file));
          AndroidHelper.toast(this, getString(R.string.import_success));;
        } catch(Exception e) {
          AndroidHelper.toast_long(this, getString(R.string.error) + ": " + e.getMessage());
          Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
        }
      }
    }
  }

  private void myStartActivity(Map<String, String> extra, Class<?> c, int code) {
    final Intent i = new Intent(getApplicationContext(), c);
    Set<String> keysSet = extra.keySet();
    for(Iterator<String> keys = keysSet.iterator(); keys.hasNext();) {
      String key = keys.next();
      i.putExtra(key, extra.get(key));
    }
    startActivityForResult(i, code);
  }

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences);
    }
  }
}
