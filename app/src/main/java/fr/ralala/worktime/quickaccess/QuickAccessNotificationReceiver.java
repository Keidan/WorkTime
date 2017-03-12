package fr.ralala.worktime.quickaccess;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;

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

  @Override
  public void onReceive(Context context, Intent intent) {
    MainApplication app = MainApplication.getApp(context);
    if(intent.getAction().equals(KEY_PAUSE)) {
      if(!app.isQuickAccessPause())
        context.stopService(new Intent(context, QuickAccessService.class));
      else
        context.startService(new Intent(context, QuickAccessService.class));
      app.setQuickAccessPause(!app.isQuickAccessPause());
      DayEntry d = app.getDaysFactory().getCurrentDay();
      WorkTimeDay w = d.getWorkTime();
      String text = context.getString(R.string.work_time) + ": ";
      text += String.format(Locale.US, "%02d:%02d:%02d", w.getHours(), w.getMinutes(), w.getSeconds());
      app.getQuickAccessNotification().update(text, app.isQuickAccessPause());
    }
  }

}