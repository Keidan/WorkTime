package fr.ralala.worktime.factories;


import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.Calendar;
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
  private final List<DayEntry> mDays;
  private SqlFactory mSql = null;

  /**
   * Creates the factory.
   */
  public DaysFactory() {
    mDays = new ArrayList<>();
  }

  /**
   * Reloads the entries from the SQLite databases.
   * @param sql The SQLite factory.
   */
  public void reload(final SqlFactory sql) {
    mSql = sql;
    mDays.clear();
    mDays.addAll(sql.getDays());
  }

  /**
   * Returns the list of days.
   * @return List<DayEntry>
   */
  public List<DayEntry> list() {
    return mDays;
  }


  /**
   * Returns a list formatted as follow:
   * Map<YEAR, Map<MONTH, Map<WEEK, List<DayEntry>>>>
   * @return <code>Map<YEAR, Map<MONTH, Map<WEEK, List<DayEntry>>>></code>
   */
  @SuppressLint("UseSparseArrays")
  public Map<Integer, Map<Integer, Map<Integer, List<DayEntry>>>> getDays() {
    Map<Integer, Map<Integer, Map<Integer, List<DayEntry>>>> list = new HashMap<>();
    for(DayEntry de : mDays) {
      Integer y = de.getDay().getYear();
      Integer m = de.getDay().getMonth();
      int w = de.getDay().toCalendar().get(Calendar.WEEK_OF_YEAR);
      if(!list.containsKey(y))
        list.put(y, new HashMap<>());
      if(!list.get(y).containsKey(m))
        list.get(y).put(m, new HashMap<>());
      if(!list.get(y).get(m).containsKey(w))
        list.get(y).get(m).put(w, new ArrayList<>());
      list.get(y).get(m).get(w).add(de);
    }
    return list;
  }

  /**
   * Returns the list of work time day of a week.
   * @param map The main list.
   * @param week The associated week.
   * @param month The week month.
   * @param year The week year.
   * @return WorkTimeDay
   */
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
      if(de != null && (de.isValidMorningType() && de.isValidAfternoonType()) && week == ctime.get(Calendar.WEEK_OF_YEAR)) {
        ret.addTime(de.getWorkTime());
      }
    }
    return ret;
  }

  /**
   * Returns the current day.
   * @return DayEntry
   */
  public DayEntry getCurrentDay() {
    for(DayEntry d : mDays)
      if(d.getDay().dateString().equals(WorkTimeDay.now().dateString())) {
        return d;
      }
    DayEntry d = new DayEntry(mSql.getContext(), WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
    mDays.add(d);
    return d;
  }

  /**
   * Converts days to a map.
   * @return Map<String, DayEntry>
   */
  public Map<String, DayEntry> toDaysMap() {
    Map<String, DayEntry> map = new HashMap<>();
    for(DayEntry de : mDays) map.put(de.getDay().dateString(), de);
    return map;
  }

  /**
   * Copies the current entry and calculate the pay.
   * @param current The current entry.
   */
  public void checkForDayDateAndCopy(DayEntry current) {
    for(DayEntry de : mDays) {
      if(de.getDay().dateString().equals(current.getDay().dateString())) {
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
    for(int i = 0; i < mDays.size(); i++)
      if(de.match(mDays.get(i))) {
        mDays.remove(i);
        break;
      }
    mSql.removeDay(de);
  }

  /**
   * Adds a new entry.
   * @param de The entry to add.
   */
  public void add(final DayEntry de) {
    mDays.add(de);
    mSql.insertDay(de);
    Comparator<DayEntry> comp = (a, b) -> a.getDay().compareTo(b.getDay());
    mDays.sort(comp);
  }
}
