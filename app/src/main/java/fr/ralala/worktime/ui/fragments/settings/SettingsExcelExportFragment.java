package fr.ralala.worktime.ui.fragments.settings;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings (Excel export part)
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsExcelExportFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
  public static final String PREFS_DEF_VAL_EMAIL = "";
  public static final boolean PREFS_DEF_VAL_EMAIL_ENABLE = true;
  public static final boolean PREFS_DEF_VAL_EXPORT_HIDE_WAGE = false;
  public static final String PREFS_KEY_EXPORT_HIDE_WAGE = "prefExportHideWage";
  public static final String PREFS_KEY_EMAIL = "prefExportMail";
  public static final String PREFS_KEY_EMAIL_ENABLE = "prefExportMailEnable";

  private Preference mPrefExportMail;
  private Preference mPrefExportMailEnable;

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
    setPreferencesFromResource(R.xml.preferences_excel_export, rootKey);
    mPrefExportMail = findPreference(PREFS_KEY_EMAIL);
    mPrefExportMailEnable = findPreference(PREFS_KEY_EMAIL_ENABLE);
    if (mPrefExportMailEnable != null)
      mPrefExportMailEnable.setOnPreferenceClickListener(this);
  }

  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mPrefExportMailEnable)) {
      mPrefExportMail.setEnabled(!mPrefExportMail.isEnabled());
    }
    return false;
  }
}
