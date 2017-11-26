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
  private DayType mTypeMorning = DayType.ERROR;
  private DayType mTypeAfternoon = DayType.ERROR;
  private double mAmountByHour = 0.0;
  private int mLearningWeight = 0;
  private boolean mRecurrence = false;
  private WorkTimeDay mLegalWorktime = null;
  private MainApplication mApp = null;

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
      && mEndMorning.match(de.mEndMorning)&& mAdditionalBreak.match(de.mAdditionalBreak) && mStartAfternoon.match(de.mStartAfternoon)
      && mEndAfternoon.match(de.mEndAfternoon)&& mLegalWorktime.match(de.mLegalWorktime) && mTypeMorning == de.mTypeMorning
      && mTypeAfternoon == de.mTypeAfternoon && mAmountByHour == de.mAmountByHour
      && mLearningWeight == de.mLearningWeight&& mRecurrence == de.mRecurrence);
  }

  public DayEntry(final Context c, final WorkTimeDay day, final DayType typeMorning, final DayType typeAfternoon) {
    mDay = new WorkTimeDay();
    mStartMorning = new WorkTimeDay();
    mEndMorning = new WorkTimeDay();
    mAdditionalBreak = new WorkTimeDay();
    mStartAfternoon = new WorkTimeDay();
    mEndAfternoon = new WorkTimeDay();
    mTypeMorning = typeMorning;
    mTypeAfternoon = typeAfternoon;
    mApp = MainApplication.getApp(c);
    mLegalWorktime = mApp.getLegalWorkTimeByDay();
    mDay.copy(day);
  }

  public DayEntry(final Context c, final Calendar day, final DayType typeMorning, final DayType typeAfternoon) {
    this(c, new WorkTimeDay().fromCalendar(day), typeMorning, typeAfternoon);
  }

  public double getWorkTimePay(double defAmount) {
    double amount = getAmountByHour();
    if(amount == 0 && defAmount != -1) amount = defAmount;
    return amount * Double.parseDouble(
      String.valueOf(getWorkTime().getHours()) + "." + String.valueOf(getWorkTime().getMinutes()));
  }

  public WorkTimeDay getPause() {
    if(mStartAfternoon.timeString().equals("00:00"))
      return mStartAfternoon;
    WorkTimeDay wp = mStartAfternoon.clone();
    wp.delTime(mEndMorning.clone());
    if(!mAdditionalBreak.timeString().equals("00:00"))
      wp.addTime(mAdditionalBreak);
    return wp;
  }

  public double getWorkTimePay() {
    return getWorkTimePay(-1);
  }

  public long getOverTimeMs() {
    return getWorkTime().getTimeMs() - getLegalWorktime().getTimeMs();
  }

  public WorkTimeDay getOverTime(long diffInMs) {
    return new WorkTimeDay(diffInMs);
  }

  public WorkTimeDay getOverTime() {
    return getOverTime(getOverTimeMs());
  }

  public WorkTimeDay getWorkTime() {
    WorkTimeDay wm = isValidMorningType() ? mEndMorning.clone() : new WorkTimeDay();
    if(!wm.timeString().equals("00:00") && isValidMorningType())
      wm.delTime(mStartMorning);
    WorkTimeDay wa = isValidAfternoonType() ? mEndAfternoon.clone() : new WorkTimeDay();
    if(!wa.timeString().equals("00:00") && isValidAfternoonType())
      wa.delTime(mStartAfternoon);
    WorkTimeDay wt = wm.clone();
    wt.addTime(wa);
    if(!wt.timeString().equals("00:00") && !mAdditionalBreak.timeString().equals("00:00"))
      wt.delTime(mAdditionalBreak.clone());
    return wt;
  }

  public void copy(DayEntry de) {
    mLearningWeight = de.mLearningWeight;
    mTypeMorning = de.mTypeMorning;
    mTypeAfternoon = de.mTypeAfternoon;
    mName = de.mName;
    mDay.copy(de.mDay);
    mStartMorning.copy(de.mStartMorning);
    mEndMorning.copy(de.mEndMorning);
    mAdditionalBreak.copy(de.mAdditionalBreak);
    mStartAfternoon.copy(de.mStartAfternoon);
    mEndAfternoon.copy(de.mEndAfternoon);
    mAmountByHour = de.mAmountByHour;
    mRecurrence = de.mRecurrence;
    mLegalWorktime = de.mLegalWorktime;
  }

  public boolean match(DayEntry de) {
    return mDay.match(de.mDay)  && mStartMorning.match(de.mStartMorning) && mEndMorning.match(de.mEndMorning) && mAdditionalBreak.match(de.mAdditionalBreak)
      && mStartAfternoon.match(de.mStartAfternoon) && mEndAfternoon.match(de.mEndAfternoon) &&
        mTypeMorning == de.mTypeMorning && mTypeAfternoon == de.mTypeAfternoon && mAmountByHour == de.mAmountByHour && mLegalWorktime.match(de.mLegalWorktime);
  }

  public boolean matchSimpleDate(WorkTimeDay current) {
    boolean ret = (current.getMonth() == mDay.getMonth() && current.getDay() == mDay.getDay());
    /* simple holidays can change between each years */
    if(!isRecurrence() && ret && mTypeMorning == DayType.HOLIDAY && mTypeAfternoon == DayType.HOLIDAY)
      return current.getYear() == mDay.getYear();
    return ret;
  }

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

  private WorkTimeDay parseTime(String time) {
    if(time == null)
      time = "00:00";
    WorkTimeDay wtd = new WorkTimeDay();
    String split [] = time.split(":");
    wtd.setHours(Integer.parseInt(split[0]));
    wtd.setMinutes(Integer.parseInt(split[1]));
    return wtd;
  }

  private WorkTimeDay parseDate(String time) {
    WorkTimeDay wtd = new WorkTimeDay();
    String split [] = time.split("/");
    wtd.setDay(Integer.parseInt(split[0]));
    wtd.setMonth(Integer.parseInt(split[1]));
    wtd.setYear(Integer.parseInt(split[2]));
    return wtd;
  }

  public void setStartMorning(String start) {
    mStartMorning.copy(parseTime(start));
  }
  public void setStartAfternoon(String start) {
    mStartAfternoon.copy(parseTime(start));
  }

  public void setEndMorning(String end) {
    mEndMorning.copy(parseTime(end));
  }
  public void setEndAfternoon(String end) {
    mEndAfternoon.copy(parseTime(end));
  }
  public void setEndAfternoon(WorkTimeDay end) {
    mEndAfternoon.copy(end);
  }
  public void setEndMorning(WorkTimeDay end) {
    mEndMorning.copy(end);
  }


  public void setAdditionalBreak(String sbreak) {
    mAdditionalBreak.copy(parseTime(sbreak));
  }
  public WorkTimeDay getAdditionalBreak() {
    return mAdditionalBreak;
  }

  public void setDay(String day) {
    mDay.copy(parseDate(day));
  }
  public WorkTimeDay getDay() {
    return mDay;
  }

  public WorkTimeDay getStartMorning() {
    return mStartMorning;
  }
  public WorkTimeDay getStartAfternoon() {
    return mStartAfternoon;
  }


  public boolean isValidMorningType() {
    return getTypeMorning() != DayType.UNPAID && getTypeMorning() != DayType.ERROR;
  }
  public boolean isValidAfternoonType() {
    return getTypeAfternoon() != DayType.UNPAID && getTypeAfternoon() != DayType.ERROR;
  }

  public DayType getTypeMorning() {
    return mTypeMorning;
  }

  public void setTypeMorning(DayType type) {
    mTypeMorning = type;
  }

  public DayType getTypeAfternoon() {
    return mTypeAfternoon;
  }

  public void setTypeAfternoon(DayType type) {
    mTypeAfternoon = type;
  }

  public WorkTimeDay getEndMorning() {
    return mEndMorning;
  }
  public WorkTimeDay getEndAfternoon() {
    return mEndAfternoon;
  }

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }

  public double getAmountByHour() {
    return mAmountByHour;
  }

  public void setAmountByHour(double amountByHour) {
    mAmountByHour = amountByHour;
  }
  public int getLearningWeight() {
    return mLearningWeight;
  }

  public void setLearningWeight(int learningWeight) {
    mLearningWeight = learningWeight;
  }

  public boolean isRecurrence() {
    return mRecurrence;
  }

  public void setRecurrence(boolean recurrence) {
    mRecurrence = recurrence;
  }

  public WorkTimeDay getLegalWorktime() {
    if(mLegalWorktime.timeString().equals("00:00"))
      mLegalWorktime = mApp.getLegalWorkTimeByDay();
    return mLegalWorktime;
  }

  public void setLegalWorktime(String legalWorktime) {
    mLegalWorktime.copy(parseTime(legalWorktime));
  }
}
