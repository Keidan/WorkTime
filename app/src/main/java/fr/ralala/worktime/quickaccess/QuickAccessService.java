package fr.ralala.worktime.quickaccess;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;

import fr.ralala.worktime.MainApplication;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the quick access
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class QuickAccessService extends Service {
  private MainApplication app = null;
  private Timer timer = null;
  private QuickAccessServiceTask task = null;

  @Override
  public void onCreate() {
    app = MainApplication.getApp(this);
    // cancel if already existed
    if (timer != null) {
      timer.cancel();
    } else {
      // recreate new
      timer = new Timer();
    }
    // schedule task
    timer.scheduleAtFixedRate(task = new QuickAccessServiceTask(app), 0, 1000);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (timer != null) {
      timer.cancel();
      timer.purge();
      timer = null;
      task = null;
    }
  }

  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }
}
