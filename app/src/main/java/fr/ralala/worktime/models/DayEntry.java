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
  private String name = "";
  private WorkTimeDay day = null;
  private WorkTimeDay startMorning = null;
  private WorkTimeDay endMorning = null;
  private WorkTimeDay startAfternoon = null;
  private WorkTimeDay endAfternoon = null;
  private WorkTimeDay additionalBreak = null;
  private DayType typeMorning = DayType.ERROR;
  private DayType typeAfternoon = DayType.ERROR;
  private double amountByHour = 0.0;
  private int learningWeight = 0;
  private boolean recurrence = false;
  private WorkTimeDay legalWorktime = null;
  private Context context = null;

  public boolean equals(Object o) {
    if(o == null || !DayEntry.class.isInstance(o))
      return false;
    if (this == o)
      return true;
    DayEntry de = (DayEntry)o;
    if(name != null || de.name != null) {
      if(name != null && de.name != null && name.compareTo(de.name) != 0)
        return false;
      else if((name == null && de.name != null) || (name != null && de.name == null))
        return false;
    }
    return (day.match(de.day) && startMorning.match(de.startMorning)
      && endMorning.match(de.endMorning)&& additionalBreak.match(de.additionalBreak) && startAfternoon.match(de.startAfternoon)
      && endAfternoon.match(de.endAfternoon)&& legalWorktime.match(de.legalWorktime) && typeMorning == de.typeMorning
      && typeAfternoon == de.typeAfternoon && amountByHour == de.amountByHour
      && learningWeight == de.learningWeight&& recurrence == de.recurrence);
  }

  public DayEntry(final Context c, final WorkTimeDay day, final DayType typeMorning, final DayType typeAfternoon) {
    this.context = c;
    this.day = new WorkTimeDay();
    this.startMorning = new WorkTimeDay();
    this.endMorning = new WorkTimeDay();
    this.additionalBreak = new WorkTimeDay();
    this.startAfternoon = new WorkTimeDay();
    this.endAfternoon = new WorkTimeDay();
    this.typeMorning = typeMorning;
    this.typeAfternoon = typeAfternoon;
    this.legalWorktime = MainApplication.getApp(c).getLegalWorkTimeByDay();
    this.day.copy(day);
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
    if(startAfternoon.timeString().equals("00:00"))
      return startAfternoon;
    WorkTimeDay wp = startAfternoon.clone();
    wp.delTime(endMorning.clone());
    if(!additionalBreak.timeString().equals("00:00"))
      wp.addTime(additionalBreak);
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
    WorkTimeDay wm = isValidMorningType() ? endMorning.clone() : new WorkTimeDay();
    if(!wm.timeString().equals("00:00") && isValidMorningType())
      wm.delTime(startMorning);
    WorkTimeDay wa = isValidAfternoonType() ? endAfternoon.clone() : new WorkTimeDay();
    if(!wa.timeString().equals("00:00") && isValidAfternoonType())
      wa.delTime(startAfternoon);
    WorkTimeDay wt = wm.clone();
    wt.addTime(wa);
    if(!wt.timeString().equals("00:00") && !additionalBreak.timeString().equals("00:00"))
      wt.delTime(additionalBreak.clone());
    return wt;
  }

  public void copy(DayEntry de) {
    context = de.context;
    learningWeight = de.learningWeight;
    typeMorning = de.typeMorning;
    typeAfternoon = de.typeAfternoon;
    name = de.name;
    day.copy(de.day);
    startMorning.copy(de.startMorning);
    endMorning.copy(de.endMorning);
    additionalBreak.copy(de.additionalBreak);
    startAfternoon.copy(de.startAfternoon);
    endAfternoon.copy(de.endAfternoon);
    amountByHour = de.amountByHour;
    recurrence = de.recurrence;
    legalWorktime = de.legalWorktime;
  }

  public boolean match(DayEntry de) {
    return day.match(de.day)  && startMorning.match(de.startMorning) && endMorning.match(de.endMorning) && additionalBreak.match(de.additionalBreak)
      && startAfternoon.match(de.startAfternoon) && endAfternoon.match(de.endAfternoon) &&
      typeMorning == de.typeMorning && typeAfternoon == de.typeAfternoon && amountByHour == de.amountByHour && legalWorktime.match(de.legalWorktime);
  }

  public boolean matchSimpleDate(WorkTimeDay current) {
    boolean ret = (current.getMonth() == day.getMonth() && current.getDay() == day.getDay());
    /* simple holidays can change between each years */
    if(!isRecurrence() && ret && typeMorning == DayType.HOLIDAY && typeAfternoon == DayType.HOLIDAY)
      return current.getYear() == day.getYear();
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
    this.startMorning.copy(parseTime(start));
  }
  public void setStartAfternoon(String start) {
    this.startAfternoon.copy(parseTime(start));
  }

  public void setEndMorning(String end) {
    this.endMorning.copy(parseTime(end));
  }
  public void setEndAfternoon(String end) {
    this.endAfternoon.copy(parseTime(end));
  }
  public void setEndAfternoon(WorkTimeDay end) {
    this.endAfternoon.copy(end);
  }
  public void setEndMorning(WorkTimeDay end) {
    this.endMorning.copy(end);
  }


  public void setAdditionalBreak(String sbreak) {
    this.additionalBreak.copy(parseTime(sbreak));
  }
  public WorkTimeDay getAdditionalBreak() {
    return additionalBreak;
  }

  public void setDay(String day) {
    this.day.copy(parseDate(day));
  }
  public WorkTimeDay getDay() {
    return day;
  }

  public WorkTimeDay getStartMorning() {
    return startMorning;
  }
  public WorkTimeDay getStartAfternoon() {
    return startAfternoon;
  }


  public boolean isValidMorningType() {
    return getTypeMorning() != DayType.UNPAID && getTypeMorning() != DayType.ERROR;
  }
  public boolean isValidAfternoonType() {
    return getTypeAfternoon() != DayType.UNPAID && getTypeAfternoon() != DayType.ERROR;
  }

  public DayType getTypeMorning() {
    return typeMorning;
  }

  public void setTypeMorning(DayType type) {
    this.typeMorning = type;
  }

  public DayType getTypeAfternoon() {
    return typeAfternoon;
  }

  public void setTypeAfternoon(DayType type) {
    this.typeAfternoon = type;
  }

  public WorkTimeDay getEndMorning() {
    return endMorning;
  }
  public WorkTimeDay getEndAfternoon() {
    return endAfternoon;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public double getAmountByHour() {
    return amountByHour;
  }

  public void setAmountByHour(double amountByHour) {
    this.amountByHour = amountByHour;
  }
  public int getLearningWeight() {
    return learningWeight;
  }

  public void setLearningWeight(int learningWeight) {
    this.learningWeight = learningWeight;
  }

  public boolean isRecurrence() {
    return recurrence;
  }

  public void setRecurrence(boolean recurrence) {
    this.recurrence = recurrence;
  }

  public WorkTimeDay getLegalWorktime() {
    if(legalWorktime.timeString().equals("00:00"))
      legalWorktime = MainApplication.getApp(context).getLegalWorkTimeByDay();
    return legalWorktime;
  }

  public void setLegalWorktime(String legalWorktime) {
    this.legalWorktime.copy(parseTime(legalWorktime));
  }
}
