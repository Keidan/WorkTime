package fr.ralala.worktime.models;


import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Representation of a worktime day
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class WorkTimeDay {
  private int mDay = 1;
  private int mMonth = 1;
  private int mYear = 1970;
  private int mHours = 0;
  private int mMinutes = 0;
  private int mSeconds = 0;

  public WorkTimeDay() {
  }

  public WorkTimeDay(long ms) {
    this(0, 0, 0, 0, 0);
    fromTimeMS(ms);
  }

  public WorkTimeDay(int year, int month, int day, int hours, int minutes) {
    mYear = year;
    mMonth = month;
    mDay = day;
    mHours = hours;
    mMinutes = minutes;
  }

  private WorkTimeDay fromTimeMS(long ms) {
    mHours = (int)TimeUnit.MILLISECONDS.toHours(ms);
    mMinutes = (int)(TimeUnit.MILLISECONDS.toMinutes(ms) - (mHours * 60));
    return this;
  }

  public WorkTimeDay delTime(WorkTimeDay wtd) {
    return fromTimeMS(getTimeMs() - wtd.getTimeMs());
  }


  public WorkTimeDay addTime(WorkTimeDay wtd) {
    return fromTimeMS(getTimeMs() + wtd.getTimeMs());
  }

  public static WorkTimeDay now() {
    WorkTimeDay wtd = new WorkTimeDay();
    Calendar now = wtd.toCalendar();
    now.setFirstDayOfWeek(Calendar.MONDAY);
    now.setTime(new Date());
    wtd.fromCalendar(now);
    return wtd;
  }

  @SuppressWarnings("CloneDoesntCallSuperClone")
  public WorkTimeDay clone() {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.mDay = mDay;
    wtd.mMonth = mMonth;
    wtd.mYear = mYear;
    wtd.mHours = mHours;
    wtd.mMinutes = mMinutes;
    wtd.mSeconds = mSeconds;
    return wtd;
  }

  void copy(WorkTimeDay wtd) {
    mDay = wtd.mDay;
    mMonth = wtd.mMonth;
    mYear = wtd.mYear;
    mHours = wtd.mHours;
    mMinutes = wtd.mMinutes;
    mSeconds = wtd.mSeconds;
  }

  public String toString() {
    return dateString() + " " + timeString();
  }

  public void setTime(Calendar c) {
    fromCalendar(c);
  }

  public long getTimeMs() {
    return TimeUnit.HOURS.toMillis(mHours) + TimeUnit.MINUTES.toMillis(mMinutes);
  }

  public int compareTo(WorkTimeDay wtd) {
    return toCalendar().compareTo(wtd.toCalendar());
  }

  WorkTimeDay fromCalendar(final Calendar c) {
    mYear = c.get(Calendar.YEAR);
    mMonth = c.get(Calendar.MONTH) + 1;
    mDay = c.get(Calendar.DAY_OF_MONTH);
    mHours = c.get(Calendar.HOUR_OF_DAY);
    mMinutes = c.get(Calendar.MINUTE);
    return this;
  }

  public Calendar toCalendar() {
    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getDefault());
    c.set(Calendar.YEAR, mYear);
    c.set(Calendar.MONTH, mMonth - 1);
    c.set(Calendar.DAY_OF_MONTH, mDay);
    c.set(Calendar.HOUR_OF_DAY, mHours);
    c.set(Calendar.MINUTE, mMinutes);
    return c;
  }

  public static String timeString(int hours, int minutes) {
    return ((hours < 0 || minutes < 0) ? "-" : "") + String.format(Locale.US, "%02d:%02d", Math.abs(hours), Math.abs(minutes));
  }

  public String timeString(boolean plusSign) {
    String s = timeString(mHours, mMinutes);
    if(plusSign && !s.startsWith("-"))
      return "+" + s;
    return s;
  }

  public String timeString() {
    return timeString(false) ;
  }

  public String dateString() {
    return String.format(Locale.US, "%02d/%02d/%04d", mDay, mMonth, mYear);
  }

  public boolean isInMonth(int month) {
    return getMonth() == month;
  }

  public boolean isInYear(int year) {
    return getYear() == year;
  }

  boolean match(WorkTimeDay wtd) {
    return mDay == wtd.mDay && mMonth == wtd.mMonth && mYear == wtd.mYear && mHours == wtd.mHours && mMinutes == wtd.mMinutes;
  }

  public boolean isValidTime() {
    return !(mHours == 0 && mMinutes == 0);
  }

  public int getDay() {
    return mDay;
  }

  public void setDay(int day) {
    mDay = day;
  }

  public int getMonth() {
    return mMonth;
  }

  public void setMonth(int month) {
    mMonth = month;
  }

  public int getYear() {
    return mYear;
  }

  public void setYear(int year) {
    mYear = year;
  }

  public int getHours() {
    return mHours;
  }

  public void setHours(int hours) {
    mHours = hours;
  }

  public int getMinutes() {
    return mMinutes;
  }

  public void setMinutes(int minutes) {
    mMinutes = minutes;
  }

  public int getSeconds() {
    return mSeconds;
  }

  public void setSeconds(int seconds) {
    mSeconds = seconds;
  }
}
