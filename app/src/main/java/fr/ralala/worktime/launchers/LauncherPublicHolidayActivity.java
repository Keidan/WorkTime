package fr.ralala.worktime.launchers;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.worktime.ui.activities.PublicHolidayActivity;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Launcher used with PublicHoliday activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class LauncherPublicHolidayActivity {
  public static final String PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME = "PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME_name";
  private final AppCompatActivity mActivity;
  private ActivityResultLauncher<Intent> mActivityResultLauncher;
  private LauncherCallback mCallback;

  public LauncherPublicHolidayActivity(AppCompatActivity act) {
    mActivity = act;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity(LauncherCallback callback, final String name) {
    mCallback = callback;
    Intent intent = new Intent(mActivity, PublicHolidayActivity.class);
    intent.putExtra(PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME, name);
    mActivityResultLauncher.launch(intent);
  }

  /**
   * Registers result launcher for the activity for line update.
   */
  private void register() {
    mActivityResultLauncher = mActivity.registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
          mCallback.onLauncherResult(result);
        }
      });
  }
}