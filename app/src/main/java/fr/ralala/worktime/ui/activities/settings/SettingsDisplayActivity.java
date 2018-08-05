package fr.ralala.worktime.ui.activities.settings;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.utils.UIHelper;

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
  public static final String PREFS_DEFVAL_DEFAULT_HOME = "0";
  public static final String PREFS_DEFVAL_DAY_ROWS_HEIGHT = "44";
  public static final String PREFS_DEFVAL_SCROLL_TO_CURRENT_DAY = "false";
  public static final String PREFS_DEFVAL_HIDE_WAGE = "false";
  public static final String PREFS_DEFVAL_HIDE_EXIT_BUTTON = "false";
  public static final String PREFS_KEY_HIDE_WAGE = "prefHideWage";
  public static final String PREFS_KEY_DAY_ROWS_HEIGHT = "prefDayRowsHeight";
  public static final String PREFS_KEY_DEFAULT_HOME = "prefDefaultHome";
  public static final String PREFS_KEY_SCROLL_TO_CURRENT_DAY = "prefScrollToCurrentDay";
  public static final String PREFS_KEY_HIDE_EXIT_BUTTON = "prefHideExitButton";


  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  public void onCreate(final Bundle savedInstanceState) {
    UIHelper.openAnimation(this);
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

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    MainApplication.getApp(this).getSql().settingsLoad(null);
  }

  /**
   * Called when the activity is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    MainApplication.getApp(this).getSql().settingsSave();
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    UIHelper.closeAnimation(this);
  }

  /**
   * Called when the options item is clicked (home).
   * @param item The selected menu.
   * @return boolean
   */
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
