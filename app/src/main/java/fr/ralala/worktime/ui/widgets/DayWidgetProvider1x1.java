package fr.ralala.worktime.ui.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.ui.activities.DayActivity;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Day widget 1x1
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DayWidgetProvider1x1 extends AppWidgetProvider {
  private static final String TIME_ZERO = "00:00";
  public static final String ACTION_FROM_WIDGET = "ACTION_FROM_WIDGET_1x1";

  /**
   * Called when the widget receive an update request.
   *
   * @param context          The Android context.
   * @param appWidgetManager The applocation widget manager.
   * @param appWidgetIds     The widget ids.
   */
  @Override
  public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    ApplicationCtx app = (ApplicationCtx) context.getApplicationContext();
    if (!app.openSql(context)) {
      String text = context.getString(R.string.error_widget_sql);
      UIHelper.toast(context, text);
      ApplicationCtx.addLog(context, "ExportFragment", text);
      Log.e(getClass().getSimpleName(), text);
      return;
    }

    DayEntry de = app.getDaysFactory().getCurrentDay(context);
    WorkTimeDay w = de == null ? new WorkTimeDay() : de.getDay();

    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_day1x1);

    for (int idx : appWidgetIds) {
      Intent intent = new Intent(context, DayActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction(ACTION_FROM_WIDGET);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);

      remoteViews.setOnClickPendingIntent(R.id.tvMonth, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvDay, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvWorkTime, pendingIntent);
      remoteViews.setTextViewText(R.id.tvMonth, context.getResources().getStringArray(R.array.month_long)[w.getMonth() - 1]);
      remoteViews.setTextViewText(R.id.tvDay, "" + w.getDay());

      if (de == null || de.getWorkTime().timeString().equals(TIME_ZERO) || de.getOverTime().timeString().equals(TIME_ZERO))
        remoteViews.setTextViewText(R.id.tvWorkTime, de == null ? TIME_ZERO : de.getWorkTime().timeString());
      else
        remoteViews.setTextViewText(R.id.tvWorkTime, de.getWorkTime().timeString() + " (" + de.getOverTime().timeString(true) + ")");
      appWidgetManager.updateAppWidget(idx, remoteViews);
    }
  }
}
