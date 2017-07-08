package fr.ralala.worktime.quickaccess;


import java.util.Locale;
import java.util.TimerTask;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the quick access service task
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class QuickAccessServiceTask extends TimerTask{
  private MainApplication app = null;
  private int seconds = 0;
  private String textLabel = "";

  public QuickAccessServiceTask(MainApplication app) {
    this.app = app;
    textLabel = app.getString(R.string.work_time) + ": ";
  }

  @Override
  public void run() {
    DayEntry de = app.getDaysFactory().getCurrentDay();
    WorkTimeDay d = de.getDay();
    DayEntry wtd = app.getProfilesFactory().getHighestLearningWeight();
    long time = 0;
    if(wtd != null) {
      WorkTimeDay wem = wtd.getEndMorning();
      time = wem.getTimeMs();
    } else
      time = new WorkTimeDay(0, 0, 0, 12, 0).getTimeMs();

    if(d.getTimeMs() < time) {
      /* morning */
      if(de.getStartMorning().timeString().equals("00:00")) {
        de.setStartMorning(d.timeString());
        de.setTypeMorning(DayType.AT_WORK);
        de.setEndMorning(d.timeString());
      }
      de.setEndMorning(update(de.getEndMorning()));
      WorkTimeDay w = de.getWorkTime();
      String text = textLabel + String.format(Locale.US, "%02d:%02d:%02d", w.getHours(), w.getMinutes(), seconds);
      app.getQuickAccessNotification().update(text, app.isQuickAccessPause());
    } else {
      /* afternoon */
      if(de.getStartAfternoon().timeString().equals("00:00")) {
        de.setStartAfternoon(d.timeString());
        de.setTypeAfternoon(DayType.AT_WORK);
        de.setEndAfternoon(d.timeString());
      }
      de.setEndAfternoon(update(de.getEndAfternoon()));
      WorkTimeDay w = de.getWorkTime();
      String text = textLabel + String.format(Locale.US, "%02d:%02d:%02d", w.getHours(), w.getMinutes(), seconds);
      app.getQuickAccessNotification().update(text, app.isQuickAccessPause());
    }
  }

  private WorkTimeDay update(WorkTimeDay we) {
    int h = we.getHours();
    int m = we.getMinutes();
    seconds = we.getSeconds();
    ++seconds;
    if(seconds == 60) {
      seconds = 0;
      ++m;
    }
    if(m == 60) {
      m = 0;
      ++h;
    }
    we.setHours(h);
    we.setMinutes(m);
    we.setSeconds(seconds);
    return we;
  }
}
