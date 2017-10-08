package fr.ralala.worktime.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.activities.DayActivity;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Day widget
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DayWidgetProvider extends AppWidgetProvider {
  public static final String ACTION_FROM_WIDGET = "ACTION_FROM_WIDGET";

  @Override
  public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    MainApplication app = MainApplication.getApp(context);
    if(!app.openSql(context)) {
      AndroidHelper.toast(context, R.string.error_widget_sql);
      Log.e(getClass().getSimpleName(), "Widger error SQL");
      return ;
    }

    DayEntry de = app.getDaysFactory().getCurrentDay();
    WorkTimeDay w = de == null ? new WorkTimeDay() : de.getDay();

    RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_day);

    for (int idx : appWidgetIds) {
      Intent intent = new Intent(context, DayActivity.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.setAction(ACTION_FROM_WIDGET);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

      remoteViews.setOnClickPendingIntent(R.id.tvMonth, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvDay, pendingIntent);
      remoteViews.setOnClickPendingIntent(R.id.tvYear, pendingIntent);
      remoteViews.setTextViewText(R.id.tvMonth, context.getResources().getStringArray(R.array.month_long)[w.getMonth() - 1]);
      remoteViews.setTextViewText(R.id.tvDay, ""+w.getDay());
      remoteViews.setTextViewText(R.id.tvYear, ""+w.getYear());
      appWidgetManager.updateAppWidget(idx, remoteViews);
    }
  }
}
