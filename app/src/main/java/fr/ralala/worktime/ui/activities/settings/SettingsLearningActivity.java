package fr.ralala.worktime.ui.activities.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings (learning part)
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SettingsLearningActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener{
  public static final String       PREFS_DEFVAL_PROFILES_WEIGHT_DEPTH               = "5";
  public static final String       PREFS_KEY_PROFILES_WEIGHT_CLEAR                  = "prefWeightClear";
  public static final String       PREFS_KEY_PROFILES_WEIGHT_DEPTH                  = "prefWeightDepth";
  public static final String       PREFS_KEY_PROFILES_LIST                          = "prefWeightList";

  private MyPreferenceFragment mPrefFrag = null;
  private MainApplication mApp = null;

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

    mApp = (MainApplication)getApplicationContext();

    mPrefFrag.findPreference(PREFS_KEY_PROFILES_WEIGHT_CLEAR).setOnPreferenceClickListener(this);
    mPrefFrag.findPreference(PREFS_KEY_PROFILES_LIST).setOnPreferenceClickListener(this);
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
    if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_PROFILES_WEIGHT_CLEAR))) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.reset_profile));
      builder.setIcon(android.R.drawable.ic_dialog_alert);
      builder.setCancelable(true);
      builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
        dialog.dismiss();
        mApp.getProfilesFactory().resetProfilesLearningWeight();
        UIHelper.toast(this, R.string.profiles_weight_reseted);
      });
      builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
      builder.setMessage(R.string.confirm_reset_profile);
      builder.show();
    } else if (preference.equals(mPrefFrag.findPreference(PREFS_KEY_PROFILES_LIST))) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(getString(R.string.pref_summary_learning_list));
      builder.setIcon(android.R.drawable.ic_dialog_info);
      builder.setCancelable(true);
      List<String> items = new ArrayList<>();
      for(DayEntry profile : mApp.getProfilesFactory().list()) {
        String text = String.format(Locale.US,"%02d", profile.getLearningWeight()) + " - " + profile.getName();
        items.add(text);
      }
      final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item, items);
      builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
      builder.setAdapter(arrayAdapter, (dialog, which) -> dialog.dismiss());
      builder.show();
    }
    return true;
  }

  public static class MyPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setRetainInstance(true);
      addPreferencesFromResource(R.xml.preferences_learning);
    }
  }
}
