package fr.ralala.worktime.prefs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import fr.ralala.worktime.R;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsActivity extends PreferenceActivity{
  public static final String       PREFS_KEY_WORKTIME_BY_DAY      = "prefWorkTimeByDay";
  public static final String       PREFS_KEY_AMOUNT_BY_HOUR       = "prefAmountByHour";
  public static final String       PREFS_KEY_CURRENCY             = "prefCurrency";
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
