package fr.ralala.worktime.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;
import android.util.SparseArray;


import java.text.DateFormatSymbols;

import fr.ralala.worktime.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class AndroidHelper {

  public static final String EXTRA_RESTART = "EXTRA_RESTART";

  /**
   * Tests if the SparseArray contains the key.
   * @param array The sparse array.
   * @param key The key to search.
   * @param <T> The sparse array type.
   * @return True if contains.
   */
  public static <T> boolean containsKey(SparseArray<T> array, Integer key) {
    for(int i = 0; i < array.size(); i++) {
      Integer k = array.keyAt(i);
      if (k.equals(key))
        return true;
    }
    return false;
  }

  /**
   * Updates a widget.
   * @param context The Android context.
   * @param widgetClass The widget class.
   */
  public static void updateWidget(final Context context, final Class<?> widgetClass) {
    int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, widgetClass));
    if(ids == null || ids.length == 0) return;
    Intent intent = new Intent(context, widgetClass);
    intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    context.sendBroadcast(intent);
  }

  /**
   * Test if a specific service is in running state.
   * @param context The Android context.
   * @param serviceClass The service class
   * @return boolean
   */
  public static boolean isServiceRunning(final Context context,
                                         final Class<?> serviceClass) {
    if(context == null) return false;
    final ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    if(manager != null) {
      for (final ActivityManager.RunningServiceInfo service : manager
        .getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.getName().equals(service.service.getClassName())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Kill a specific service (if running)
   * @param context The Android context.
   * @param serviceClass The service class.
   */
  public static void killServiceIfRunning(final Context context, final Class<?> serviceClass) {
    if(isServiceRunning(context, serviceClass))
      context.stopService(new Intent(context, serviceClass));
  }

  /**
   * Restarts the current application.
   * @param c The Android context.
   * @param string The string to display before the restart.
   */
  public static void restartApplication(final Context c, final int string) {
    Intent startActivity = c.getApplicationContext().getPackageManager()
        .getLaunchIntentForPackage(c.getApplicationContext().getPackageName());
    assert startActivity != null;
    startActivity.putExtra(EXTRA_RESTART, c.getString(string));
    int mPendingIntentId = 123456;
    PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId, startActivity, PendingIntent.FLAG_CANCEL_CURRENT);
    AlarmManager mgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    if(mgr != null) {
      mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, mPendingIntent);
      Runtime.getRuntime().exit(0);
    }
  }

  /**
   * Sent an email using the Android Intent.
   * @param activity The activity context.
   * @param mailto The email to address.
   * @param attachment The email attachment.
   * @param subject The email subject.
   * @param body The email body.
   * @param senderMsg The sender email address.
   */
  public static void sentMailTo(final Activity activity, String mailto, Uri attachment, String subject, String body, String senderMsg) {
    Intent emailIntent = new Intent(Intent.ACTION_SEND);
    emailIntent .setType("application/excel");
    String to[] = {mailto};
    emailIntent .putExtra(Intent.EXTRA_EMAIL, to);
    emailIntent .putExtra(Intent.EXTRA_STREAM, attachment);
    emailIntent .putExtra(Intent.EXTRA_SUBJECT, subject == null ? "" : subject);
    emailIntent.putExtra(Intent.EXTRA_TEXT, body == null ? "" : body);
    activity.startActivity(Intent.createChooser(emailIntent , senderMsg));
  }

  /**
   * Returns the month in string format.
   * @param month The current month (0-11)
   * @return String.
   */
  public static String getMonthString(int month) {
    String[] months = new DateFormatSymbols().getMonths();
    return months[month].substring(0, 1).toUpperCase() + months[month].substring(1);
  }

  /**
   * Tells Android that the system should start a short vibration (100ms).
   * @param c The Android context.
   */
  public static void vibrate(final Context c) {
    Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
    if(v != null)
      v.vibrate(100);
  }

}
