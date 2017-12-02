package fr.ralala.worktime.ui.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.MenuItem;


import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.changelog.ChangeLog;
import fr.ralala.worktime.ui.changelog.ChangeLogIds;
import fr.ralala.worktime.utils.AndroidHelper;

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
  public static final String       PREFS_DEFVAL_WORKTIME_BY_DAY                     = "00:00";
  public static final String       PREFS_DEFVAL_AMOUNT_BY_HOUR                      = "0.0";
  public static final String       PREFS_DEFVAL_CURRENCY                            = "€";
  public static final String       PREFS_KEY_WORKTIME_BY_DAY                        = "prefWorkTimeByDay";
  public static final String       PREFS_KEY_AMOUNT_BY_HOUR                         = "prefAmountByHour";
  public static final String       PREFS_KEY_CURRENCY                               = "prefCurrency";
  public static final String       PREFS_KEY_CHANGELOG                              = "prefChangelog";
  public static final String       PREFS_KEY_VERSION                                = "prefVersion";
  public static final String       PREFS_KEY_SELECT_DISPLAY                         = "prefSelectDisplay";
  public static final String       PREFS_KEY_SELECT_EXCEL_EXPORT                    = "prefSelectExcelExport";
  public static final String       PREFS_KEY_SELECT_LEARNING                        = "prefSelectLearning";
  public static final String       PREFS_KEY_SELECT_DATABASE                        = "prefSelectDatabaseExport";


  private MyPreferenceFragment mPrefFrag = null;
  private ChangeLog mChangeLog = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    AndroidHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    mPrefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, mPrefFrag).commit();
    getFragmentManager().executePendingTransactions();

    AppCompatDelegate delegate = AppCompatDelegate.create(this, null);
    android.support.v7.app.ActionBar actionBar = delegate.getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    mChangeLog = new ChangeLog(
      new ChangeLogIds(
        R.raw.changelog,
        R.string.changelog_ok_button,
        R.string.background_color,
        R.string.changelog_title,
        R.string.changelog_full_title,
        R.string.changelog_show_full), this);

    mPrefFrag.findPreference(PREFS_KEY_SELECT_DISPLAY).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_SELECT_LEARNING).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_SELECT_EXCEL_EXPORT).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_SELECT_DATABASE).setOnPreferenceClickListener(this);


    mPrefFrag.findPreference(PREFS_KEY_VERSION).setTitle(getResources().getString(R.string.app_name));
    try {
      mPrefFrag.findPreference(PREFS_KEY_VERSION).setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      mPrefFrag.findPreference(PREFS_KEY_VERSION).setSummary(e.getMessage());
    }
    mPrefFrag.findPreference(PREFS_KEY_CHANGELOG).setOnPreferenceClickListener(this);

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


  @Override
  public boolean onPreferenceClick(final Preference preference) {
    if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_SELECT_DISPLAY))) {
      startActivity(new Intent(getApplicationContext(), SettingsDisplayActivity.class));
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_SELECT_DATABASE))) {
      startActivity(new Intent(getApplicationContext(), SettingsDatabaseActivity.class));
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_SELECT_EXCEL_EXPORT))) {
      startActivity(new Intent(getApplicationContext(), SettingsExcelExportActivity.class));
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_SELECT_LEARNING))) {
      startActivity(new Intent(getApplicationContext(), SettingsLearningActivity.class));
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_CHANGELOG))) {
      mChangeLog.getFullLogDialog().show();
    }
    return true;
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
