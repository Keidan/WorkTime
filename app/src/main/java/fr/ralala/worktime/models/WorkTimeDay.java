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
  private int day = 1;
  private int month = 1;
  private int year = 1970;
  private int hours = 0;
  private int minutes = 0;
  private int seconds = 0;

  public WorkTimeDay() {
  }

  public WorkTimeDay(long ms) {
    this(0, 0, 0, 0, 0);
    fromTimeMS(ms);
  }

  public WorkTimeDay(int year, int month, int day, int hours, int minutes) {
    this.year = year;
    this.month = month;
    this.day = day;
    this.hours = hours;
    this.minutes = minutes;
  }

  public WorkTimeDay fromTimeMS(long ms) {
    hours = (int)TimeUnit.MILLISECONDS.toHours(ms);
    minutes = (int)(TimeUnit.MILLISECONDS.toMinutes(ms) - (hours * 60));
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
    now.setTime(new Date());
    wtd.fromCalendar(now);
    return wtd;
  }

  public WorkTimeDay clone() {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.day = day;
    wtd.month = month;
    wtd.year = year;
    wtd.hours = hours;
    wtd.minutes = minutes;
    wtd.seconds = seconds;
    return wtd;
  }

  public void copy(WorkTimeDay wtd) {
    day = wtd.day;
    month = wtd.month;
    year = wtd.year;
    hours = wtd.hours;
    minutes = wtd.minutes;
    seconds = wtd.seconds;
  }

  public String toString() {
    return dateString() + " " + timeString();
  }

  public void setTime(Calendar c) {
    fromCalendar(c);
  }

  public long getTimeMs() {
    return TimeUnit.HOURS.toMillis(hours) + TimeUnit.MINUTES.toMillis(minutes);
  }

  public int compareTo(WorkTimeDay wtd) {
    return toCalendar().compareTo(wtd.toCalendar());
  }

  public WorkTimeDay fromCalendar(final Calendar c) {
    year = c.get(Calendar.YEAR);
    month = c.get(Calendar.MONTH) + 1;
    day = c.get(Calendar.DAY_OF_MONTH);
    hours = c.get(Calendar.HOUR_OF_DAY);
    minutes = c.get(Calendar.MINUTE);
    return this;
  }

  public Calendar toCalendar() {
    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getDefault());
    c.set(Calendar.YEAR, year);
    c.set(Calendar.MONTH, month - 1);
    c.set(Calendar.DAY_OF_MONTH, day);
    c.set(Calendar.HOUR_OF_DAY, hours);
    c.set(Calendar.MINUTE, minutes);
    return c;
  }

  public static String timeString(int hours, int minutes) {
    return ((hours < 0 || minutes < 0) ? "-" : "") + String.format(Locale.US, "%02d:%02d", Math.abs(hours), Math.abs(minutes));
  }

  public String timeString() {
    return timeString(hours, minutes);
  }

  public String dateString() {
    return String.format(Locale.US, "%02d/%02d/%04d", day, month, year);
  }

  public boolean isInMonth(int month) {
    return getMonth() == month;
  }

  public boolean isInYear(int year) {
    return getYear() == year;
  }

  public boolean match(WorkTimeDay wtd) {
    return day == wtd.day && month == wtd.month && year == wtd.year && hours == wtd.hours && minutes == wtd.minutes;
  }

  public boolean isValidTime() {
    return !(hours == 0 && minutes == 0);
  }

  public int getDay() {
    return day;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public int getMonth() {
    return month;
  }

  public void setMonth(int month) {
    this.month = month;
  }

  public int getYear() {
    return year;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getHours() {
    return hours;
  }

  public void setHours(int hours) {
    this.hours = hours;
  }

  public int getMinutes() {
    return minutes;
  }

  public void setMinutes(int minutes) {
    this.minutes = minutes;
  }

  public int getSeconds() {
    return seconds;
  }

  public void setSeconds(int seconds) {
    this.seconds = seconds;
  }
}
