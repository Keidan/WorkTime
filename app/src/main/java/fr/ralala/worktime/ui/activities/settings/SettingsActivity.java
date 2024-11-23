package fr.ralala.worktime.ui.activities.settings;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.fragments.settings.SettingsFragment;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the application settings
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SettingsActivity extends AppCompatActivity {
  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState Bundle
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UIHelper.openAnimation(this);
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_settings);

    getSupportFragmentManager()
      .beginTransaction()
      .replace(R.id.settings_container, new SettingsFragment(this))
      .commit();

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
  }

  /**
   * Called when the options item is clicked (home).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      UIHelper.closeAnimation(this);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    ((MainApplication) getApplication()).getSql().settingsLoad(null);
  }

  /**
   * Called when the activity is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    ((MainApplication) getApplication()).getSql().settingsSave();
  }
}
