package fr.ralala.worktime.models;

import android.content.Context;

import fr.ralala.worktime.R;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Type of day
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public enum DayType {
  AT_WORK(0),
  HOLIDAY(1),
  PUBLIC_HOLIDAY(2),
  SICKNESS(3),
  UNPAID(4),
  ERROR(5);

  DayType(final int value) {
    this.value = value;
  }

  private final int value;

  public int value() {
    return value;
  }


  public static DayType compute(int value) {
    switch (value) {
      case 0: return DayType.AT_WORK;
      case 1: return DayType.HOLIDAY;
      case 2: return DayType.PUBLIC_HOLIDAY;
      case 3: return DayType.SICKNESS;
      case 4: return DayType.UNPAID;
      default: return DayType.ERROR;
    }
  }

  public static DayType compute(final Context c, String value) {
    if(value.equals(c.getString(R.string.at_work)))
      return DayType.AT_WORK;
    else if(value.equals(c.getString(R.string.holidays)))
      return DayType.HOLIDAY;
    else if(value.equals(c.getString(R.string.public_holidays)))
      return DayType.PUBLIC_HOLIDAY;
    else if(value.equals(c.getString(R.string.sickness)))
      return DayType.SICKNESS;
    else if(value.equals(c.getString(R.string.unpaid)))
      return DayType.UNPAID;
    else
      return DayType.ERROR;
  }

  public String string(final Context c) {
    switch (this) {
      case AT_WORK: return c.getString(R.string.at_work);
      case HOLIDAY: return c.getString(R.string.holidays);
      case PUBLIC_HOLIDAY: return c.getString(R.string.public_holidays);
      case SICKNESS: return c.getString(R.string.sickness);
      case UNPAID: return c.getString(R.string.unpaid);
    }
    return c.getString(R.string.error);
  }

  public static String getText(final Context c, final DayType dt) {
    return dt.string(c);
  }
}
