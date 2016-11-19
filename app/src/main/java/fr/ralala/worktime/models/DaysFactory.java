package fr.ralala.worktime.models;




import java.util.ArrayList;
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
