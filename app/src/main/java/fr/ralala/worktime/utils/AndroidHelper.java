package fr.ralala.worktime.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.os.Process;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.SparseArray;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.text.DateFormatSymbols;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.services.DropboxAutoExportService;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.ui.activities.FileChooserActivity;
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
   * Opens the default navigation apps using latitude and longitude.
   * @param c The Android context.
   * @param name The name to use.
   * @param location The location (latitude+longitude).
   */
  public static void openDefaultNavigationApp(Context c, String name, Location location) {
    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
        Uri.parse("geo:0,0?q=" + location.getLatitude() + "," + location.getLongitude() + " (" + name + ")"));
    c.startActivity(intent);
  }

  public interface LocationListener {
    void onLocationSuccess(Location location);
    void onLocationError(@NonNull Exception e);
  }

  /**
   * Gets the last position using FusedLocationProviderClient (gms API).
   * @param c The Android context.
   * @param li The output listener.
   * @return False on permissions error.
   */
  public static boolean getLastLocationNewMethod(Context c, final LocationListener li){
    if (ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(c, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
      FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(c);
      mFusedLocationClient.getLastLocation()
          .addOnSuccessListener(li::onLocationSuccess)
          .addOnFailureListener(li::onLocationError);
      return true;
    }
    return false;
  }

  /**
   * Tests if the SparseArray contains the key.
   * @param array The sparse array.
   * @param key The key to search.
   * @param <T> The sparse array type.
   * @return True if contains.
   */
  public static <T> boolean notContainsKey(SparseArray<T> array, Integer key) {
    for(int i = 0; i < array.size(); i++) {
      Integer k = array.keyAt(i);
      if (k.equals(key))
        return false;
    }
    return true;
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
    startActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity.putExtra(EXTRA_RESTART, c.getString(string));
    int mPendingIntentId = 123456;
    PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId, startActivity,
        startActivity.getFlags());
    AlarmManager mgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
    if(mgr != null) {
      mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
      if(Activity.class.isInstance(c))
        ActivityCompat.finishAffinity(((Activity)c));
      Process.killProcess(Process.myPid());
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

  /**
   * Export the database to dropbox.
   * @param app Main application
   * @param c Android context.
   */
  public static void exportDropbox(MainApplication app, Context c) {
    DropboxAutoExportService.setNeedUpdate(app, false);
    app.reloadDatabaseMD5();
    app.getDropboxImportExport().exportDatabase(c, true, null);
  }

  /**
   * Export the database to device.
   * @param a Activity.
   */
  public static void exportDevice(Activity a){
    final Intent i = new Intent(a.getApplicationContext(), FileChooserActivity.class);
    i.putExtra(FileChooserActivity.FILECHOOSER_TYPE_KEY, "" + FileChooserActivity.FILECHOOSER_TYPE_DIRECTORY_ONLY);
    i.putExtra(FileChooserActivity.FILECHOOSER_TITLE_KEY, a.getString(R.string.pref_title_export));
    i.putExtra(FileChooserActivity.FILECHOOSER_MESSAGE_KEY, a.getString(R.string.use_folder) + ":? ");
    i.putExtra(FileChooserActivity.FILECHOOSER_DEFAULT_DIR, Environment
        .getExternalStorageDirectory().getAbsolutePath());
    i.putExtra(FileChooserActivity.FILECHOOSER_SHOW_KEY, "" + FileChooserActivity.FILECHOOSER_SHOW_DIRECTORY_ONLY);
    a.startActivityForResult(i, FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY);
  }

  /**
   * Called when the file chooser is disposed with a result.
   * @param a Activity.
   * @param requestCode The request code.
   * @param resultCode The result code.
   * @param data The Intent data.
   * @return true if consumed.
   */
  public static boolean exportDeviceActivityResult(Activity a, final int requestCode, final int resultCode, final Intent data) {
    if (requestCode == FileChooserActivity.FILECHOOSER_SELECTION_TYPE_DIRECTORY) {
      if (resultCode == Activity.RESULT_OK) {
        String dir = data.getStringExtra(FileChooserActivity.FILECHOOSER_SELECTION_KEY);
        try {
          SqlHelper.copyDatabase(a, SqlHelper.DB_NAME, dir);
          UIHelper.toast(a, a.getString(R.string.export_success));
        } catch(Exception e) {
          UIHelper.toastLong(a, a.getString(R.string.error) + ": " + e.getMessage());
          Log.e(AndroidHelper.class.getSimpleName(), "Error: " + e.getMessage(), e);
        }
      }
      return true;
    }
    return false;
  }

}
