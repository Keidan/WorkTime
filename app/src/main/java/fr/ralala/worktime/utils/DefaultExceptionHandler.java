package fr.ralala.worktime.utils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;

import androidx.annotation.NonNull;

import java.lang.Thread.UncaughtExceptionHandler;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.MainActivity;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Default exception handler
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {
  private final Activity mActivity;

  private DefaultExceptionHandler(Activity activity) {
    mActivity = activity;
  }

  @Override
  public void uncaughtException(@NonNull Thread thread, @NonNull Throwable ex) {
    Log.error(mActivity, "uncaughtException", mActivity.getString(R.string.error) + ex.getMessage(), ex);
    ApplicationCtx app = (ApplicationCtx) mActivity.getApplication();
    try {
      Intent intent = new Intent(mActivity, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        | Intent.FLAG_ACTIVITY_NEW_TASK);

      PendingIntent pendingIntent = PendingIntent.getActivity(
        app.getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

      //Following code will restart your application after 2 seconds
      AlarmManager mgr = (AlarmManager) app.getBaseContext()
        .getSystemService(Context.ALARM_SERVICE);
      if (mgr != null)
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 800,
          pendingIntent);
      //This will finish your activity manually
      mActivity.finish();
      //This will stop your application and take out from it.
      Process.killProcess(Process.myPid());
    } catch (Exception e) {
      Log.error(mActivity, "uncaughtException", mActivity.getString(R.string.error) + e.getMessage(), e);
    }
  }

  /**
   * Installs the default uncaught exception handler.
   *
   * @param activity The owner activity.
   */
  public static void install(Activity activity) {
    Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(activity));
  }
}
