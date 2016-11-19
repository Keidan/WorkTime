package fr.ralala.worktime.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the main listview
 * </p>
 *
 * @author Keidan
 *         <p>
 *******************************************************************************
 */
public class DaysEntriesArrayAdapter extends ArrayAdapter<DayEntry> {

  private Context c = null;
  private int id = 0;
  private List<DayEntry> items = null;

  private class ViewHolder {
    LinearLayout llDay = null;
    TextView tvDay = null;
    TextView tvStart = null;
    TextView tvEnd = null;
    TextView tvPause = null;
    TextView tvTotal = null;
    TextView tvOver = null;
  }

  public DaysEntriesArrayAdapter(final Context context, final int textViewResourceId,
                                           final List<DayEntry> objects) {
    super(context, textViewResourceId, objects);
    this.c = context;
    this.id = textViewResourceId;
    this.items = objects;
  }

  @Override
  public DayEntry getItem(final int i) {
    return items.get(i);
  }

  @Override
  public @NonNull View getView(final int position, final View convertView,
                      @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    final DayEntry t = items.get(position);
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.llDay = (LinearLayout) v.findViewById(R.id.llDay);
      holder.tvDay = (TextView) v.findViewById(R.id.tvDay);
      holder.tvStart = (TextView) v.findViewById(R.id.tvStart);
      holder.tvEnd = (TextView) v.findViewById(R.id.tvEnd);
      holder.tvPause = (TextView) v.findViewById(R.id.tvPause);
      holder.tvTotal = (TextView) v.findViewById(R.id.tvTotal);
      holder.tvOver = (TextView) v.findViewById(R.id.tvOver);
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    WorkTimeDay today = WorkTimeDay.now();
    if (t != null) {

      Calendar cal = t.getDay().toCalendar();
      int bg = c.getResources().getColor(android.R.color.transparent, null);
      if (t.matchSimpleDate(today))
        bg = c.getResources().getColor(R.color.green, null);
      else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
        bg = c.getResources().getColor(R.color.blue_1, null);
      else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
        bg = c.getResources().getColor(R.color.blue_2, null);
      else if (t.getType() == DayType.HOLIDAY || t.getType() == DayType.PUBLIC_HOLIDAY)
        bg = c.getResources().getColor(R.color.purple, null);
      else if (t.getType() == DayType.UNPAID)
        bg = c.getResources().getColor(R.color.red, null);
      else if (t.getType() == DayType.SICKNESS)
        bg = c.getResources().getColor(R.color.orange, null);

      holder.llDay.setBackgroundColor(bg);

      /* Update the texts */
      if (holder.tvDay != null) {
        holder.tvDay.setText(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)) + " " +
          DayEntry.getDayString2lt(c, cal.get(Calendar.DAY_OF_WEEK)));
      }
      if (holder.tvStart != null) {
        if (!t.getStart().isValidTime())
          holder.tvStart.setText("-");
        else {
          holder.tvStart.setText(t.getStart().timeString());
        }
      }
      if (holder.tvEnd != null) {
        if (!t.getEnd().isValidTime())
          holder.tvEnd.setText("-");
        else {
          holder.tvEnd.setText(t.getEnd().timeString());
        }
      }
      if (holder.tvPause != null) {
        if (t.getPause().getHours() == 0 && t.getPause().getMinutes() == 0)
          holder.tvPause.setText("-");
        else {
          holder.tvPause.setText(t.getPause().timeString());
        }
      }
      if (holder.tvTotal != null) {
        if(!t.getStart().isValidTime() || !t.getEnd().isValidTime())
          holder.tvTotal.setText("-");
        else {
          holder.tvTotal.setText(t.getWorkTime().timeString());
        }
      }
      if (holder.tvOver != null) {
        MainApplication app = MainApplication.getApp(c);
        if(!t.getStart().isValidTime() || !t.getEnd().isValidTime() || !t.getPause().isValidTime()) {
          holder.tvOver.setText("-");
          holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
        }
        else {
          long overtime = t.getOverTimeMs(app);
          WorkTimeDay wtd = t.getOverTime(overtime);
          if(overtime == 0) {
            holder.tvOver.setText("-");
            holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
          } else {
            holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            if (overtime < 0) {
              holder.tvOver.setTextColor(c.getResources().getColor(android.R.color.holo_red_dark, null));
            } else
              holder.tvOver.setTextColor(c.getResources().getColor(android.R.color.holo_green_dark, null));
            holder.tvOver.setText(wtd.timeString());
          }
        }
      }
    }
    return v;
  }

}