package fr.ralala.worktime.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.preference.PreferenceManager;

import java.util.Calendar;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.dropbox.DropboxImportExport;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Export the db to dropbox in background.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class AutoExportService extends Service implements DropboxImportExport.DropboxUploaded, DropboxImportExport.DropboxDownloaded {
  public static final boolean DEF_VAL_NEED_UPDATE = false;
  private ApplicationCtx mApp = null;
  public static final String KEY_NEED_UPDATE = "dropboxKeyNeedUpdate";

  /**
   * Called when the service is created.
   */
  @Override
  public void onCreate() {
    mApp = (ApplicationCtx) getApplication();
    if (!mApp.openSql(this))
      stopSelf();
    if (mApp.isTablesChanges())
      tableChanged();
    else
      tableNotChanged();
  }

  private void tableNotChanged() {
    boolean export = isExportable();
    String text = "";
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mApp);
    boolean needUpdate = prefs.getBoolean(KEY_NEED_UPDATE, DEF_VAL_NEED_UPDATE);
    if (export && needUpdate) {
      text = "No changes have been detected but the day is valid for pending export.";
      ApplicationCtx.addLog(this, "tableNotChanged", text);
      Log.e(getClass().getSimpleName(), text);
      exportDatabase();
      return;
    }
    if (!needUpdate)
      text = "No change detected.";
    if ((!export && needUpdate))
      text = "Changes detected but the current day does not allow export.";

    ApplicationCtx.addLog(this, "tableNotChanged", text);
    Log.e(getClass().getSimpleName(), text);
    stopSelf();
  }

  private void tableChanged() {
    boolean export = isExportable();
    String text = "Exportation required and the day" + (export ? " " : " not") + " match.";
    ApplicationCtx.addLog(this, "tableChanged", text);
    Log.e(getClass().getSimpleName(), text);
    if (export) {
      exportDatabase();
    } else {
      setNeedUpdate(mApp, true);
      stopSelf();
    }
  }

  /**
   * Export database.
   */
  private void exportDatabase() {
    setNeedUpdate(mApp, false);
    if (mApp.getLastExportType().equals(ApplicationCtx.PREFS_VAL_LAST_EXPORT_DROPBOX) &&
      !mApp.getDropboxImportExport().exportDatabase(this, this)) {
      String text = "Export failed.";
      ApplicationCtx.addLog(this, "exportDatabase", text);
      Log.e(getClass().getSimpleName(), text);
      setNeedUpdate(mApp, true);
      stopSelf();
    }
  }

  /**
   * Changes the value of the needUpdate field.
   *
   * @param app Application context.
   * @param b   needUpdate state.
   */
  public static void setNeedUpdate(ApplicationCtx app, boolean b) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
    SharedPreferences.Editor ed = prefs.edit();
    ed.putBoolean(KEY_NEED_UPDATE, b);
    ed.apply();
    app.getSql().settingsSave();
  }

  /**
   * Tests if the database is exportable.
   *
   * @return boolean
   */
  private boolean isExportable() {
    Calendar c = WorkTimeDay.now().toCalendar();
    int p = mApp.getExportAutoSavePeriodicity();
    return ((p == 0) ||
      (p == 1 && c.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) ||
      (p == 2 && c.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) ||
      (p == 3 && c.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) ||
      (p == 4 && c.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) ||
      (p == 5 && c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) ||
      (p == 6 && c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) ||
      (p == 7 && c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY));
  }

  /**
   * Called when the service is destroyed.
   */
  @Override
  public void onDestroy() {
    boolean cond = false;
    if (mApp != null) {
      mApp.getSql().close();
      cond = (mApp.getLastWidgetOpen() != 0L || mApp.getLifeCycle().isActivityVisible());
    }
    super.onDestroy();
    if (cond)
      return;
    String text = "killProcess";
    ApplicationCtx.addLog(this, "Service.onDestroy", text);
    Log.e(getClass().getSimpleName(), text);
    Process.killProcess(android.os.Process.myPid());
  }

  /**
   * Called when the service is binded.
   *
   * @param intent Not used.
   * @return null
   */
  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }


  /**
   * File uploaded to dropbox.
   *
   * @param error True on error.
   */
  @Override
  public void dropboxUploaded(final boolean error) {
    stopSelf();
  }

  /**
   * File downloaded from dropbox.
   *
   * @param error True on error.
   */
  @Override
  public void dropboxDownloaded(final boolean error) {
    stopSelf();
  }
}
