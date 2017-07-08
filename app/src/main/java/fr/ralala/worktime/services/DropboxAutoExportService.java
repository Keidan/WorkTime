package fr.ralala.worktime.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;


import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.dropbox.DropboxImportExport;

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

  @Override
  public void onCreate() {
    app = MainApplication.getApp(this);
    if(!app.openSql(this)) {
      stopSelf();
    }
    if(app.isTablesChanges()) {
      if (!app.getDropboxImportExport().exportDatabase(this, false, this)) {
        stopSelf();
      }
    } else {
      stopSelf();
    }
  }

  @Override
  public void onDestroy() {
    if(app != null)
      app.getSql().close();
    super.onDestroy();
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
