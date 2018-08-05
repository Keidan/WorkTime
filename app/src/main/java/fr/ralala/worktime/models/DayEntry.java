package fr.ralala.worktime.models;


import android.content.Context;

import java.util.Calendar;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Representation of a day entry
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DayEntry {
  private String mName = "";
  private WorkTimeDay mDay = null;
  private WorkTimeDay mStartMorning = null;
  private WorkTimeDay mEndMorning = null;
  private WorkTimeDay mStartAfternoon = null;
  private WorkTimeDay mEndAfternoon = null;
  private WorkTimeDay mAdditionalBreak = null;
  private WorkTimeDay mRecoveryTime = null;
  private DayType mTypeMorning = DayType.ERROR;
  private DayType mTypeAfternoon = DayType.ERROR;
  private double mAmountByHour = 0.0;
  private int mLearningWeight = 0;
  private boolean mRecurrence = false;
  private WorkTimeDay mLegalWorktime = null;
  private MainApplication mApp = null;

  /**
   * Tests if an object is equal to this instance.
   * @param o The input object.
   * @return boolean
   */
  public boolean equals(Object o) {
    if(o == null || !DayEntry.class.isInstance(o))
      return false;
    if (this == o)
      return true;
    DayEntry de = (DayEntry)o;
    if(mName != null || de.mName != null) {
      if(mName != null && de.mName != null && mName.compareTo(de.mName) != 0)
        return false;
      else if((mName == null && de.mName != null) || (mName != null && de.mName == null))
        return false;
    }
    return (mDay.match(de.mDay) && mStartMorning.match(de.mStartMorning)
      && mEndMorning.match(de.mEndMorning)&& mAdditionalBreak.match(de.mAdditionalBreak)&& mRecoveryTime.match(de.mRecoveryTime) && mStartAfternoon.match(de.mStartAfternoon)
      && mEndAfternoon.match(de.mEndAfternoon)&& mLegalWorktime.match(de.mLegalWorktime) && mTypeMorning == de.mTypeMorning
      && mTypeAfternoon == de.mTypeAfternoon && mAmountByHour == de.mAmountByHour
      && mLearningWeight == de.mLearningWeight&& mRecurrence == de.mRecurrence);
  }

  /**
   * Constructs a new day entry.
   * @param c The Android context.
   * @param day Associated day.
   * @param typeMorning Type of morning.
   * @param typeAfternoon Type of afternoon.
   */
  public DayEntry(final Context c, final WorkTimeDay day, final DayType typeMorning, final DayType typeAfternoon) {
    mDay = new WorkTimeDay();
    mStartMorning = new WorkTimeDay();
    mEndMorning = new WorkTimeDay();
    mAdditionalBreak = new WorkTimeDay();
    mRecoveryTime = new WorkTimeDay();
    mStartAfternoon = new WorkTimeDay();
    mEndAfternoon = new WorkTimeDay();
    mTypeMorning = typeMorning;
    mTypeAfternoon = typeAfternoon;
    mApp = MainApplication.getApp(c);
    mLegalWorktime = mApp.getLegalWorkTimeByDay();
    mDay.copy(day);
  }

  /**
   * Constructs a new day entry.
   * @param c The Android context.
   * @param day Associated day.
   * @param typeMorning Type of morning.
   * @param typeAfternoon Type of afternoon.
   */
  public DayEntry(final Context c, final Calendar day, final DayType typeMorning, final DayType typeAfternoon) {
    this(c, new WorkTimeDay().fromCalendar(day), typeMorning, typeAfternoon);
  }

  /**
   * Returns the work time pay.
   * @return double
   */
  public double getWorkTimePay(double defAmount) {
    double amount = getAmountByHour();
    if(amount == 0 && defAmount != -1) amount = defAmount;
    return amount * Double.parseDouble(
      String.valueOf(getWorkTime().getHours()) + "." + String.valueOf(getWorkTime().getMinutes()));
  }

  /**
   * Returns the pause time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getPause() {
    if(mTypeMorning == DayType.RECOVERY || mTypeAfternoon == DayType.RECOVERY)
      return mAdditionalBreak;
    if(mStartAfternoon.timeString().equals("00:00"))
      return mStartAfternoon;
    WorkTimeDay wp = mStartAfternoon.clone();
    wp.delTime(mEndMorning.clone());
    if(!mAdditionalBreak.timeString().equals("00:00"))
      wp.addTime(mAdditionalBreak);
    return wp;
  }

  /**
   * Returns the work time pay.
   * @return double
   */
  public double getWorkTimePay() {
    return getWorkTimePay(-1);
  }

  /**
   * Returns the over time (in milliseconds).
   * @return long
   */
  public long getOverTimeMs() {
    return getWorkTime().getTimeMs() - getRealLegalWorktimeMS().getTimeMs();
  }

  /**
   * Returns the over time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getOverTime(long diffInMs) {
    return new WorkTimeDay(diffInMs);
  }

  /**
   * Returns the over time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getOverTime() {
    return getOverTime(getOverTimeMs());
  }

  /**
   * Returns the work time value.
   * @return WorkTimeDay
   */
  public WorkTimeDay getWorkTime() {
    WorkTimeDay wm = (getTypeMorning() != DayType.RECOVERY && isValidMorningType()) ? mEndMorning.clone() : new WorkTimeDay();
    if(!wm.timeString().equals("00:00") && isValidMorningType() && getTypeMorning() != DayType.RECOVERY)
      wm.delTime(mStartMorning);
    WorkTimeDay wa = (getTypeAfternoon() != DayType.RECOVERY && isValidAfternoonType()) ? mEndAfternoon.clone() : new WorkTimeDay();
    if(!wa.timeString().equals("00:00") && isValidAfternoonType() && getTypeAfternoon() != DayType.RECOVERY)
      wa.delTime(mStartAfternoon);
    WorkTimeDay wt = wm.clone();
    wt.addTime(wa);
    if(!wt.timeString().equals("00:00") && !mAdditionalBreak.timeString().equals("00:00"))
      wt.delTime(mAdditionalBreak.clone());
    return wt;
  }

  /**
   * Copies another object to this object.
   * @param de The object to copy.
   */
  public void copy(DayEntry de) {
    mLearningWeight = de.mLearningWeight;
    mTypeMorning = de.mTypeMorning;
    mTypeAfternoon = de.mTypeAfternoon;
    mName = de.mName;
    mDay.copy(de.mDay);
    mStartMorning.copy(de.mStartMorning);
    mEndMorning.copy(de.mEndMorning);
    mAdditionalBreak.copy(de.mAdditionalBreak);
    mRecoveryTime.copy(de.mRecoveryTime);
    mStartAfternoon.copy(de.mStartAfternoon);
    mEndAfternoon.copy(de.mEndAfternoon);
    mAmountByHour = de.mAmountByHour;
    mRecurrence = de.mRecurrence;
    mLegalWorktime = de.mLegalWorktime;
  }

  /**
   * Tests if the current instance matches with the current entry.
   * @param de The entry to test.
   * @param testName Test name.
   * @return boolean
   */
  public boolean match(DayEntry de, boolean testName) {
    return !(testName && !mName.equals(de.mName)) && mDay.match(de.mDay) &&
        mStartMorning.match(de.mStartMorning) && mEndMorning.match(de.mEndMorning) &&
        mAdditionalBreak.match(de.mAdditionalBreak) && mRecoveryTime.match(de.mRecoveryTime) && mStartAfternoon.match(de.mStartAfternoon) &&
        mEndAfternoon.match(de.mEndAfternoon) && mTypeMorning == de.mTypeMorning && mTypeAfternoon ==
        de.mTypeAfternoon && mAmountByHour == de.mAmountByHour && mLegalWorktime.match(de.mLegalWorktime);
  }

  /**
   * Tests if the date matches with the current entry.
   * @param current The entry to test.
   * @return boolean
   */
  public boolean matchSimpleDate(WorkTimeDay current) {
    boolean ret = (current.getMonth() == mDay.getMonth() && current.getDay() == mDay.getDay());
    /* simple holidays can change between each years */
    if(!isRecurrence() && ret)
      return current.getYear() == mDay.getYear();
    return ret;
  }

  /**
   * Returns the day string in 2 letters.
   * @param c The Android context.
   * @param dayOfWeek The day of week (See Calendar.MONDAY, etc...)
   * @return String
   */
  public static String getDayString2lt(final Context c, final int dayOfWeek) {
    switch (dayOfWeek) {
      case Calendar.MONDAY: return c.getString(R.string.day_monday_2lt);
      case Calendar.TUESDAY: return c.getString(R.string.day_tuesday_2lt);
      case Calendar.WEDNESDAY: return c.getString(R.string.day_wednesday_2lt);
      case Calendar.THURSDAY: return c.getString(R.string.day_thursday_2lt);
      case Calendar.FRIDAY: return c.getString(R.string.day_friday_2lt);
      case Calendar.SATURDAY: return c.getString(R.string.day_saturday_2lt);
      case Calendar.SUNDAY: return c.getString(R.string.day_sunday_2lt);
    }
    return c.getString(R.string.error);
  }

  /**
   * Parses the input time.
   * @param time The input time.
   * @return WorkTimeDay
   */
  private WorkTimeDay parseTime(String time) {
    if(time == null)
      time = "00:00";
    WorkTimeDay wtd = new WorkTimeDay();
    String split [] = time.split(":");
    wtd.setHours(Integer.parseInt(split[0]));
    wtd.setMinutes(Integer.parseInt(split[1]));
    return wtd;
  }

  /**
   * Parses the input date.
   * @param date The input date.
   * @return WorkTimeDay
   */
  private WorkTimeDay parseDate(String date) {
    WorkTimeDay wtd = new WorkTimeDay();
    String split [] = date.split("/");
    wtd.setDay(Integer.parseInt(split[0]));
    wtd.setMonth(Integer.parseInt(split[1]));
    wtd.setYear(Integer.parseInt(split[2]));
    return wtd;
  }

  /**
   * Returns the start morning time.
   * @param start The new value.
   */
  public void setStartMorning(String start) {
    mStartMorning.copy(parseTime(start));
  }

  /**
   * Returns the start afternoon time.
   * @param start The new value.
   */
  public void setStartAfternoon(String start) {
    mStartAfternoon.copy(parseTime(start));
  }

  /**
   * Sets the end morning time.
   * @param end The new value.
   */
  public void setEndMorning(String end) {
    mEndMorning.copy(parseTime(end));
  }

  /**
   * Sets the end afternoon time.
   * @param end The new value.
   */
  public void setEndAfternoon(String end) {
    mEndAfternoon.copy(parseTime(end));
  }

  /**
   * Sets the end afternoon time.
   * @param end The new value.
   */
  public void setEndAfternoon(WorkTimeDay end) {
    mEndAfternoon.copy(end);
  }

  /**
   * Sets the end morning time.
   * @param end The new value.
   */
  public void setEndMorning(WorkTimeDay end) {
    mEndMorning.copy(end);
  }


  /**
   * Sets the additional break time.
   * @param sbreak The new time in String.
   */
  public void setAdditionalBreak(String sbreak) {
    mAdditionalBreak.copy(parseTime(sbreak));
  }

  /**
   * Returns the additional break time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getAdditionalBreak() {
    return mAdditionalBreak;
  }

  /**
   * Sets the recovery time.
   * @param sbreak The new time in String.
   */
  public void setRecoveryTime(String sbreak) {
    mRecoveryTime.copy(parseTime(sbreak));
  }

  /**
   * Returns the recovery time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getRecoveryTime() {
    return mRecoveryTime;
  }

  /**
   * Sets the day.
   * @param day The new day.
   */
  public void setDay(String day) {
    mDay.copy(parseDate(day));
  }

  /**
   * Returns the day.
   * @return WorkTimeDay
   */
  public WorkTimeDay getDay() {
    return mDay;
  }

  /**
   * Returns the start morning time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getStartMorning() {
    return mStartMorning;
  }
  /**
   * Returns the start afternoon time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getStartAfternoon() {
    return mStartAfternoon;
  }


  /**
   * Tests if the morning type is valid.
   * @return boolean
   */
  public boolean isValidMorningType() {
    return getTypeMorning() != DayType.UNPAID && getTypeMorning() != DayType.ERROR;
  }

  /**
   * Tests if the afternoon type is valid.
   * @return boolean
   */
  public boolean isValidAfternoonType() {
    return getTypeAfternoon() != DayType.UNPAID && getTypeAfternoon() != DayType.ERROR;
  }

  /**
   * Returns the type of the morning time.
   * @return DayType
   */
  public DayType getTypeMorning() {
    return mTypeMorning;
  }

  /**
   * Sets the type of the morning time.
   * @param type The new type.
   */
  public void setTypeMorning(DayType type) {
    mTypeMorning = type;
  }

  /**
   * Returns the type of the afternoon time.
   * @return DayType
   */
  public DayType getTypeAfternoon() {
    return mTypeAfternoon;
  }

  /**
   * Sets the type of the afternoon time.
   * @param type The new type.
   */
  public void setTypeAfternoon(DayType type) {
    mTypeAfternoon = type;
  }

  /**
   * Returns the end morning time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getEndMorning() {
    return mEndMorning;
  }

  /**
   * Returns the end afternoon time.
   * @return WorkTimeDay
   */
  public WorkTimeDay getEndAfternoon() {
    return mEndAfternoon;
  }

  /**
   * Returns the name (if used with profile or public holidays.
   * @return String
   */
  public String getName() {
    return mName;
  }

  /**
   * Sets the name (if used with profile or public holidays)
   * @param name The new value.
   */
  public void setName(String name) {
    mName = name;
  }

  /**
   * Returns the amount by hour.
   * @return double
   */
  public double getAmountByHour() {
    return mAmountByHour;
  }

  /**
   * Sets the amount by hour.
   * @param amountByHour The new value.
   */
  public void setAmountByHour(double amountByHour) {
    mAmountByHour = amountByHour;
  }

  /**
   * Returns the learning weight.
   * @return int
   */
  public int getLearningWeight() {
    return mLearningWeight;
  }

  /**
   * Sets the learning weight.
   * @param learningWeight The new value.
   */
  public void setLearningWeight(int learningWeight) {
    mLearningWeight = learningWeight;
  }

  /**
   * Tests if the recurrence state is enabled.
   * @return boolean
   */
  public boolean isRecurrence() {
    return mRecurrence;
  }

  /**
   * Sets the recurrence state.
   * @param recurrence The new state.
   */
  public void setRecurrence(boolean recurrence) {
    mRecurrence = recurrence;
  }

  /**
   * Returns the legal work time value.
   * @return WorkTimeDay
   */
  public WorkTimeDay getLegalWorktime() {
    if(mLegalWorktime.timeString().equals("00:00"))
      mLegalWorktime = mApp.getLegalWorkTimeByDay();
    return mLegalWorktime;
  }


  /**
   * Returns the legal work time value adjusted with the recovery time.
   * @return WorkTimeDay
   */
  private WorkTimeDay getRealLegalWorktimeMS() {
    WorkTimeDay wmLegal = getLegalWorktime();
    WorkTimeDay wm = new WorkTimeDay();
    if(!wmLegal.timeString().equals("00:00"))
      wm.addTime(wmLegal);
    if(!wm.timeString().equals("00:00") && !mRecoveryTime.timeString().equals("00:00"))
      wm.delTime(mRecoveryTime.clone());
    return wm;
  }

  /**
   * Sets the legal work time from a string value.
   * @param legalWorktime The new value.
   */
  public void setLegalWorktime(String legalWorktime) {
    mLegalWorktime.copy(parseTime(legalWorktime));
  }
}
