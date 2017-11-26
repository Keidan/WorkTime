package fr.ralala.worktime.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Vibrator;


import java.text.DateFormatSymbols;

import fr.ralala.worktime.R;
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

  public static void updateWidget(final Context context, final Class<?> widgetClass) {
    int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, widgetClass));
    if(ids == null || ids.length == 0) return;
    Intent intent = new Intent(context, widgetClass);
    intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
    context.sendBroadcast(intent);
  }

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

  public static void killServiceIfRunning(final Context context, final Class<?> serviceClass) {
    if(isServiceRunning(context, serviceClass))
      context.stopService(new Intent(context, serviceClass));
  }

  public static void restartApplication(final Context c, final String string) {
    if(string != null)
      UIHelper.toast(c, string);
    Intent i = c.getApplicationContext().getPackageManager()
      .getLaunchIntentForPackage(c.getApplicationContext().getPackageName() );
    if(i != null) {
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }
    c.startActivity(i);
    Runtime.getRuntime().exit(0);
  }

  public static void restartApplication(final Context c, final int string_id) {
    restartApplication(c, string_id == -1 ? null : c.getString(string_id));
  }



  public static void openAnimation(final Activity a) {
    a.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
  }
  public static void closeAnimation(final Activity a) {
    a.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
  }

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

  public static String getMonthString(int month) {
    String[] months = new DateFormatSymbols().getMonths();
    return months[month].substring(0, 1).toUpperCase() + months[month].substring(1);
  }

  public static void vibrate(final Context c) {
    Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
    if(v != null)
      v.vibrate(100);
  }

}
