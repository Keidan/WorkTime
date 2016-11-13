package fr.ralala.worktime.models;



import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

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

  public boolean isHolidays(Calendar currentDate) {
    WorkTimeDay wtd = new WorkTimeDay();
    for(DayEntry de : days) {
      wtd.fromCalendar(currentDate);
      if((de.getType() == DayType.HOLIDAY) && de.matchSimpleDate(wtd))
        return true;
    }
    return false;
  }

  public double checkForDayDateAndCopy(DayEntry current) {
    for(DayEntry de : days) {
      if(de.getDay().dateString().equals(current.getDay().dateString())) {
        current.copy(de);
        // Calculate Pay
        return de.getAmountByHour() * Double.parseDouble(
          String.valueOf(de.getWorkTime().getHours()) + "." + String.valueOf(de.getWorkTime().getMinutes()));
      }
    }
    return 0.0;
  }

  public String totalWorkTime(DayEntry current) {
    for(DayEntry de : days) {
      if(de.getDay().dateString().equals(current.getDay().dateString()))
        return de.getWorkTime().timeString();
    }
    return "00:00";
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
