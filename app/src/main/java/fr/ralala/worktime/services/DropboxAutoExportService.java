package fr.ralala.worktime.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Process;
import android.preference.PreferenceManager;
import android.util.Log;


import java.util.Calendar;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.activities.DayActivity;
import fr.ralala.worktime.activities.MainActivity;
import fr.ralala.worktime.dropbox.DropboxImportExport;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Export the db to dropbox in background.
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DropboxAutoExportService extends Service implements DropboxImportExport.DropboxUploaded, DropboxImportExport.DropboxDownloaded {
  private MainApplication app = null;
  public static final String KEY_NEED_UPDATE = "dropboxKeyNeedUpdate";
  public static final String DEFVAL_NEED_UPDATE = "false";

  @Override
  public void onCreate() {
    app = MainApplication.getApp(this);
    if(!app.openSql(this)) {
      stopSelf();
    }

    if(app.isTablesChanges()) {
      boolean export = isExportable();
      Log.e(getClass().getSimpleName(), "Exportation required and the day" + (export ? " " : " not") + " match.");
      if(export) {
        setNeedUpdate(app, false);
        if (!app.getDropboxImportExport().exportDatabase(this, false, this)) {
          Log.e(getClass().getSimpleName(), "Export failed.");
          setNeedUpdate(app, true);
          stopSelf();
        }
      } else {
        setNeedUpdate(app, true);
        stopSelf();
      }
    } else {
      boolean export = isExportable();
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      boolean needUpdate = prefs.getBoolean(KEY_NEED_UPDATE, DEFVAL_NEED_UPDATE.equals("true"));
      if(export && needUpdate) {
        Log.e(getClass().getSimpleName(), "No changes have been detected but the day is valid for pending export.");
        setNeedUpdate(app, false);
        if (!app.getDropboxImportExport().exportDatabase(this, false, this)) {
          Log.e(getClass().getSimpleName(), "Export failed.");
          setNeedUpdate(app, true);
          stopSelf();
        }
        return;
      }
      if(!needUpdate)
        Log.e(getClass().getSimpleName(), "No change detected.");
      if((!export && needUpdate))
        Log.e(getClass().getSimpleName(), "Changes detected but the current day does not allow export.");
      stopSelf();
    }
  }

  public static void setNeedUpdate(MainApplication app, boolean b) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
    SharedPreferences.Editor ed = prefs.edit();
    ed.putBoolean(KEY_NEED_UPDATE, b);
    ed.apply();
    app.getSql().settingsSave();
    if(!b)
      app.disableDbUpdateFromOnloadSettings();
  }

  private boolean isExportable() {
    Calendar c = WorkTimeDay.now().toCalendar();
    int p = app.getExportAutoSavePeriodicity();
    return ((p == 0) ||
      (p == 1 && c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) ||
      (p == 2 && c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) ||
      (p == 3 && c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) ||
      (p == 4 && c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) ||
      (p == 5 && c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) ||
      (p == 6 && c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
      (p == 7 && c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY));
  }

  @Override
  public void onDestroy() {
    boolean cond = false;
    if(app != null) {
      app.getSql().close();
      cond = (app.getLastWidgetOpen() != 0L || AndroidHelper.isActivityRunning(this, DayActivity.class) || AndroidHelper.isActivityRunning(this, MainActivity.class));
    }
    super.onDestroy();
    if(cond)
      return;
    Log.e("fr.ralala.", "killProcess");
    Process.killProcess(android.os.Process.myPid());
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }


  @Override
  public void dropboxUploaded(final boolean error) {
    stopSelf();
  }

  @Override
  public void dropboxDownloaded(final boolean error) {
    stopSelf();
  }
}
