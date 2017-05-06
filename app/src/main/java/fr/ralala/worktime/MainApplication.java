package fr.ralala.worktime;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.ralala.worktime.factories.DaysFactory;
import fr.ralala.worktime.factories.ProfilesFactory;
import fr.ralala.worktime.factories.PublicHolidaysFactory;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.activities.SettingsActivity;
import fr.ralala.worktime.quickaccess.QuickAccessNotification;
import fr.ralala.worktime.sql.SqlFactory;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Application context
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MainApplication extends Application {
  private final PublicHolidaysFactory publicHolidaysFactory;
  private final ProfilesFactory profilesFactory;
  private final DaysFactory daysFactory;
  private SqlFactory sql = null;
  private Calendar currentDate = null;
  private boolean quickAccessPause = true;
  private int nfyIdQuickAccess = 1;
  private QuickAccessNotification quickAccessNotification = null;

  public MainApplication() {
    publicHolidaysFactory = new PublicHolidaysFactory();
    profilesFactory = new ProfilesFactory();
    daysFactory = new DaysFactory();
    quickAccessNotification = new QuickAccessNotification(this, nfyIdQuickAccess);
  }


  public static MainApplication getApp(final Context c) {
    return (MainApplication) c.getApplicationContext();
  }

  public boolean openSql(final Activity activity) {
    boolean ret = false;
    try {
      sql = new SqlFactory(this);
      sql.open(activity);
      sql.settingsLoad();
      publicHolidaysFactory.reload(sql);
      daysFactory.reload(sql);
      profilesFactory.reload(sql);
      ret = true;
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
      AndroidHelper.showAlertDialog(this, R.string.error, getString(R.string.error) + ": " + e.getMessage());
    }
    return ret;
  }

  public Calendar getCurrentDate() {
    if(currentDate == null) {
      currentDate = Calendar.getInstance();
      currentDate.setTimeZone(TimeZone.getTimeZone("GMT"));
      currentDate.setTime(new Date());
    }
    return currentDate;
  }

  public QuickAccessNotification getQuickAccessNotification() {
    return quickAccessNotification;
  }

  public ProfilesFactory getProfilesFactory() {
    return profilesFactory;
  }

  public DaysFactory getDaysFactory() {
    return daysFactory;
  }

  public PublicHolidaysFactory getPublicHolidaysFactory() {
    return publicHolidaysFactory;
  }

  public SqlFactory getSql() {
    return sql;
  }


  public int getDefaultHome() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsActivity.PREFS_KEY_DEFAULT_HOME, SettingsActivity.PREFS_DEFVAL_DEFAULT_HOME));
  }
  public int getProfilesWeightDepth() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsActivity.PREFS_KEY_PROFILES_WEIGHT_DEPTH, SettingsActivity.PREFS_DEFVAL_PROFILES_WEIGHT_DEPTH));
  }

  public int getDayRowsHeight() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsActivity.PREFS_KEY_DAY_ROWS_HEIGHT, SettingsActivity.PREFS_DEFVAL_DAY_ROWS_HEIGHT));
  }

  public WorkTimeDay getLegalWorkTimeByDay() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    String [] split = prefs.getString(SettingsActivity.PREFS_KEY_WORKTIME_BY_DAY, SettingsActivity.PREFS_DEFVAL_WORKTIME_BY_DAY).split(":");
    return new WorkTimeDay(0, 0, 0, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

  public double getAmountByHour() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Double.parseDouble(prefs.getString(SettingsActivity.PREFS_KEY_AMOUNT_BY_HOUR, SettingsActivity.PREFS_DEFVAL_AMOUNT_BY_HOUR).replaceAll(",", "."));
  }

  public String getCurrency() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(SettingsActivity.PREFS_KEY_CURRENCY, SettingsActivity.PREFS_DEFVAL_CURRENCY);
  }

  public String getEMail() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(SettingsActivity.PREFS_KEY_EMAIL, SettingsActivity.PREFS_DEFVAL_EMAIL);
  }

  public boolean isExportMailEnabled() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_EMAIL_ENABLE, SettingsActivity.PREFS_DEFVAL_EMAIL_ENABLE.equals("true"));
  }

  public boolean isHideWage() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_HIDE_WAGE, SettingsActivity.PREFS_DEFVAL_HIDE_WAGE.equals("true"));
  }

  public boolean isExportHideWage() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_EXPORT_HIDE_WAGE, SettingsActivity.PREFS_DEFVAL_EXPORT_HIDE_WAGE.equals("true"));
  }

  public boolean isScrollToCurrentDay() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_SCROLL_TO_CURRENT_DAY, SettingsActivity.PREFS_DEFVAL_SCROLL_TO_CURRENT_DAY.equals("true"));
  }

  public WorkTimeDay getEstimatedHours(List<DayEntry> wDays) {
    WorkTimeDay w = new WorkTimeDay();
    for(int i = 0; i < wDays.size(); ++i)
      w.addTime(wDays.get(i).getLegalWorktime());
    return w;
  }

  public void setQuickAccessPause(boolean quickAccessPause) {
    this.quickAccessPause = quickAccessPause;
  }

  public boolean isQuickAccessPause() {
    return quickAccessPause;
  }
}
