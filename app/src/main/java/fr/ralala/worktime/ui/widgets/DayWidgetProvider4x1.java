package fr.ralala.worktime.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.DayActivity;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Day widget 4x1
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DayWidgetProvider4x1 extends AppWidgetProvider {
  public static final String ACTION_FROM_WIDGET = "ACTION_FROM_WIDGET_4x1";

  /**
   * Called when the widget receive an update request.
   * @param context The Android context.
   * @param appWidgetManager The applocation widget manager.
   * @param appWidgetIds The widget ids.
   */
  @Override
  public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    MainApplication app = MainApplication.getInstance();
    if(!app.openSql(context)) {
      UIHelper.toast(context, R.string.error_widget_sql);
      Log.e(getClass().getSimpleName(), "Widger error SQL");
      return ;
    }

    DayEntry de = app.getDaysFactory().getCurrentDay();
    if(de == null) return;
    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_day4x1);

    for (int idx : appWidgetIds) {
      Intent intent = new Intent(context, DayActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction(ACTION_FROM_WIDGET);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

      remoteViews.setOnClickPendingIntent(R.id.icon, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.rl, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.ll1, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.ll2, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvDate, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvMorning, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvBreak, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvAfternoon, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvWorkTime, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvLblMorning, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvLblBreak, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvLblAfternoon, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvLblWorkTime, pendingIntent);

      remoteViews.setTextViewText(R.id.tvDate, de.getDay().dateString());
      remoteViews.setTextViewText(R.id.tvMorning, de.getStartMorning().timeString() + " - " + de.getEndMorning().timeString());
      if(de.getAdditionalBreak().timeString().equals("00:00"))
        remoteViews.setTextViewText(R.id.tvBreak, de.getPause().timeString());
      else
        remoteViews.setTextViewText(R.id.tvBreak, de.getPause().timeString() + " (" + de.getAdditionalBreak().timeString(true) + ")");
      remoteViews.setTextViewText(R.id.tvAfternoon, de.getStartAfternoon().timeString() + " - " + de.getEndAfternoon().timeString());
      if(de.getWorkTime().timeString().equals("00:00") || de.getOverTime().timeString().equals("00:00"))
        remoteViews.setTextViewText(R.id.tvWorkTime, de.getWorkTime().timeString());
      else
        remoteViews.setTextViewText(R.id.tvWorkTime, de.getWorkTime().timeString() + " (" + de.getOverTime().timeString(true) + ")");
      appWidgetManager.updateAppWidget(idx, remoteViews);
    }
  }
}
