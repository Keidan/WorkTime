package fr.ralala.worktime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.ralala.worktime.dropbox.DropboxImportExport;
import fr.ralala.worktime.factories.DaysFactory;
import fr.ralala.worktime.factories.ProfilesFactory;
import fr.ralala.worktime.factories.PublicHolidaysFactory;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.Setting;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.activities.SettingsActivity;
import fr.ralala.worktime.quickaccess.QuickAccessNotification;
import fr.ralala.worktime.services.DropboxAutoExportService;
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
  private static final int NFY_QUICK_ACCESS = 1;
  private final PublicHolidaysFactory publicHolidaysFactory;
  private final ProfilesFactory profilesFactory;
  private final DaysFactory daysFactory;
  private SqlFactory sql = null;
  private Calendar currentDate = null;
  private boolean quickAccessPause = true;
  private QuickAccessNotification quickAccessNotification = null;
  private boolean resumeAfterActivity = false;
  private int lastFirstVisibleItem = 0;
  private DropboxImportExport dropboxImportExport = null;
  private List<DayEntry> onloadProfiles = null;
  private List<DayEntry> onloadDays = null;
  private List<DayEntry> onloadPublicHolidays = null;
  private List<Setting> onloadSettings = null;
  private WorkTimeDay lastQuickAccessBreak = null;
  private long lastWidgetOpen = 0L;


  public MainApplication() {
    publicHolidaysFactory = new PublicHolidaysFactory();
    profilesFactory = new ProfilesFactory();
    daysFactory = new DaysFactory();
    quickAccessNotification = new QuickAccessNotification(this, NFY_QUICK_ACCESS);
    dropboxImportExport = new DropboxImportExport();
    onloadSettings = new ArrayList<>();
  }


  public static MainApplication getApp(final Context c) {
    return (MainApplication) c.getApplicationContext();
  }

  public boolean openSql(final Context c) {
    boolean ret = false;
    try {
      sql = new SqlFactory(c);
      sql.open(c);
      if(onloadSettings.isEmpty())
        sql.settingsLoad(onloadSettings);
      publicHolidaysFactory.reload(sql);
      daysFactory.reload(sql);
      profilesFactory.reload(sql);
      ret = true;
      /*SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
      SharedPreferences.Editor ed = prefs.edit();
      ed.remove(DropboxAutoExportService.KEY_NEED_UPDATE);
      ed.apply();*/
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

  public WorkTimeDay getLastQuickAccessBreak() {
    return lastQuickAccessBreak;
  }

  public void setLastQuickAccessBreak(WorkTimeDay lastQuickAccessBreak) {
    this.lastQuickAccessBreak = lastQuickAccessBreak;
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

  public int getExportAutoSavePeriodicity() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY, SettingsActivity.PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY));
  }

  public boolean isExportAutoSave() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE, SettingsActivity.PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE.equals("true"));
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

  public boolean isHideExitButton() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_HIDE_EXIT_BUTTON, SettingsActivity.PREFS_DEFVAL_HIDE_EXIT_BUTTON.equals("true"));
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

  public void setResumeAfterActivity(boolean resumeAfterActivity) {
    this.resumeAfterActivity = resumeAfterActivity;
  }

  public boolean isResumeAfterActivity() {
    return resumeAfterActivity;
  }

  public void setLastFirstVisibleItem(int lastFirstVisibleItem) {
    this.lastFirstVisibleItem = lastFirstVisibleItem;
  }

  public int getLastFirstVisibleItem() {
    return lastFirstVisibleItem;
  }

  public DropboxImportExport getDropboxImportExport() {
    return dropboxImportExport;
  }

  public void initOnLoadTables() {
    if(onloadSettings == null)
      onloadSettings = new ArrayList<>();
    if(onloadProfiles == null)
      onloadProfiles = sql.getProfiles();
    if(onloadDays == null)
      onloadDays = sql.getDays();
    if(onloadPublicHolidays == null)
      onloadPublicHolidays = sql.getPublicHolidays();
    if(onloadSettings.isEmpty()) {
      sql.settingsLoad(onloadSettings);
    }
  }

  public void disableDbUpdateFromOnloadSettings() {
    for(Setting s : onloadSettings) {
      if(s.getName().equals(DropboxAutoExportService.KEY_NEED_UPDATE)) {
        s.setValue("false");
      }
    }
  }

  private <T> boolean listEquals(List<T> l1, List<T> l2) {
    List<T> list = new ArrayList<>();
    if(l1 == null && l2 == null)
      return true;
    if(l1 == null || l2 == null)
      return false;
    if(l1.size() != l2.size())
      return false;
    for(T i1 : l1) {
      for(T i2 : l2) {
        if(i1.equals(i2)) {
          list.add(i1);
        }
      }
    }
    return list.size() == l1.size();
  }

  public boolean isTablesChanges() {
    List<DayEntry> profiles = sql.getProfiles();
    List<DayEntry> days = sql.getDays();
    List<DayEntry> publicHolidays = sql.getPublicHolidays();
    sql.settingsSave();
    List<Setting> settings = new ArrayList<>();
    sql.settingsLoad(settings);
    return !listEquals(profiles, onloadProfiles) || !listEquals(onloadDays, days) || !listEquals(onloadPublicHolidays, publicHolidays) || (!listEquals(onloadSettings, settings));
  }

  public long getLastWidgetOpen() {
    return lastWidgetOpen;
  }

  public void setLastWidgetOpen(long lastWidgetOpen) {
    this.lastWidgetOpen = lastWidgetOpen;
  }

}
