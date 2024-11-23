package fr.ralala.worktime.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import fr.ralala.worktime.models.PublicHolidayEntry;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Simple sort comparator for the public holidays.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public enum PublicHolidayEntrySortComparator implements Comparator<PublicHolidayEntry> {
  SORT_BY_RECURRENCE {
    public int compare(PublicHolidayEntry a, PublicHolidayEntry b) {
      return Boolean.compare(a.isRecurrence(), b.isRecurrence());
    }
  },
  SORT_BY_DATE {
    public int compare(PublicHolidayEntry a, PublicHolidayEntry b) {
      WorkTimeDay aWTD = a.getDay();
      WorkTimeDay bWTD = b.getDay();
      String aDate = String.format(Locale.US, "%02d/%02d/%04d", aWTD.getDay(), aWTD.getMonth(), (!a.isRecurrence() ? aWTD.getYear() : 1900));
      String bDate = String.format(Locale.US, "%02d/%02d/%04d", bWTD.getDay(), bWTD.getMonth(), (!b.isRecurrence() ? bWTD.getYear() : 1900));
      SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
      try {
        Date date1 = format.parse(aDate);
        Date date2 = format.parse(bDate);
        if (date1 == null || date2 == null)
          return -1;
        return date2.compareTo(date1);
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
        return -1;
      }
    }
  },
  ;

  /**
   * Compares entry.
   *
   * @param other The comparator entry.
   * @return Comparator<PublicHolidayEntry>
   */
  public static Comparator<PublicHolidayEntry> comparator(final Comparator<PublicHolidayEntry> other) {
    return (o1, o2) -> -1 * other.compare(o1, o2);
  }

  /**
   * Returns the comparate to use.
   *
   * @param multipleOptions Multiple comparator.
   * @return Comparator<DayEntry>
   */
  public static Comparator<PublicHolidayEntry> getComparator(final PublicHolidayEntrySortComparator... multipleOptions) {
    return (o1, o2) -> {
      for (PublicHolidayEntrySortComparator option : multipleOptions) {
        int result = option.compare(o1, o2);
        if (result != 0) {
          return result;
        }
      }
      return 0;
    };
  }
}