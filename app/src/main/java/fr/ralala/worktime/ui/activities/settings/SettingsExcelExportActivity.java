package fr.ralala.worktime.ui.activities.settings;

import android.os.Bundle;
import android.preference.Preference;
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
 * Management of the application settings (Excel export part)
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsExcelExportActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{
  public static final String       PREFS_DEFVAL_EMAIL                               = "";
  public static final String       PREFS_DEFVAL_EMAIL_ENABLE                        = "true";
  public static final String       PREFS_DEFVAL_EXPORT_HIDE_WAGE                    = "false";
  public static final String       PREFS_KEY_EXPORT_HIDE_WAGE                       = "prefExportHideWage";
  public static final String       PREFS_KEY_EMAIL                                  = "prefExportMail";
  public static final String       PREFS_KEY_EMAIL_ENABLE                           = "prefExportMailEnable";

  private MyPreferenceFragment mPrefFrag = null;

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

    MainApplication app = (MainApplication)getApplicationContext();

    mPrefFrag.findPreference(PREFS_KEY_EMAIL).setEnabled(app.isExportMailEnabled());
    mPrefFrag.findPreference(PREFS_KEY_EMAIL_ENABLE).setOnPreferenceClickListener(this);
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
    if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_EMAIL_ENABLE))) {
      Preference p = mPrefFrag.findPreference(PREFS_KEY_EMAIL);
      mPrefFrag.findPreference(PREFS_KEY_EMAIL).setEnabled(!p.isEnabled());
    }
    return true;
  }

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences_excel_export);
    }
  }
}
