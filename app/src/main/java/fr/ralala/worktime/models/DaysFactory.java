package fr.ralala.worktime.models;




import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.MainApplication;
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
  private final List<DayEntry> days;
  private SqlFactory sql = null;

  public DaysFactory() {
    days = new ArrayList<>();
  }

  public void reload(final SqlFactory sql) {
    this.sql = sql;
    days.clear();
    days.addAll(sql.getDays());
  }

  public List<DayEntry> list() {
    return days;
  }


  public int getWorkDayFromWeek(int week, boolean onlyAtWork) {
    int wDays = 0;
    int prevMonth = -1;
    for(DayEntry de : days) {
      if(onlyAtWork && de.getType() != DayType.AT_WORK) continue;
      if(de.getDay().isInWeek(week))
        if(prevMonth == -1 || prevMonth == de.getDay().getMonth()) {
          ++wDays;
          prevMonth = de.getDay().getMonth();
        }
    }
    return wDays;
  }


  public double getWageFromWeek(int week, int month) {
    double wage = 0.0;
    for(DayEntry de : days) {
      if(de.getDay().isInWeek(week) && de.getDay().isInMonth(month)) {
        wage += de.getWorkTimePay();
      }
    }
    return wage;
  }

  public WorkTimeDay getWorkTimeDayFromWeek(int week) {
    long hours = 0L, minutes = 0L;
    int prevMonth = -1;
    for(DayEntry de : days) {
      if(de.getDay().isInWeek(week) && de.getType() == DayType.AT_WORK) {
        if(prevMonth == -1 || prevMonth == de.getDay().getMonth()) {
          WorkTimeDay wt = de.getWorkTime();
          hours += wt.getHours();
          minutes += wt.getMinutes();
          prevMonth = de.getDay().getMonth();
        }
      }
    }
    if(hours == 0 && minutes == 0) return new WorkTimeDay();
    return new WorkTimeDay().fromTimeUsingCalendar(hours, minutes, prevMonth == -1 ? 0 : prevMonth);
  }

  public double checkForDayDateAndCopy(DayEntry current) {
    for(DayEntry de : days) {
      if(de.getDay().dateString().equals(current.getDay().dateString())) {
        current.copy(de);
        // Calculate Pay
        return de.getWorkTimePay();
      }
    }
    return 0.0;
  }

  public void remove(final DayEntry de) {
    for(int i = 0; i < days.size(); i++)
      if(de.match(days.get(i))) {
        days.remove(i);
        break;
      }
    sql.removeDay(de);
  }

  public void add(final DayEntry de) {
    days.add(de);
    sql.insertDay(de);
    Collections.sort(days, new Comparator<DayEntry>() {
      @Override
      public int compare(DayEntry a, DayEntry b) {
        return a.getDay().compareTo(b.getDay());
      }
    });
  }
}
