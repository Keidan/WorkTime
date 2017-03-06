package fr.ralala.worktime.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

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
      setRowHeight(holder.tvDay);
      setRowHeight(holder.tvStart);
      setRowHeight(holder.tvEnd);
      setRowHeight(holder.tvPause);
      setRowHeight(holder.tvTotal);
      setRowHeight(holder.tvOver);

      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    WorkTimeDay today = WorkTimeDay.now();
    if (t != null) {

      Calendar cal = t.getDay().toCalendar();
      int bg_morning;
      int bg_afternoon;
      if (t.matchSimpleDate(today)) {
        bg_afternoon = bg_morning = c.getResources().getColor(R.color.green, null);
      } else {
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && t.getTypeMorning() != DayType.PUBLIC_HOLIDAY)
          bg_morning = c.getResources().getColor(R.color.blue_1, null);
        else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && t.getTypeMorning() != DayType.PUBLIC_HOLIDAY)
          bg_morning = c.getResources().getColor(R.color.blue_2, null);
        else
          bg_morning = (t.getTypeMorning() == DayType.HOLIDAY) ?
            c.getResources().getColor(R.color.purple, null) :
            (t.getTypeMorning() == DayType.PUBLIC_HOLIDAY) ?
              c.getResources().getColor(R.color.purple2, null) :
              (t.getTypeMorning() == DayType.UNPAID) ?
                c.getResources().getColor(R.color.red, null) :
                (t.getTypeMorning() == DayType.SICKNESS) ?
                  c.getResources().getColor(R.color.orange, null) :
                  c.getResources().getColor(android.R.color.transparent, null);

        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && t.getTypeAfternoon() != DayType.PUBLIC_HOLIDAY)
          bg_afternoon = c.getResources().getColor(R.color.blue_1, null);
        else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && t.getTypeAfternoon() != DayType.PUBLIC_HOLIDAY)
          bg_afternoon = c.getResources().getColor(R.color.blue_2, null);
        else
          bg_afternoon = (t.getTypeAfternoon() == DayType.HOLIDAY) ?
            c.getResources().getColor(R.color.purple, null) :
            (t.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) ?
              c.getResources().getColor(R.color.purple2, null) :
              (t.getTypeAfternoon() == DayType.UNPAID) ?
                c.getResources().getColor(R.color.red, null) :
                (t.getTypeAfternoon() == DayType.SICKNESS) ?
                  c.getResources().getColor(R.color.orange, null) :
                  c.getResources().getColor(android.R.color.transparent, null);
      }
      if(bg_morning == bg_afternoon) {
        holder.llDay.setBackgroundColor(bg_morning);
      } else {
        AndroidHelper.applyLinearGradient(holder.llDay, bg_afternoon, bg_morning);
      }


      /* Update the texts */
      if (holder.tvDay != null) {
        holder.tvDay.setText(String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)) + " " +
          DayEntry.getDayString2lt(c, cal.get(Calendar.DAY_OF_WEEK)));
      }
      if (holder.tvStart != null) {
        if (isNotValidMorning(t))
          holder.tvStart.setText("-");
        else {
          holder.tvStart.setText(t.getStartMorning().timeString());
        }
      }
      if (holder.tvEnd != null) {
        if (isNotValidAfternoon(t))
          holder.tvEnd.setText("-");
        else {
          holder.tvEnd.setText(t.getEndAfternoon().timeString());
        }
      }
      if (holder.tvPause != null) {
        if (isNotValidMorning(t) && isNotValidAfternoon(t))
          holder.tvPause.setText("-");
        else {
          holder.tvPause.setText(t.getPause().timeString());
        }
      }
      if (holder.tvTotal != null) {
        if(isNotValidMorning(t) && isNotValidAfternoon(t))
          holder.tvTotal.setText("-");
        else {
          holder.tvTotal.setText(t.getWorkTime().timeString());
        }
      }
      if (holder.tvOver != null) {
        MainApplication app = MainApplication.getApp(c);
        if(isNotValidMorning(t) && isNotValidAfternoon(t)) {
          holder.tvOver.setText("-");
          holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
          holder.tvOver.setGravity(Gravity.CENTER);
        }
        else {
          long overtime = t.getOverTimeMs(app);
          WorkTimeDay wtd = t.getOverTime(overtime);
          if(overtime == 0) {
            holder.tvOver.setText("-");
            holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            holder.tvOver.setGravity(Gravity.CENTER);
          } else {
            holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
            holder.tvOver.setGravity(Gravity.END);
            if (overtime < 0) {
              holder.tvOver.setTextColor(c.getResources().getColor(android.R.color.holo_red_dark, null));
            } else {
              holder.tvOver.setTextColor(c.getResources().getColor(android.R.color.holo_green_dark, null));
            }
            holder.tvOver.setText(wtd.timeString());
          }
        }
      }
    }
    return v;
  }

  private void setRowHeight(TextView v) {
    v.setMinHeight(0); // Min Height
    v.setMinimumHeight(0); // Min Height
    v.setHeight(44); // Height
  }

  private boolean isNotValidMorning(DayEntry t) {
    return (t.getTypeMorning() != DayType.AT_WORK || (!t.getStartMorning().isValidTime() && !t.getEndMorning().isValidTime()));
  }
  private boolean isNotValidAfternoon(DayEntry t) {
    return (t.getTypeAfternoon() != DayType.AT_WORK || (!t.getStartAfternoon().isValidTime() && !t.getEndAfternoon().isValidTime()));
  }

}