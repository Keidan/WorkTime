package fr.ralala.worktime.services;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import java.util.Timer;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;

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
  private MainApplication mApp = null;
  private Timer mTimer = null;

  /**
   * Called when the service is created.
   */
  @Override
  public void onCreate() {
    mApp = MainApplication.getInstance();
    // cancel if already existed
    if (mTimer != null) {
      mTimer.cancel();
    } else {
      // recreate new
      mTimer = new Timer();
    }
    WorkTimeDay lastQuickAccessBreak = mApp.getLastQuickAccessBreak();
    if(lastQuickAccessBreak != null) {
      WorkTimeDay dif = WorkTimeDay.now();
      dif.delTime(lastQuickAccessBreak);
      DayEntry de = mApp.getDaysFactory().getCurrentDay();
      de.getAdditionalBreak().addTime(dif);
    }
    // schedule task
    mTimer.scheduleAtFixedRate(new QuickAccessServiceTask(mApp), 0, 1000);
  }

  /**
   * Called when the service is destroyed.
   */
  @Override
  public void onDestroy() {
    super.onDestroy();
    mApp.setLastQuickAccessBreak(WorkTimeDay.now());
    if (mTimer != null) {
      mTimer.cancel();
      mTimer.purge();
      mTimer = null;
    }
  }

  /**
   * Called when the service is binded.
   * @param intent Not used.
   * @return null
   */
  @Override
  public IBinder onBind(final Intent intent) {
    return null;
  }
}
