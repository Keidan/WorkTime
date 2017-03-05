package fr.ralala.worktime.models;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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
