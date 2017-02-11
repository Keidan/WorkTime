package fr.ralala.worktime.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;


import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;

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
  public static final String       PREFS_KEY_WORKTIME_BY_DAY      = "prefWorkTimeByDay";
  public static final String       PREFS_KEY_AMOUNT_BY_HOUR       = "prefAmountByHour";
  public static final String       PREFS_KEY_CURRENCY             = "prefCurrency";
  public static final String       PREFS_KEY_IMPORT_EXPORT        = "prefImportExport";
  public static final String       PREFS_KEY_EMAIL                = "prefExportMail";
  public static final String       PREFS_KEY_EMAIL_ENABLE         = "prefExportMailEnable";
  public static final String       PREFS_KEY_EXPORT_HIDE_WAGE     = "prefExportHideWage";
  public static final String       PREFS_KEY_HIDE_WAGE            = "prefHideWage";

  private MyPreferenceFragment     prefFrag                       = null;
  private AppCompatDelegate        mDelegate;

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    prefFrag = new MyPreferenceFragment();
    getFragmentManager().beginTransaction()
      .replace(android.R.id.content, prefFrag).commit();
    getFragmentManager().executePendingTransactions();
    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);

    MainApplication app = (MainApplication)getApplicationContext();
    prefFrag.findPreference(PREFS_KEY_EMAIL).setEnabled(app.isExportMailEnabled());
    prefFrag.findPreference(PREFS_KEY_EMAIL_ENABLE).setOnPreferenceClickListener(this);
    prefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT).setOnPreferenceClickListener(this);
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
