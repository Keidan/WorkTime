package fr.ralala.worktime.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.ui.activities.MainActivity;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Default exception handler
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DefaultExceptionHandler implements UncaughtExceptionHandler {
  private Activity mActivity;

  private DefaultExceptionHandler(Activity activity) {
    mActivity = activity;
  }

  @Override
  public void uncaughtException(Thread thread, Throwable ex) {
    Log.e(getClass().getSimpleName(), "uncaughtException: " + ex.getMessage(), ex);
    try {
      Intent intent = new Intent(mActivity, MainActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
          | Intent.FLAG_ACTIVITY_CLEAR_TASK
          | Intent.FLAG_ACTIVITY_NEW_TASK);

      PendingIntent pendingIntent = PendingIntent.getActivity(
          MainApplication.getInstance().getBaseContext(), 0, intent, intent.getFlags());

      //Following code will restart your application after 2 seconds
      AlarmManager mgr = (AlarmManager) MainApplication.getInstance().getBaseContext()
          .getSystemService(Context.ALARM_SERVICE);
      if(mgr != null)
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 800,
            pendingIntent);
      //This will finish your activity manually
      mActivity.finish();
      //This will stop your application and take out from it.
      Process.killProcess(Process.myPid());
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
  }

  /**
   * Installs the default uncaught exception handler.
   * @param activity The owner activity.
   */
  public static void install(Activity activity) {
    Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(activity));
  }
}
