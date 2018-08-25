package fr.ralala.worktime.ui.quickaccess;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat;

import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.MainActivity;

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
  private NotificationCompat.Builder mNfyBuilder = null;
  private int mNfyId;
  private Notification mNfy = null;
  private Context mContext;
  private boolean mVisible = false;

  /**
   * Creates the quick access notification object.
   * @param c The Android context.
   * @param id The notification id.
   */
  public QuickAccessNotification(final Context c, final int id) {
    mContext = c;
    mNfyId = id;
  }

  /**
   * Removes the notification.
   * @param c The Android contexte.
   */
  public void remove(final Context c) {
    NotificationManagerCompat.from(c).cancel(mNfyId);
    mVisible = false;
  }

  /**
   * Tests if the notification is visible.
   * @return boolean
   */
  public boolean isVisible() {
    return mVisible;
  }

  /**
   * Updates the current notification.
   * @param text The new text to display.
   * @param pause True if the pause icon should be used.
   */
  public void update(String text, boolean pause){
    if(mNfyBuilder == null)
      set(text);
    if(text != null)
      mNfyBuilder.setContentText(text)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
    android.support.v4.app.NotificationCompat.Action action = mNfyBuilder.mActions.get(0);
    if  (pause) {
      action.icon = R.mipmap.ic_play;
      action.title = mContext.getString(R.string.bt_start);
      mNfy = mNfyBuilder.build();
    } else {
      action.icon = R.mipmap.ic_pause;
      action.title = mContext.getString(R.string.bt_pause);
      mNfy = mNfyBuilder.build();
    }
    NotificationManagerCompat.from(mContext).notify(mNfyId, mNfy);
  }

  /**
   * Sets the system notification.
   * @param text The init text.
   */
  public void set(String text){
    mNfyBuilder = new NotificationCompat.Builder(mContext, "MyChannelId_0");
    mNfyBuilder.setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle(mContext.getString(R.string.app_name));
    mNfyBuilder.setContentText(text);
    Intent pauseIntent = new Intent(mContext, QuickAccessNotificationReceiver.class);
    pauseIntent.setAction(QuickAccessNotificationReceiver.KEY_PAUSE);
    PendingIntent pausePendingIntent = PendingIntent.getBroadcast(mContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    mNfyBuilder.addAction(new NotificationCompat.Action(R.mipmap.ic_pause, mContext.getString(R.string.bt_pause), pausePendingIntent));
    Intent contentIntent = new Intent(mContext, MainActivity.class);
    contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    PendingIntent contentPendingIntent = PendingIntent.getActivity(mContext, 0, contentIntent, 0);
    mNfyBuilder.setContentIntent(contentPendingIntent);
    mNfy = mNfyBuilder.build();
    NotificationManagerCompat.from(mContext).notify(mNfyId, mNfy);
    mVisible = true;
  }
}
