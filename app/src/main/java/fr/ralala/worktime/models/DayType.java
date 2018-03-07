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
  OFF(5),
  ERROR(6);

  /**
   * Creates an enum entry.
   * @param value The int value.
   */
  DayType(final int value) {
    mValue = value;
  }

  private final int mValue;

  /**
   * Returns the associated value.
   * @return int
   */
  public int value() {
    return mValue;
  }


  /**
   * Converts an integer value to enum value.
   * @param value The integer value.
   * @return DayType
   */
  public static DayType compute(int value) {
    switch (value) {
      case 0: return DayType.AT_WORK;
      case 1: return DayType.HOLIDAY;
      case 2: return DayType.PUBLIC_HOLIDAY;
      case 3: return DayType.SICKNESS;
      case 4: return DayType.UNPAID;
      case 5: return DayType.OFF;
      default: return DayType.ERROR;
    }
  }

  /**
   * Converts a String value to enum value.
   * @param value The String value.
   * @return DayType
   */
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
    else if(value.equals(c.getString(R.string.off)))
      return DayType.OFF;
    else
      return DayType.ERROR;
  }

  /**
   * Converts the enum to a String value.
   * @param c Android context.
   * @return String
   */
  public String string(final Context c) {
    switch (this) {
      case AT_WORK: return c.getString(R.string.at_work);
      case HOLIDAY: return c.getString(R.string.holidays);
      case PUBLIC_HOLIDAY: return c.getString(R.string.public_holidays);
      case SICKNESS: return c.getString(R.string.sickness);
      case UNPAID: return c.getString(R.string.unpaid);
      case OFF: return c.getString(R.string.off);
    }
    return c.getString(R.string.error);
  }

  /**
   * Returns the String value of the specified enum.
   * @param c The Android context.
   * @param dt The day type to convert.
   * @return String.
   */
  public static String getText(final Context c, final DayType dt) {
    return dt.string(c);
  }
}
