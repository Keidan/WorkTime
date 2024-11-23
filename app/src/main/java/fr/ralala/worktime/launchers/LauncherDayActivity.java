package fr.ralala.worktime.launchers;

import android.app.Activity;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.worktime.ui.activities.DayActivity;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Launcher used with Day activity
 * </p>
 *
 * @author Keidan
 * <p>
 * License: GPLv3
 * <p>
 * ******************************************************************************
 */
public class LauncherDayActivity {
  public static final String DAY_ACTIVITY_EXTRA_DATE = "DAY_ACTIVITY_EXTRA_DATE_date";
  public static final String DAY_ACTIVITY_EXTRA_PROFILE = "DAY_ACTIVITY_EXTRA_DATE_profile";
  private final AppCompatActivity mActivity;
  private ActivityResultLauncher<Intent> mActivityResultLauncher;
  private LauncherCallback mCallback;

  public LauncherDayActivity(AppCompatActivity act) {
    mActivity = act;
    register();
  }

  /**
   * Starts the activity.
   */
  public void startActivity(LauncherCallback callback, final String date, final boolean profile) {
    mCallback = callback;
    Intent intent = new Intent(mActivity, DayActivity.class);
    intent.putExtra(DAY_ACTIVITY_EXTRA_DATE, date);
    intent.putExtra(DAY_ACTIVITY_EXTRA_PROFILE, profile);
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
