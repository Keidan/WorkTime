package fr.ralala.worktime.ui.fragments.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.settings.SettingsImportExportActivity;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings (database import/export part)
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsDatabaseFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
  public static final boolean PREFS_DEF_VAL_IMPORT_EXPORT_AUTO_SAVE = false;
  public static final String PREFS_DEF_VAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY = "0";
  public static final String PREFS_KEY_IMPORT_EXPORT = "prefImportExport";
  public static final String PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE = "prefImportExportAutoSave";
  public static final String PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY = "prefImportExportAutoSavePeriodicity";
  private final AppCompatActivity mActivity;
  private Preference mPrefImportExport;
  private CheckBoxPreference mPrefImportExportAutoSave;
  private Preference mPrefImportExportAutoSavePeriodicity;

  public SettingsDatabaseFragment(AppCompatActivity owner) {
    mActivity = owner;
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
    setPreferencesFromResource(R.xml.preferences_database, rootKey);
    mPrefImportExport = findPreference(PREFS_KEY_IMPORT_EXPORT);
    mPrefImportExportAutoSave = findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE);
    mPrefImportExportAutoSavePeriodicity = findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY);

    mPrefImportExport.setOnPreferenceClickListener(this);
    mPrefImportExportAutoSave.setOnPreferenceClickListener(this);

    mPrefImportExportAutoSavePeriodicity.setEnabled(mPrefImportExportAutoSave.isChecked());
  }

  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mPrefImportExport)) {
      startActivity(new Intent(mActivity, SettingsImportExportActivity.class));
    } else if (preference.equals(mPrefImportExportAutoSave)) {
      mPrefImportExportAutoSavePeriodicity.setEnabled(mPrefImportExportAutoSave.isChecked());
    }
    return false;
  }
}
