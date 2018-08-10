package fr.ralala.worktime.services;

import java.util.Locale;
import java.util.TimerTask;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.ProfileEntry;
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
  private MainApplication mApp;
  private int mSeconds = 0;
  private String mTextLabel;
  private boolean mNeg = false;

  /**
   * Creates the service task.
   * @param app The application context.
   */
  QuickAccessServiceTask(MainApplication app) {
    mApp = app;
    mTextLabel = app.getString(R.string.work_time) + ": ";
  }

  /**
   * Run method.
   */
  @Override
  public void run() {
    DayEntry de = mApp.getDaysFactory().getCurrentDay();
    WorkTimeDay d = de.getDay();
    ProfileEntry wtd = mApp.getProfilesFactory().getHighestLearningWeight();
    long time;
    if(wtd != null) {
      WorkTimeDay wem = wtd.getEndMorning();
      time = wem.getTimeMs();
    } else
      time = new WorkTimeDay(0, 0, 0, 12, 0).getTimeMs();
    WorkTimeDay w = de.getWorkTime();
    mNeg = (w.getHours() < 0 || w.getMinutes() < 0);
    if(d.getTimeMs() < time) {
      /* morning */
      if(de.getStartMorning().timeString().equals("00:00")) {
        de.setStartMorning(d.timeString());
        de.setTypeMorning(DayType.AT_WORK);
        de.setEndMorning(d.timeString());
      }
      de.setEndMorning(update(de.getEndMorning()));
    } else {
      /* afternoon */
      if(de.getStartAfternoon().timeString().equals("00:00")) {
        de.setStartAfternoon(d.timeString());
        de.setTypeAfternoon(DayType.AT_WORK);
        de.setEndAfternoon(d.timeString());
      }
      de.setEndAfternoon(update(de.getEndAfternoon()));
    }
    w = de.getWorkTime();
    String text = mTextLabel;
    if(w.getHours() < 0 || w.getMinutes() < 0)
      text += String.format(Locale.US, "-%02d:%02d:%02d", Math.abs(w.getHours()), Math.abs(w.getMinutes()), mSeconds);
    else
      text += String.format(Locale.US, "%02d:%02d:%02d", w.getHours(), w.getMinutes(), mSeconds);
    mApp.getQuickAccessNotification().update(text, mApp.isQuickAccessPause());
  }

  /**
   * Updates the quicktime value.
   * @param we The current work time entry.
   * @return WorkTimeDay
   */
  private WorkTimeDay update(WorkTimeDay we) {
    int h = we.getHours();
    int m = we.getMinutes();
    mSeconds = we.getSeconds();
    if(mNeg) {
      --mSeconds;
      if (mSeconds == -1) {
        mSeconds = 59;
        --m;
      }
      if (m == -1) {
        m = 59;
        --h;
      }
    } else {
      ++mSeconds;
      if (mSeconds == 60) {
        mSeconds = 0;
        ++m;
      }
      if (m == 60) {
        m = 0;
        ++h;
      }
    }
    we.setHours(h);
    we.setMinutes(m);
    we.setSeconds(mSeconds);
    return we;
  }
}
