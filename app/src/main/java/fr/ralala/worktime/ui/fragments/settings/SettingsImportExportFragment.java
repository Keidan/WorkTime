package fr.ralala.worktime.ui.fragments.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.settings.SettingsImportExportActivity;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the db import/export
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsImportExportFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
  public static final String PREFS_KEY_EXPORT_TO_DROPBOX = "prefExportToDropbox";
  public static final String PREFS_KEY_IMPORT_FROM_DROPBOX = "prefImportFromDropbox";

  private Preference mPrefExportToDropbox;
  private Preference mPrefImportFromDropbox;
  private final MainApplication mApp;
  private final SettingsImportExportActivity mActivity;

  public SettingsImportExportFragment(SettingsImportExportActivity act) {
    mActivity = act;
    mApp = (MainApplication) act.getApplication();
  }

  /**
   * Called during {@link #onCreate(Bundle)} to supply the preferences for this fragment.
   * Subclasses are expected to call {@link #setPreferenceScreen(PreferenceScreen)} either
   * directly or via helper methods such as {@link #addPreferencesFromResource(int)}.
   *
   * @param savedInstanceState If the fragment is being re-created from a previous saved state,
   *                           this is the state.
   * @param rootKey            If non-null, this preference fragment should be rooted at the
   *                           {@link PreferenceScreen} with this key.
   */
  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences_import_export, rootKey);
    mPrefExportToDropbox = findPreference(PREFS_KEY_EXPORT_TO_DROPBOX);
    mPrefImportFromDropbox = findPreference(PREFS_KEY_IMPORT_FROM_DROPBOX);

    mPrefExportToDropbox.setOnPreferenceClickListener(this);
    mPrefImportFromDropbox.setOnPreferenceClickListener(this);

  }

  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mPrefExportToDropbox)) {
      mApp.setLastExportType(MainApplication.PREFS_VAL_LAST_EXPORT_DROPBOX);
      AndroidHelper.exportDropbox(mApp, mActivity);
    } else if (preference.equals(mPrefImportFromDropbox)) {
      mApp.getDropboxImportExport().importDatabase(mActivity);
    }
    return false;
  }
}
