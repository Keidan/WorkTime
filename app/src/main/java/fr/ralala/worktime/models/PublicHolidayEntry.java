package fr.ralala.worktime.models;

import android.content.Context;

import fr.ralala.worktime.sql.SqlConstants;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Representation of a public holiday entry
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PublicHolidayEntry {
  private String mName = "";
  private final WorkTimeDay mDay;
  private final DayType mTypeMorning;
  private final DayType mTypeAfternoon;
  private boolean mRecurrence = false;
  private long mId = SqlConstants.INVALID_ID;
  private final Context mCtx;

  /**
   * Constructs a new day entry.
   *
   * @param day           Associated day.
   * @param typeMorning   Type of morning.
   * @param typeAfternoon Type of afternoon.
   */
  public PublicHolidayEntry(Context ctx, final WorkTimeDay day, final DayType typeMorning, final DayType typeAfternoon) {
    mCtx = ctx;
    mDay = new WorkTimeDay();
    mTypeMorning = typeMorning;
    mTypeAfternoon = typeAfternoon;
    mDay.copy(day);
  }

  /**
   * Converts the PublicHolidayEntry to DayEntry.
   *
   * @return DayEntry
   */
  public DayEntry toDayEntry() {
    return new DayEntry(mCtx, mDay, mTypeMorning, mTypeAfternoon);
  }

  /**
   * Sets the DB id.
   *
   * @param id The new ID.
   */
  public void setID(long id) {
    mId = id;
  }

  /**
   * Returns the DB id.
   *
   * @return long
   */
  public long getID() {
    return mId;
  }

  /**
   * Tests if the date matches with the current entry.
   *
   * @param current The entry to test.
   * @return boolean
   */
  public boolean matchSimpleDate(WorkTimeDay current) {
    boolean ret = (current.getMonth() == mDay.getMonth() && current.getDay() == mDay.getDay());
    /* simple holidays can change between each years */
    if (!isRecurrence() && ret)
      return current.getYear() == mDay.getYear();
    return ret;
  }

  /**
   * Returns the day.
   *
   * @return WorkTimeDay
   */
  public WorkTimeDay getDay() {
    return mDay;
  }

  /**
   * Returns the type of the morning time.
   *
   * @return DayType
   */
  public DayType getTypeMorning() {
    return mTypeMorning;
  }

  /**
   * Returns the type of the afternoon time.
   *
   * @return DayType
   */
  public DayType getTypeAfternoon() {
    return mTypeAfternoon;
  }

  /**
   * Returns the name (if used with profile or public holidays.
   *
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Sets the name (if used with profile or public holidays)
   *
   * @param name The new value.
   */
  public void setName(String name) {
    mName = name;
  }

  /**
   * Tests if the recurrence state is enabled.
   *
   * @return boolean
   */
  public boolean isRecurrence() {
    return mRecurrence;
  }

  /**
   * Sets the recurrence state.
   *
   * @param recurrence The new state.
   */
  public void setRecurrence(boolean recurrence) {
    mRecurrence = recurrence;
  }

}
