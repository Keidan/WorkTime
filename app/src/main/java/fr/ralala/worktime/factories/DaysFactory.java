package fr.ralala.worktime.factories;


import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.sql.SqlConstants;
import fr.ralala.worktime.sql.SqlFactory;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Days factory functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DaysFactory {
  private SqlFactory mSql = null;

  /**
   * Sets the reference to the SqlFactory.
   * @param sql The SQLite factory.
   */
  public void setSqlFactory(final SqlFactory sql) {
    mSql = sql;
  }

  /**
   * Returns the list of days.
   * @param year The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day The current day (-1 to ignore days).
   * @return List<DayEntry>
   */
  public List<DayEntry> list(int year, int month, int day) {
    if(mSql == null)
      return Collections.emptyList();
    return mSql.getDays(year, month, day);
  }

  /**
   * Returns the day.
   * @param date The current date.
   * @return DayEntry
   */
  public DayEntry getDay(String date) {
    return mSql.getDay(date);
  }

  /**
   * Returns the years (unique values only) from the days table.
   * @return List<Integer>
   */
  public List<Integer> getYears() {
    return mSql.getDaysYears();
  }

  /**
   * Returns the months (unique values only) from the days table.
   * @param year The reference year.
   * @return List<Integer>
   */
  public List<Integer> getMonths(int year) {
    return mSql.getDaysMonths(year);
  }

  /**
   * Returns the weeks and days in a week.
   * Map<WEEK, List<DayEntry>>
   * @param year The current year.
   * @param month The current month.
   * @return <code>Map<WEEK, List<DayEntry>></code>
   */
  @SuppressLint("UseSparseArrays")
  public Map<Integer, List<DayEntry>> getWeeksAndDays(int year, int month) {
    return getWeeksAndDays(mSql.getDays(year, month, -1));
  }
  /**
   * Returns the weeks and days in a week.
   * Map<WEEK, List<DayEntry>>
   * @param refList The reference List.
   * @return <code>Map<WEEK, List<DayEntry>></code>
   */
  @SuppressLint("UseSparseArrays")
  private Map<Integer, List<DayEntry>> getWeeksAndDays(List<DayEntry> refList) {
    Map<Integer, List<DayEntry>> list = new HashMap<>();
    for (DayEntry de : refList) {
      int w = de.getDay().toCalendar().get(Calendar.WEEK_OF_YEAR);
      if (!list.containsKey(w))
        list.put(w, new ArrayList<>());
      list.get(w).add(de);
    }
    return list;
  }

  /**
   * Returns the list of work time day of a week.
   * @param refList The reference List.
   * @param week The associated week.
   * @return WorkTimeDay
   */
  public WorkTimeDay getWorkTimeDayFromWeek(List<DayEntry> refList, int week) {
    WorkTimeDay ret = new WorkTimeDay();
    Map<Integer, List<DayEntry>> wdays = getWeeksAndDays(refList);
    if(wdays.containsKey(week)) {
      List<DayEntry> days = wdays.get(week);
      for (DayEntry de : days) {
        if (de.isValidMorningType() && de.isValidAfternoonType()) {
          ret.addTime(de.getWorkTime());
        }
      }
    }
    return ret;
  }

  /**
   * Returns the current day.
   * @return DayEntry
   */
  public DayEntry getCurrentDay() {
    WorkTimeDay now = WorkTimeDay.now();
    DayEntry d = mSql.getDay(now.dateString());
    if(d == null)
      d = new DayEntry(WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
    return d;
  }

  /**
   * Copies the current entry and calculate the pay.
   * @param refList The reference list.
   * @param current The current entry.
   */
  public void checkForDayDateAndCopy(List<DayEntry> refList, DayEntry current) {
    for(DayEntry de : refList) {
      WorkTimeDay wtdLoop = de.getDay();
      WorkTimeDay wtdParam = current.getDay();

      if(wtdLoop.getDay() == wtdParam.getDay() && wtdLoop.getMonth() == wtdParam.getMonth() && wtdLoop.getYear() == wtdParam.getYear()) {
        current.copy(de);
        // Calculate Pay
        de.getWorkTimePay();
      }
    }
  }

  /**
   * Removes an entry.
   * @param de The entry to delete.
   */
  public void remove(final DayEntry de) {
    mSql.removeDay(de);
  }

  /**
   * Adds a new entry.
   * @param de The entry to add.
   */
  public void add(final DayEntry de) {
    mSql.insertOrUpdateDay(de, de.getID() != SqlConstants.INVALID_ID);
  }
}
