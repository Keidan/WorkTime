package fr.ralala.worktime.ui.fragments.settings;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.LogsActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsDatabaseActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsDisplayActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsExcelExportActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsLearningActivity;
import fr.ralala.worktime.ui.changelog.ChangeLog;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
  public static final String PREFS_DEF_VAL_WORK_TIME_BY_DAY = "00:00";
  public static final String PREFS_DEF_VAL_AMOUNT_BY_HOUR = "0.0";
  public static final String PREFS_DEF_VAL_CURRENCY = "€";
  public static final String PREFS_KEY_WORK_TIME_BY_DAY = "prefWorkTimeByDay";
  public static final String PREFS_KEY_AMOUNT_BY_HOUR = "prefAmountByHour";
  public static final String PREFS_KEY_CURRENCY = "prefCurrency";
  public static final String PREFS_KEY_CHANGELOG = "prefChangelog";
  public static final String PREFS_KEY_VERSION = "prefVersion";
  public static final String PREFS_KEY_SELECT_DISPLAY = "prefSelectDisplay";
  public static final String PREFS_KEY_SELECT_EXCEL_EXPORT = "prefSelectExcelExport";
  public static final String PREFS_KEY_SELECT_LEARNING = "prefSelectLearning";
  public static final String PREFS_KEY_SELECT_DATABASE = "prefSelectDatabaseExport";
  public static final String PREFS_KEY_LOGS = "prefLogs";

  private ChangeLog mChangeLog = null;
  private final AppCompatActivity mActivity;

  private Preference mPrefChangelog;
  private Preference mPrefSelectDisplay;
  private Preference mPrefSelectExcelExport;
  private Preference mPrefSelectLearning;
  private Preference mPrefSelectDatabaseExport;
  private Preference mLogs;

  public SettingsFragment(AppCompatActivity owner) {
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
    setPreferencesFromResource(R.xml.preferences, rootKey);

    mChangeLog = ((MainApplication) mActivity.getApplication()).getChangeLog();
    mPrefChangelog = findPreference(PREFS_KEY_CHANGELOG);
    mPrefSelectDisplay = findPreference(PREFS_KEY_SELECT_DISPLAY);
    mPrefSelectExcelExport = findPreference(PREFS_KEY_SELECT_EXCEL_EXPORT);
    mPrefSelectLearning = findPreference(PREFS_KEY_SELECT_LEARNING);
    mPrefSelectDatabaseExport = findPreference(PREFS_KEY_SELECT_DATABASE);
    mLogs = findPreference(PREFS_KEY_LOGS);

    mPrefChangelog.setOnPreferenceClickListener(this);
    mPrefSelectDisplay.setOnPreferenceClickListener(this);
    mPrefSelectExcelExport.setOnPreferenceClickListener(this);
    mPrefSelectLearning.setOnPreferenceClickListener(this);
    mPrefSelectDatabaseExport.setOnPreferenceClickListener(this);
    mLogs.setOnPreferenceClickListener(this);
  }

  @Override
  public void onDisplayPreferenceDialog(Preference preference) {
    if (preference instanceof TimePreference) {
      DialogFragment dialogFragment = TimePreferenceDialogFragment.newInstance(preference.getKey());
      dialogFragment.setTargetFragment(this, 0);
      dialogFragment.show(getFragmentManager(), null);
    } else super.onDisplayPreferenceDialog(preference);
  }

  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mPrefSelectDisplay)) {
      startActivity(new Intent(mActivity, SettingsDisplayActivity.class));
    } else if (preference.equals(mPrefSelectDatabaseExport)) {
      startActivity(new Intent(mActivity, SettingsDatabaseActivity.class));
    } else if (preference.equals(mPrefSelectExcelExport)) {
      startActivity(new Intent(mActivity, SettingsExcelExportActivity.class));
    } else if (preference.equals(mPrefSelectLearning)) {
      startActivity(new Intent(mActivity, SettingsLearningActivity.class));
    } else if (preference.equals(mPrefChangelog)) {
      mChangeLog.getFullLogDialog(mActivity).show();
    } else if (preference.equals(mLogs)) {
      LogsActivity.startActivity(mActivity);
    }
    return false;
  }
}
