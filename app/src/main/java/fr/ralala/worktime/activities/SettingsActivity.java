package fr.ralala.worktime.activities;

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
import fr.ralala.worktime.changelog.ChangeLog;
import fr.ralala.worktime.changelog.ChangeLogIds;
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
  public static final String       PREFS_KEY_WORKTIME_BY_DAY            = "prefWorkTimeByDay";
  public static final String       PREFS_KEY_AMOUNT_BY_HOUR             = "prefAmountByHour";
  public static final String       PREFS_KEY_CURRENCY                   = "prefCurrency";
  public static final String       PREFS_KEY_IMPORT_EXPORT              = "prefImportExport";
  public static final String       PREFS_KEY_EMAIL                      = "prefExportMail";
  public static final String       PREFS_KEY_EMAIL_ENABLE               = "prefExportMailEnable";
  public static final String       PREFS_KEY_EXPORT_HIDE_WAGE           = "prefExportHideWage";
  public static final String       PREFS_KEY_HIDE_WAGE                  = "prefHideWage";
  public static final String       PREFS_KEY_CHANGELOG                  = "prefChangelog";
  public static final String       PREFS_KEY_VERSION                    = "prefVersion";
  public static final String       PREFS_KEY_DAY_ROWS_HEIGHT            = "prefDayRowsHeight";
  public static final String       PREFS_KEY_PROFILES_WEIGHT_DEPTH      = "prefWeightDepth";
  public static final String       PREFS_KEY_PROFILES_WEIGHT_CLEAR      = "prefWeightClear";
  public static final String       PREFS_KEY_DEFAULT_HOME               = "prefDefaultHome";
  public static final String       PREFS_KEY_SCROLL_TO_CURRENT_DAY      = "prefScrollToCurrentDay";



  private MyPreferenceFragment     prefFrag                       = null;
  private AppCompatDelegate        mDelegate;
  private ChangeLog changeLog = null;
  private MainApplication app = null;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    AndroidHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    changeLog = new ChangeLog(
      new ChangeLogIds(
        R.raw.changelog,
        R.string.changelog_ok_button,
        R.string.background_color,
        R.string.changelog_title,
        R.string.changelog_full_title,
        R.string.changelog_show_full), this);

    app = (MainApplication)getApplicationContext();
    prefFrag.findPreference(PREFS_KEY_EMAIL).setEnabled(app.isExportMailEnabled());
    prefFrag.findPreference(PREFS_KEY_EMAIL_ENABLE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_PROFILES_WEIGHT_CLEAR).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_VERSION).setTitle(
      getResources().getString(R.string.app_name));
    try {
      prefFrag.findPreference(PREFS_KEY_VERSION).setSummary(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      prefFrag.findPreference(PREFS_KEY_VERSION).setSummary(e.getMessage());
    }
    prefFrag.findPreference(PREFS_KEY_CHANGELOG).setOnPreferenceClickListener(this);

  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    AndroidHelper.closeAnimation(this);
  }

  private AppCompatDelegate getDelegate() {
    if (mDelegate == null) {
      mDelegate = AppCompatDelegate.create(this, null);
    }
    return mDelegate;
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
    if (preference.equals(prefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT))) {
      startActivity(new Intent(getApplicationContext(), SettingsImportExportActivity.class));
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_EMAIL_ENABLE))) {
      Preference p = prefFrag.findPreference(PREFS_KEY_EMAIL);
      prefFrag.findPreference(PREFS_KEY_EMAIL).setEnabled(!p.isEnabled());
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_CHANGELOG))) {
      changeLog.getFullLogDialog().show();
    } else if (preference.equals(prefFrag.findPreference(PREFS_KEY_PROFILES_WEIGHT_CLEAR))) {
      app.getProfilesFactory().resetProfilesLearningWeight();
      AndroidHelper.toast(this, R.string.profiles_weight_reseted);
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
