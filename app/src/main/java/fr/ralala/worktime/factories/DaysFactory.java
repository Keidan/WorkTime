package fr.ralala.worktime.factories;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
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

  public WorkTimeDay getWorkTimeDayFromWeek(Map<String, DayEntry> map, int week, int month, int year) {
    WorkTimeDay ret = new WorkTimeDay();
    Calendar ctime = Calendar.getInstance();
    ctime.setTimeZone(TimeZone.getTimeZone("GMT"));
    ctime.setFirstDayOfWeek(Calendar.MONDAY);
    ctime.set(Calendar.YEAR, year);
    ctime.set(Calendar.MONTH, month);
    ctime.set(Calendar.DAY_OF_MONTH, 1);
    int maxDay = ctime.getMaximum(Calendar.DATE);
    for(int day = 1; day <= maxDay; ++day) {
      ctime.set(Calendar.DAY_OF_MONTH, day);
      DayEntry de = map.get(String.format(Locale.US, "%02d/%02d/%04d", ctime.get(Calendar.DAY_OF_MONTH), ctime.get(Calendar.MONTH) + 1, ctime.get(Calendar.YEAR)));
      if(de != null && (de.getTypeMorning() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.AT_WORK) && week == ctime.get(Calendar.WEEK_OF_YEAR)) {
        ret.addTime(de.getWorkTime());
      }
    }
    return ret;
  }

  public DayEntry getCurrentDay() {
    for(DayEntry d : days)
      if(d.getDay().dateString().equals(WorkTimeDay.now().dateString())) {
        return d;
      }
    DayEntry d = new DayEntry(WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
    days.add(d);
    return d;
  }

  public Map<String, DayEntry> toDaysMap() {
    Map<String, DayEntry> map = new HashMap<>();
    for(DayEntry de : days) map.put(de.getDay().dateString(), de);
    return map;
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
