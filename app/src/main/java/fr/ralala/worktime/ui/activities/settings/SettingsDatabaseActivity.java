package fr.ralala.worktime.ui.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
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
 * Management of the application settings (database import/export part)
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsDatabaseActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{
  public static final String       PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE             = "false";
  public static final String       PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY = "0";
  public static final String       PREFS_KEY_IMPORT_EXPORT                          = "prefImportExport";
  public static final String       PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE                = "prefImportExportAutoSave";
  public static final String       PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY    = "prefImportExportAutoSavePeriodicity";
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

    mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY).setEnabled(((CheckBoxPreference)mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE)).isChecked());
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
    if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT))) {
      startActivity(new Intent(getApplicationContext(), SettingsImportExportActivity.class));
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE))) {
      mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY).setEnabled(
          ((CheckBoxPreference)mPrefFrag.findPreference(PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE)).isChecked());
    }
    return true;
  }

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences_database);
    }
  }

}
