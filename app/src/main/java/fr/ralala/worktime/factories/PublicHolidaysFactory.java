package fr.ralala.worktime.factories;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.sql.SqlFactory;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Public holidays factory functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class PublicHolidaysFactory {
  private final List<DayEntry> publicHolidays;
  private SqlFactory sql = null;

  public PublicHolidaysFactory() {
    publicHolidays = new ArrayList<>();
  }

  public void reload(final SqlFactory sql) {
    this.sql = sql;
    publicHolidays.clear();
    publicHolidays.addAll(sql.getPublicHolidays());
  }

  public List<DayEntry> list() {
    return publicHolidays;
  }

  public boolean isPublicHolidays(WorkTimeDay currentDate) {
    for(DayEntry de : publicHolidays) {
      if((de.getTypeMorning() == DayType.PUBLIC_HOLIDAY && de.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) && de.matchSimpleDate(currentDate))
        return true;
    }
    return false;
  }

  public boolean testValidity(final DayEntry de) {
    for(DayEntry d : publicHolidays) {
      WorkTimeDay d_day = d.getDay();
      WorkTimeDay de_day = de.getDay();
      if(d_day.getDay() == de_day.getDay() && d_day.getMonth() == de_day.getMonth()) {
        if(d.isRecurrence() || de.isRecurrence()) {
          return false;
        } else if(d_day.getYear() == de_day.getYear())
          return false;
      }
    }
    return true;
  }

  public void remove(final DayEntry de) {
    publicHolidays.remove(de);
    sql.removePublicHoliday(de);
  }

  public void add(final DayEntry de) {
    publicHolidays.add(de);
    sql.insertPublicHoliday(de);
    Collections.sort(publicHolidays, new Comparator<DayEntry>() {
      @Override
      public int compare(DayEntry a, DayEntry b) {
        return a.getDay().compareTo(b.getDay());
      }
    });
  }
}
