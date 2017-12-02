package fr.ralala.worktime.ui.activities.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings (display part)
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsDisplayActivity extends PreferenceActivity {
  public static final String       PREFS_DEFVAL_DEFAULT_HOME                        = "0";
  public static final String       PREFS_DEFVAL_DAY_ROWS_HEIGHT                     = "44";
  public static final String       PREFS_DEFVAL_SCROLL_TO_CURRENT_DAY               = "false";
  public static final String       PREFS_DEFVAL_HIDE_WAGE                           = "false";
  public static final String       PREFS_DEFVAL_HIDE_EXIT_BUTTON                    = "false";
  public static final String       PREFS_KEY_HIDE_WAGE                              = "prefHideWage";
  public static final String       PREFS_KEY_DAY_ROWS_HEIGHT                        = "prefDayRowsHeight";
  public static final String       PREFS_KEY_DEFAULT_HOME                           = "prefDefaultHome";
  public static final String       PREFS_KEY_SCROLL_TO_CURRENT_DAY                  = "prefScrollToCurrentDay";
  public static final String       PREFS_KEY_HIDE_EXIT_BUTTON                       = "prefHideExitButton";


  @Override
  public void onCreate(final Bundle savedInstanceState) {
    AndroidHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    MyPreferenceFragment prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    AppCompatDelegate delegate = AppCompatDelegate.create(this, null);
    android.support.v7.app.ActionBar actionBar = delegate.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    MainApplication.getApp(this).getSql().settingsLoad(null);
  }

  @Override
  public void onPause() {
    super.onPause();
    MainApplication.getApp(this).getSql().settingsSave();
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

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences_display);
    }
  }
}
