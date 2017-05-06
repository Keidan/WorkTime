package fr.ralala.worktime.factories;


import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    sort();
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
    sort();
  }

  private void sort() {
    Collections.sort(publicHolidays,  SortComparator.comparator(
      SortComparator.getComparator(SortComparator.SORT_BY_RECURRENCE,
        SortComparator.SORT_BY_DATE)));
  }
  private enum  SortComparator implements Comparator<DayEntry> {
    SORT_BY_RECURRENCE {
      public int compare(DayEntry a, DayEntry b) {
        return Boolean.compare(a.isRecurrence(), b.isRecurrence());
      }
    },
    SORT_BY_DATE {
      public int compare(DayEntry a, DayEntry b) {
        WorkTimeDay a_wtd = a.getDay();
        WorkTimeDay b_wtd = b.getDay();
        String a_date = String.format(Locale.US, "%02d/%02d/%04d", a_wtd.getDay(), a_wtd.getMonth(), (!a.isRecurrence() ? a_wtd.getYear() : 1900));
        String b_date = String.format(Locale.US, "%02d/%02d/%04d", b_wtd.getDay(), b_wtd.getMonth(), (!b.isRecurrence() ? b_wtd.getYear() : 1900));
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        try {
          Date date1 = format.parse(a_date);
          Date date2 = format.parse(b_date);
          return date2.compareTo(date1);
        } catch(Exception e) {
          Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
          return -1;
        }
      }
    },;

    public static Comparator<DayEntry> comparator(final Comparator<DayEntry> other) {
      return new Comparator<DayEntry>() {
        public int compare(DayEntry o1, DayEntry o2) {
          return -1 * other.compare(o1, o2);
        }
      };
    }

    public static Comparator<DayEntry> getComparator(final SortComparator... multipleOptions) {
      return new Comparator<DayEntry>() {
        public int compare(DayEntry o1, DayEntry o2) {
          for (SortComparator option : multipleOptions) {
            int result = option.compare(o1, o2);
            if (result != 0) {
              return result;
            }
          }
          return 0;
        }
      };
    }
  }

}
