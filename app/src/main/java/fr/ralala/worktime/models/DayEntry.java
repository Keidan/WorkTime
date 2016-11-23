package fr.ralala.worktime.models;


import android.content.Context;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Representation of a day entry
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DayEntry {
  private String name = "";
  private WorkTimeDay day = null;
  private WorkTimeDay start = null;
  private WorkTimeDay end = null;
  private WorkTimeDay pause = null;
  private DayType type = DayType.ERROR;
  private double amountByHour = 0.0;

  public DayEntry(final WorkTimeDay day, final DayType type) {
    this.day = new WorkTimeDay();
    this.start = new WorkTimeDay();
    this.end = new WorkTimeDay();
    this.pause = new WorkTimeDay();
    this.type = type;
    this.day.copy(day);
  }

  public DayEntry(final Calendar day, final DayType type) {
    this(new WorkTimeDay().fromCalendar(day), type);
  }

  public double getWorkTimePay() {
    return getAmountByHour() * Double.parseDouble(
      String.valueOf(getWorkTime().getHours()) + "." + String.valueOf(getWorkTime().getMinutes()));
  }

  public long getOverTimeMs(MainApplication app) {
    return getWorkTime().getTimeMs() - app.getLegalWorkTimeByDay().getTimeMs();
  }

  public WorkTimeDay getOverTime(long diffInMs) {
    Calendar cal_ = Calendar.getInstance();
    cal_.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal_.setTime(new Date(diffInMs));
    return new WorkTimeDay(0, 0, 0, (int)TimeUnit.MILLISECONDS.toHours(diffInMs), cal_.get(Calendar.MINUTE));
  }

  public WorkTimeDay getOverTime(MainApplication app) {
    return getOverTime(getOverTimeMs(app));
  }

  public WorkTimeDay getWorkTime() {
    long st = TimeUnit.HOURS.toMillis(start.getHours()) + TimeUnit.MINUTES.toMillis(start.getMinutes());
    long ed = TimeUnit.HOURS.toMillis(end.getHours()) + TimeUnit.MINUTES.toMillis(end.getMinutes());
    long p = TimeUnit.HOURS.toMillis(pause.getHours()) + TimeUnit.MINUTES.toMillis(pause.getMinutes());
    long time = ((ed - st) - p);
    WorkTimeDay wtd = new WorkTimeDay();
    if(time == 0) return wtd;
    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getTimeZone("GMT"));
    c.setTime(new Date((ed - st) - p));
    return wtd.fromCalendar(c);
  }

  public void copy(DayEntry de) {
    type = de.type;
    name = de.name;
    day.copy(de.day);
    start.copy(de.start);
    end.copy(de.end);
    pause.copy(de.pause);
    amountByHour = de.amountByHour;
  }

  public boolean match(DayEntry de) {
    return day.match(de.day) && start.match(de.start) && end.match(de.end) && pause.match(de.pause) &&
      type == de.type && amountByHour == de.amountByHour;
  }

  public boolean matchSimpleDate(WorkTimeDay current) {
    boolean ret = (current.getMonth() == day.getMonth() && current.getDay() == day.getDay());
    /* simple holidays can change between each years */
    if(ret && type == DayType.HOLIDAY)
      return current.getYear() == day.getYear();
    return ret;
  }

  public static String getDayString2lt(final Context c, final int dayOfWeek) {
    switch (dayOfWeek) {
      case Calendar.MONDAY: return c.getString(R.string.day_monday_2lt);
      case Calendar.TUESDAY: return c.getString(R.string.day_tuesday_2lt);
      case Calendar.WEDNESDAY: return c.getString(R.string.day_wednesday_2lt);
      case Calendar.THURSDAY: return c.getString(R.string.day_thursday_2lt);
      case Calendar.FRIDAY: return c.getString(R.string.day_friday_2lt);
      case Calendar.SATURDAY: return c.getString(R.string.day_saturday_2lt);
      case Calendar.SUNDAY: return c.getString(R.string.day_sunday_2lt);
    }
    return c.getString(R.string.error);
  }

  private WorkTimeDay parseTime(String time) {
    WorkTimeDay wtd = new WorkTimeDay();
    String split [] = time.split(":");
    wtd.setHours(Integer.parseInt(split[0]));
    wtd.setMinutes(Integer.parseInt(split[1]));
    return wtd;
  }

  public void setStart(String start) {
    this.start.copy(parseTime(start));
  }

  public void setPause(String pause) {
    this.pause.copy(parseTime(pause));
  }

  public void setEnd(String end) {
    this.end.copy(parseTime(end));
  }

  public WorkTimeDay getDay() {
    return day;
  }

  public WorkTimeDay getStart() {
    return start;
  }

  public WorkTimeDay getPause() {
    return pause;
  }

  public DayType getType() {
    return type;
  }

  public void setType(DayType type) {
    this.type = type;
  }

  public WorkTimeDay getEnd() {
    return end;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getAmountByHour() {
    return amountByHour;
  }

  public void setAmountByHour(double amountByHour) {
    this.amountByHour = amountByHour;
  }
}
