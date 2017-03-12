package fr.ralala.worktime.quickaccess;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.activities.MainActivity;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the quick access notification
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class QuickAccessNotification {
  private NotificationCompat.Builder nfyBuilder = null;
  private int nfyId = 1;
  private Notification nfy = null;
  private Context c = null;
  private PendingIntent pausePendingIntent = null;

  public QuickAccessNotification(final Context c, final int id) {
    this.c = c;
    this.nfyId = id;
  }

  public void remove(final Context c) {
    NotificationManagerCompat.from(c).cancel(nfyId);
  }

  public void update(String text, boolean pause){
    if(nfyBuilder == null)
      set(text);
    if(text != null)
      nfyBuilder.setContentText(text)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    android.support.v4.app.NotificationCompat.Action action = nfyBuilder.mActions.get(0);
    if  (pause) {
      action.icon = R.mipmap.ic_play;
      action.title = c.getString(R.string.bt_start);
      nfy = nfyBuilder.build();
    } else {
      action.icon = R.mipmap.ic_pause;
      action.title = c.getString(R.string.bt_pause);
      nfy = nfyBuilder.build();
    }
    NotificationManagerCompat.from(c).notify(nfyId, nfy);
  }

  public void set(String text){
    nfyBuilder = new NotificationCompat.Builder(c);
    nfyBuilder.setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(c.getString(R.string.app_name));
    nfyBuilder.setContentText(text);
    Intent pauseIntent = new Intent(c, QuickAccessNotificationReceiver.class);
    pauseIntent.setAction(QuickAccessNotificationReceiver.KEY_PAUSE);
    pausePendingIntent = PendingIntent.getBroadcast(c, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    nfyBuilder.addAction(new NotificationCompat.Action(R.mipmap.ic_pause, c.getString(R.string.bt_pause), pausePendingIntent));
    Intent contentIntent = new Intent(c, MainActivity.class);
    contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent contentPendingIntent = PendingIntent.getActivity(c, 0, contentIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
    nfyBuilder.setContentIntent(contentPendingIntent);
    nfy = nfyBuilder.build();
    NotificationManagerCompat.from(c).notify(nfyId, nfy);
  }
}
