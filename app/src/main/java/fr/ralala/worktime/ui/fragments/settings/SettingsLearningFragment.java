package fr.ralala.worktime.ui.fragments.settings;

import android.os.Bundle;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.ProfileEntry;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings (learning part)
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsLearningFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
  public static final String PREFS_DEF_VAL_PROFILES_WEIGHT_DEPTH = "5";
  public static final String PREFS_KEY_PROFILES_WEIGHT_CLEAR = "prefWeightClear";
  public static final String PREFS_KEY_PROFILES_WEIGHT_DEPTH = "prefWeightDepth";
  public static final String PREFS_KEY_PROFILES_LIST = "prefWeightList";
  private final MainApplication mApp;
  private Preference mPrefWeightClear;
  private Preference mPrefWeightList;
  private final AppCompatActivity mActivity;

  public SettingsLearningFragment(AppCompatActivity act) {
    mActivity = act;
    mApp = (MainApplication) act.getApplication();
  }

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
    setPreferencesFromResource(R.xml.preferences_learning, rootKey);
    mPrefWeightClear = findPreference(PREFS_KEY_PROFILES_WEIGHT_CLEAR);
    mPrefWeightList = findPreference(PREFS_KEY_PROFILES_LIST);

    mPrefWeightClear.setOnPreferenceClickListener(this);
    mPrefWeightList.setOnPreferenceClickListener(this);
  }

  /**
   * Called when a preference has been clicked.
   *
   * @param preference The preference that was clicked
   * @return {@code true} if the click was handled
   */
  @Override
  public boolean onPreferenceClick(Preference preference) {
    if (preference.equals(mPrefWeightClear)) {
      AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
      builder.setTitle(getString(R.string.reset_profile));
      builder.setIcon(android.R.drawable.ic_dialog_alert);
      builder.setCancelable(true);
      builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> {
        dialog.dismiss();
        mApp.getProfilesFactory().resetProfilesLearningWeight();
        UIHelper.toast(mActivity, R.string.profiles_weight_reset);
      });
      builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss());
      builder.setMessage(R.string.confirm_reset_profile);
      builder.show();
    } else if (preference.equals(mPrefWeightList)) {
      AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
      builder.setTitle(getString(R.string.pref_summary_learning_list));
      builder.setIcon(android.R.drawable.ic_dialog_info);
      builder.setCancelable(true);
      List<String> items = new ArrayList<>();
      for (ProfileEntry profile : mApp.getProfilesFactory().list()) {
        String text = String.format(Locale.US, "%02d", profile.getLearningWeight()) + " - " + profile.getName();
        items.add(text);
      }
      final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(mActivity, android.R.layout.select_dialog_item, items);
      builder.setPositiveButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss());
      builder.setAdapter(arrayAdapter, (dialog, which) -> dialog.dismiss());
      builder.show();
    }
    return false;
  }
}
