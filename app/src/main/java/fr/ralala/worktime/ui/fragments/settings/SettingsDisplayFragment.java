package fr.ralala.worktime.ui.fragments.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings (display part)
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsDisplayFragment extends PreferenceFragmentCompat {
  public static final String PREFS_DEF_VAL_DEFAULT_HOME = "0";
  public static final String PREFS_DEF_VAL_DAY_ROWS_HEIGHT = "80";
  public static final boolean PREFS_DEF_VAL_SCROLL_TO_CURRENT_DAY = false;
  public static final boolean PREFS_DEF_VAL_HIDE_WAGE = false;
  public static final boolean PREFS_DEF_VAL_HIDE_EXIT_BUTTON = false;
  public static final boolean PREFS_DEF_VAL_DISPLAY_WEEK = true;
  public static final String PREFS_KEY_HIDE_WAGE = "prefHideWage";
  public static final String PREFS_KEY_DAY_ROWS_HEIGHT = "prefDayRowsHeight";
  public static final String PREFS_KEY_DEFAULT_HOME = "prefDefaultHome";
  public static final String PREFS_KEY_SCROLL_TO_CURRENT_DAY = "prefScrollToCurrentDay";
  public static final String PREFS_KEY_HIDE_EXIT_BUTTON = "prefHideExitButton";
  public static final String PREFS_KEY_DISPLAY_WEEK = "prefDayDisplayWeek";

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
    setPreferencesFromResource(R.xml.preferences_display, rootKey);
  }
}
