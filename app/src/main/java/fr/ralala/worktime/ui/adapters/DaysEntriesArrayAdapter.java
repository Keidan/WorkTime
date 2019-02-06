package fr.ralala.worktime.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
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
import fr.ralala.worktime.ui.utils.UIHelper;

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

  private Context mContext;
  private int mId;
  private List<DayEntry> mItems;

  private class ViewHolder {
    LinearLayout llDay = null;
    TextView tvWeek = null;
    TextView tvDay = null;
    TextView tvStart = null;
    TextView tvEnd = null;
    TextView tvPause = null;
    TextView tvTotal = null;
    TextView tvOver = null;
    ColorStateList tvOverColors = null;
  }

  /**
   * Creates the array adapter.
   * @param context The Android context.
   * @param textViewResourceId The resource id of the container.
   * @param objects The objects list.
   */
  public DaysEntriesArrayAdapter(final Context context, final int textViewResourceId,
                                           final List<DayEntry> objects) {
    super(context, textViewResourceId, objects);
    mContext = context;
    mId = textViewResourceId;
    mItems = objects;
  }

  /**
   * Returns an items at a specific position.
   * @param i The item index.
   * @return The item.
   */
  @Override
  public DayEntry getItem(final int i) {
    return mItems.get(i);
  }

  /**
   * Returns the current view.
   * @param position The view position.
   * @param convertView The view to convert.
   * @param parent The parent.
   * @return The new view.
   */
  @Override
  public @NonNull View getView(final int position, final View convertView,
                      @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    final DayEntry t = mItems.get(position);
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      v = vi.inflate(mId, null);
      holder = new ViewHolder();
      holder.llDay = v.findViewById(R.id.llDay);
      holder.tvWeek = v.findViewById(R.id.tvWeek);
      holder.tvDay = v.findViewById(R.id.tvDay);
      holder.tvStart = v.findViewById(R.id.tvStart);
      holder.tvEnd = v.findViewById(R.id.tvEnd);
      holder.tvPause = v.findViewById(R.id.tvPause);
      holder.tvTotal = v.findViewById(R.id.tvTotal);
      holder.tvOver = v.findViewById(R.id.tvOver);
      holder.tvOverColors = holder.tvOver.getTextColors();
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }

    WorkTimeDay today = WorkTimeDay.now();
    if (t != null) {
      if(t.getWeekNumber() != DayEntry.INVALID_WEEK && MainApplication.getInstance().isDisplayWeek()) {
        holder.tvWeek.setMinHeight(0); // Min Height
        holder.tvWeek.setMinimumHeight(0); // Min Height
        holder.tvWeek.setHeight(MainApplication.getInstance().getDayRowsHeight() - 30);
        holder.tvWeek.setVisibility(View.VISIBLE);
        holder.tvDay.setVisibility(View.GONE);
        holder.tvStart.setVisibility(View.GONE);
        holder.tvEnd.setVisibility(View.GONE);
        holder.tvPause.setVisibility(View.GONE);
        holder.tvTotal.setVisibility(View.GONE);
        holder.tvOver.setVisibility(View.GONE);
        holder.llDay.setBackgroundColor(mContext.getResources().getColor(R.color.color_week, null));
        String week = mContext.getString(R.string.week).toLowerCase();
        week = Character.toUpperCase(week.charAt(0)) + week.substring(1);
        holder.tvWeek.setText((week + " " + t.getWeekNumber()));
      } else {
        setRowHeight(holder.tvDay);
        setRowHeight(holder.tvStart);
        setRowHeight(holder.tvEnd);
        setRowHeight(holder.tvPause);
        setRowHeight(holder.tvTotal);
        setRowHeight(holder.tvOver);
        holder.tvWeek.setVisibility(View.GONE);
        holder.tvDay.setVisibility(View.VISIBLE);
        holder.tvStart.setVisibility(View.VISIBLE);
        holder.tvEnd.setVisibility(View.VISIBLE);
        holder.tvPause.setVisibility(View.VISIBLE);
        holder.tvTotal.setVisibility(View.VISIBLE);
        holder.tvOver.setVisibility(View.VISIBLE);
        Calendar cal = t.getDay().toCalendar();
        int bg_morning;
        int bg_afternoon;
        if (t.matchSimpleDate(today)) {
          bg_afternoon = bg_morning = mContext.getResources().getColor(R.color.green, null);
        } else {
          if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && t.getTypeMorning() != DayType.PUBLIC_HOLIDAY)
            bg_morning = mContext.getResources().getColor(R.color.blue_1, null);
          else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && t.getTypeMorning() != DayType.PUBLIC_HOLIDAY)
            bg_morning = mContext.getResources().getColor(R.color.blue_2, null);
          else
            bg_morning = (t.getTypeMorning() == DayType.HOLIDAY) ?
                mContext.getResources().getColor(R.color.purple, null) :
                (t.getTypeMorning() == DayType.PUBLIC_HOLIDAY) ?
                    mContext.getResources().getColor(R.color.purple2, null) :
                    (t.getTypeMorning() == DayType.UNPAID) ?
                        mContext.getResources().getColor(R.color.red, null) :
                        (t.getTypeMorning() == DayType.SICKNESS) ?
                            mContext.getResources().getColor(R.color.orange, null) :
                            (t.getTypeMorning() == DayType.RECOVERY) ?
                                mContext.getResources().getColor(R.color.gray, null) :
                                mContext.getResources().getColor(android.R.color.transparent, null);

          if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && t.getTypeAfternoon() != DayType.PUBLIC_HOLIDAY)
            bg_afternoon = mContext.getResources().getColor(R.color.blue_1, null);
          else if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && t.getTypeAfternoon() != DayType.PUBLIC_HOLIDAY)
            bg_afternoon = mContext.getResources().getColor(R.color.blue_2, null);
          else
            bg_afternoon = (t.getTypeAfternoon() == DayType.HOLIDAY) ?
                mContext.getResources().getColor(R.color.purple, null) :
                (t.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) ?
                    mContext.getResources().getColor(R.color.purple2, null) :
                    (t.getTypeAfternoon() == DayType.UNPAID) ?
                        mContext.getResources().getColor(R.color.red, null) :
                        (t.getTypeAfternoon() == DayType.SICKNESS) ?
                            mContext.getResources().getColor(R.color.orange, null) :
                            (t.getTypeAfternoon() == DayType.RECOVERY) ?
                                mContext.getResources().getColor(R.color.gray, null) :
                                mContext.getResources().getColor(android.R.color.transparent, null);
        }
        if (bg_morning == bg_afternoon) {
          holder.llDay.setBackgroundColor(bg_morning);
        } else {
          UIHelper.applyLinearGradient(holder.llDay, bg_afternoon, bg_morning);
        }


        /* Update the texts */
        if (holder.tvDay != null) {
          holder.tvDay.setText((String.format(Locale.US, "%02d", cal.get(Calendar.DAY_OF_MONTH)) + " " +
              DayEntry.getDayString2lt(mContext, cal.get(Calendar.DAY_OF_WEEK))));
        }
        if (holder.tvStart != null) {
          if (isPaidButNotWorkedMorning(t)) {
            if(!isNotValidAfternoon(t))
              holder.tvStart.setText(t.getStartAfternoon().timeString());
            else
              holder.tvStart.setText("*");
          }
          else if (t.getTypeMorning() == DayType.RECOVERY && !isNotValidAfternoon(t))
            holder.tvStart.setText(t.getStartAfternoon().timeString());
          else if (isNotValidMorning(t))
            holder.tvStart.setText("-");
          else {
            holder.tvStart.setText(t.getStartMorning().timeString());
          }
        }
        if (holder.tvEnd != null) {
          if (isPaidButNotWorkedAfternoon(t)) {
            holder.tvEnd.setText("*");
            if(!isNotValidMorning(t))
              holder.tvEnd.setText(t.getEndMorning().timeString());
            else
              holder.tvEnd.setText("*");
          }
          else if (t.getTypeAfternoon() == DayType.RECOVERY && !isNotValidMorning(t))
            holder.tvEnd.setText(t.getEndMorning().timeString());
          else if (isNotValidAfternoon(t)) {
            WorkTimeDay w = t.getEndMorning();
            if (w.timeString().equals("00:00") || isNotValidMorning(t))
              holder.tvEnd.setText("-");
            else
              holder.tvEnd.setText(t.getEndMorning().timeString());
          } else {
            holder.tvEnd.setText(t.getEndAfternoon().timeString());
          }
        }
        if (holder.tvPause != null) {
          if (isPaidButNotWorkedMorning(t) && isPaidButNotWorkedAfternoon(t))
            holder.tvPause.setText("*");
          else {
            WorkTimeDay w = t.getPause();
            if (isNotValidMorning(t) && isNotValidAfternoon(t) || w.timeString().equals("00:00"))
              holder.tvPause.setText("-");
            else {
              holder.tvPause.setText(t.getPause().timeString());
            }
          }
        }
        WorkTimeDay w = t.getWorkTime();
        if (holder.tvTotal != null) {
          if (isPaidButNotWorkedMorning(t) && isPaidButNotWorkedAfternoon(t))
            holder.tvTotal.setText("*");
          else if (isNotValidMorning(t) && isNotValidAfternoon(t) || w.timeString().equals("00:00"))
            holder.tvTotal.setText("-");
          else {
            holder.tvTotal.setText(t.getWorkTime().timeString());
          }
        }
        if (holder.tvOver != null) {
          if (isPaidButNotWorkedMorning(t) && isPaidButNotWorkedAfternoon(t)) {
            holder.tvOver.setText("*");
            holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            holder.tvOver.setGravity(Gravity.CENTER);
            holder.tvOver.setTextColor(holder.tvOverColors);
          } else if (isNotValidMorning(t) && isNotValidAfternoon(t) || w.timeString().equals("00:00")) {
            holder.tvOver.setText("-");
            holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
            holder.tvOver.setGravity(Gravity.CENTER);
            holder.tvOver.setTextColor(holder.tvOverColors);
          } else {
            long overtime = t.getOverTimeMs();
            WorkTimeDay wtd = t.getOverTime(overtime);
            if (overtime == 0) {
              holder.tvOver.setText("-");
              holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
              holder.tvOver.setGravity(Gravity.CENTER);
              holder.tvOver.setTextColor(holder.tvOverColors);
            } else {
              holder.tvOver.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
              holder.tvOver.setGravity(Gravity.END);
              if (overtime < 0) {
                holder.tvOver.setTextColor(mContext.getResources().getColor(R.color.over_neg, null));
              } else {
                holder.tvOver.setTextColor(mContext.getResources().getColor(R.color.over_pos, null));
              }
              holder.tvOver.setText(wtd.timeString());
            }
          }
        }
      }
    }
    return v;
  }

  /**
   * Sets the row height.
   * @param v The current TextView.
   */
  private void setRowHeight(TextView v) {
    v.setMinHeight(0); // Min Height
    v.setMinimumHeight(0); // Min Height
    v.setHeight(MainApplication.getInstance().getDayRowsHeight()); // Height
  }

  /**
   * Tests if the morning value of the day entry is valid or not.
   * @param t The day entry to test.
   * @return boolean
   */
  private boolean isNotValidMorning(DayEntry t) {
    return (t.getTypeMorning() != DayType.AT_WORK || (!t.getStartMorning().isValidTime()
        && !t.getEndMorning().isValidTime()));
  }

  /**
   * Tests if the afternoon value of the day entry is valid or not.
   * @param t The day entry to test.
   * @return boolean
   */
  private boolean isNotValidAfternoon(DayEntry t) {
    return (t.getTypeAfternoon() != DayType.AT_WORK || (!t.getStartAfternoon().isValidTime()
        && !t.getEndAfternoon().isValidTime()));
  }



  /**
   * Tests if the morning value of the day entry is paid but not worked or not.
   * @param t The day entry to test.
   * @return boolean
   */
  private boolean isPaidButNotWorkedMorning(DayEntry t) {
    return t.getTypeMorning() == DayType.PUBLIC_HOLIDAY || ((t.getTypeMorning() == DayType.HOLIDAY
        || t.getTypeMorning() == DayType.SICKNESS) && t.getStartMorning().isValidTime()
        && t.getEndMorning().isValidTime());
  }

  /**
   * Tests if the afternoon value of the day entry is paid but not worked or not.
   * @param t The day entry to test.
   * @return boolean
   */
  private boolean isPaidButNotWorkedAfternoon(DayEntry t) {
    return t.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY || ((t.getTypeAfternoon() == DayType.HOLIDAY
        || t.getTypeAfternoon() == DayType.SICKNESS) && (t.getStartAfternoon().isValidTime()
        && t.getEndAfternoon().isValidTime()));
  }
}