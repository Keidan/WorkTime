package fr.ralala.worktime.ui.quickaccess;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.services.QuickAccessService;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the quick access service messages
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class QuickAccessNotificationReceiver extends BroadcastReceiver {

  public static final String KEY_PAUSE = "fr.ralala.worktime.services.PAUSE";

  /**
   * Called when the service receive a notification from the activity
   * @param context The Android context.
   * @param intent The notification intent.
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    MainApplication app = MainApplication.getInstance();
    if(intent.getAction() != null && intent.getAction().equals(KEY_PAUSE)) {
      if(!app.isQuickAccessPause())
        context.stopService(new Intent(context, QuickAccessService.class));
      else
        context.startService(new Intent(context, QuickAccessService.class));
      app.setQuickAccessPause(!app.isQuickAccessPause());
      app.getQuickAccessNotification().update(null, app.isQuickAccessPause());
    }
  }

}