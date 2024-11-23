package fr.ralala.worktime.models;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Representation of a worktime day
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class WorkTimeDay {
  private int mDay = 1;
  private int mMonth = 1;
  private int mYear = 1970;
  private int mHours = 0;
  private int mMinutes = 0;
  private int mSeconds = 0;

  /**
   * Creates an empty instance.
   */
  public WorkTimeDay() {
  }

  /**
   * Creates an instance using the time in milliseconds.
   *
   * @param ms Current time in milliseconds.
   */
  public WorkTimeDay(long ms) {
    this(0, 0, 0, 0, 0);
    fromTimeMS(ms);
  }

  /**
   * Creates an instance using detailed date time.
   *
   * @param year    Current year.
   * @param month   Current month.
   * @param day     Current day.
   * @param hours   Current hours.
   * @param minutes Current minutes.
   */
  public WorkTimeDay(int year, int month, int day, int hours, int minutes) {
    mYear = year;
    mMonth = month;
    mDay = day;
    mHours = hours;
    mMinutes = minutes;
  }

  /**
   * Clones the current instance.
   *
   * @return WorkTimeDay
   */
  public @NonNull WorkTimeDay copy() {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.mDay = mDay;
    wtd.mMonth = mMonth;
    wtd.mYear = mYear;
    wtd.mHours = mHours;
    wtd.mMinutes = mMinutes;
    wtd.mSeconds = mSeconds;
    return wtd;
  }

  /**
   * Initializes the current instance from a time in milliseconds.
   *
   * @param ms The time in milliseconds.
   * @return WorkTimeDay
   */
  public WorkTimeDay fromTimeMS(long ms) {
    mHours = (int) TimeUnit.MILLISECONDS.toHours(ms);
    mMinutes = (int) (TimeUnit.MILLISECONDS.toMinutes(ms) - (mHours * 60));
    return this;
  }

  /**
   * Deletes a time.
   *
   * @param wtd Time to delete.
   * @return WorkTimeDay
   */
  public WorkTimeDay delTime(WorkTimeDay wtd) {
    return fromTimeMS(getTimeMs() - wtd.getTimeMs());
  }


  /**
   * Adds a time.
   *
   * @param wtd Time to add.
   * @return WorkTimeDay
   */
  public WorkTimeDay addTime(WorkTimeDay wtd) {
    return fromTimeMS(getTimeMs() + wtd.getTimeMs());
  }

  /**
   * Returns the current instance initialized to the current time.
   *
   * @return WorkTimeDay
   */
  public static WorkTimeDay now() {
    WorkTimeDay wtd = new WorkTimeDay();
    Calendar now = wtd.toCalendar();
    now.setFirstDayOfWeek(Calendar.MONDAY);
    now.setTime(new Date());
    wtd.fromCalendar(now);
    return wtd;
  }

  /**
   * Converts a string date to a WorkTimeDay instance.
   *
   * @param d String date to convert.
   * @return WorkTimeDay
   */
  public static WorkTimeDay fromDate(@Nullable final String d) {
    WorkTimeDay wtd = new WorkTimeDay();
    if(d == null)
      return now();
    String[] split = d.split("/");
    wtd.mYear = Integer.parseInt(split[2]);
    wtd.mMonth = Integer.parseInt(split[1]);
    wtd.mDay = Integer.parseInt(split[0]);
    wtd.mHours = 0;
    wtd.mMinutes = 0;
    return wtd;
  }

  /**
   * Copies the input entry to this instance.
   *
   * @param wtd The instance to copy.
   */
  void copy(WorkTimeDay wtd) {
    mDay = wtd.mDay;
    mMonth = wtd.mMonth;
    mYear = wtd.mYear;
    mHours = wtd.mHours;
    mMinutes = wtd.mMinutes;
    mSeconds = wtd.mSeconds;
  }

  /**
   * Returns the date and the time.
   *
   * @return String
   */
  public @NonNull String toString() {
    return dateString() + " " + timeString();
  }

  /**
   * Sets the Calendar entry to the current instance.
   *
   * @param c Calendar input.
   */
  public void setTime(Calendar c) {
    fromCalendar(c);
  }

  /**
   * Returns the time in milliseconds.
   *
   * @return long
   */
  public long getTimeMs() {
    return TimeUnit.HOURS.toMillis(mHours) + TimeUnit.MINUTES.toMillis(mMinutes);
  }

  /**
   * Compares the current instance to another.
   *
   * @param wtd The instance to compare.
   * @return int
   */
  public int compareTo(WorkTimeDay wtd) {
    String s1 = String.format(Locale.US, "%04d/%02d/%02d", mYear, mMonth, mDay);
    String s2 = String.format(Locale.US, "%04d/%02d/%02d", wtd.mYear, wtd.mMonth, wtd.mDay);
    return s1.compareTo(s2);
  }

  /**
   * Converts a Calendar to a WorkTimeDay instance.
   *
   * @param c Calendar to convert.
   * @return WorkTimeDay
   */
  WorkTimeDay fromCalendar(final Calendar c) {
    mYear = c.get(Calendar.YEAR);
    mMonth = c.get(Calendar.MONTH) + 1;
    mDay = c.get(Calendar.DAY_OF_MONTH);
    mHours = c.get(Calendar.HOUR_OF_DAY);
    mMinutes = c.get(Calendar.MINUTE);
    return this;
  }

  /**
   * Converts the current instance to a Calendar value.
   *
   * @return Calendar
   */
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

  /**
   * Returns the time in string format.
   *
   * @param hours   The hours.
   * @param minutes The minutes.
   * @return String
   */
  public static String timeString(int hours, int minutes) {
    return ((hours < 0 || minutes < 0) ? "-" : "") + String.format(Locale.US, "%02d:%02d", Math.abs(hours), Math.abs(minutes));
  }

  /**
   * Returns the time in string format.
   *
   * @param plusSign True to add the '+' sign.
   * @return String
   */
  public String timeString(boolean plusSign) {
    String s = timeString(mHours, mMinutes);
    if (plusSign && !s.startsWith("-"))
      return "+" + s;
    return s;
  }

  /**
   * Returns the time in string format.
   *
   * @return String
   */
  public String timeString() {
    return timeString(false);
  }

  /**
   * Returns the date in string format.
   *
   * @return String
   */
  public String dateString() {
    return String.format(Locale.US, "%02d/%02d/%04d", mDay, mMonth, mYear);
  }

  /**
   * Tests if the current time matches with another.
   *
   * @param wtd The time to test.
   * @return boolean
   */
  boolean match(WorkTimeDay wtd) {
    return mDay == wtd.mDay && mMonth == wtd.mMonth && mYear == wtd.mYear && mHours == wtd.mHours && mMinutes == wtd.mMinutes;
  }

  /**
   * Tests if the time is valid.
   *
   * @return boolean
   */
  public boolean isValidTime() {
    return !(mHours == 0 && mMinutes == 0);
  }

  /**
   * Returns the day.
   *
   * @return int
   */
  public int getDay() {
    return mDay;
  }

  /**
   * Returns the day.
   *
   * @return String
   */
  public String getDayString() {
    return String.format(Locale.US, "%02d", mDay);
  }

  /**
   * Sets the day.
   *
   * @param day The new value.
   */
  public void setDay(int day) {
    mDay = day;
  }

  /**
   * Returns the month.
   *
   * @return int
   */
  public int getMonth() {
    return mMonth;
  }

  /**
   * Returns the month.
   *
   * @return String
   */
  public String getMonthString() {
    return String.format(Locale.US, "%02d", mMonth);
  }

  /**
   * Sets the month.
   *
   * @param month The new value.
   */
  public void setMonth(int month) {
    mMonth = month;
  }

  /**
   * Returns the year.
   *
   * @return int
   */
  public int getYear() {
    return mYear;
  }

  /**
   * Returns the year.
   *
   * @return String
   */
  public String getYearString() {
    return String.format(Locale.US, "%04d", mYear);
  }

  /**
   * Sets the year.
   *
   * @param year The new value.
   */
  public void setYear(int year) {
    mYear = year;
  }

  /**
   * Returns the hours.
   *
   * @return int
   */
  public int getHours() {
    return mHours;
  }

  /**
   * Sets the hours.
   *
   * @param hours The new value.
   */
  public void setHours(int hours) {
    mHours = hours;
  }

  /**
   * Returns the minutes.
   *
   * @return int
   */
  public int getMinutes() {
    return mMinutes;
  }

  /**
   * Sets the minutes.
   *
   * @param minutes The new value.
   */
  public void setMinutes(int minutes) {
    mMinutes = minutes;
  }

  /**
   * Returns the seconds.
   *
   * @return int
   */
  public int getSeconds() {
    return mSeconds;
  }

  /**
   * Sets the seconds.
   *
   * @param seconds The new value.
   */
  public void setSeconds(int seconds) {
    mSeconds = seconds;
  }
}
