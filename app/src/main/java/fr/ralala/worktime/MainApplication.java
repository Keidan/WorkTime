package fr.ralala.worktime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.ralala.worktime.ui.activities.DayActivity;
import fr.ralala.worktime.ui.activities.MainActivity;
import fr.ralala.worktime.dropbox.DropboxImportExport;
import fr.ralala.worktime.factories.DaysFactory;
import fr.ralala.worktime.factories.ProfilesFactory;
import fr.ralala.worktime.factories.PublicHolidaysFactory;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.Setting;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.ui.activities.SettingsActivity;
import fr.ralala.worktime.ui.quickaccess.QuickAccessNotification;
import fr.ralala.worktime.services.DropboxAutoExportService;
import fr.ralala.worktime.sql.SqlFactory;
import fr.ralala.worktime.utils.MyActivityLifecycleCallbacks;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Application context
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MainApplication extends Application  {
  private static final int NFY_QUICK_ACCESS = 1;
  private PublicHolidaysFactory mPublicHolidaysFactory;
  private ProfilesFactory mProfilesFactory;
  private DaysFactory mDaysFactory;
  private SqlFactory mSql = null;
  private Calendar mCurrentDate = null;
  private boolean mQuickAccessPause = true;
  private QuickAccessNotification mQuickAccessNotification = null;
  private boolean mResumeAfterActivity = false;
  private int mLastFirstVisibleItem = 0;
  private DropboxImportExport mDropboxImportExport = null;
  private List<DayEntry> mOnloadProfiles = null;
  private List<DayEntry> mOnloadDays = null;
  private List<DayEntry> mOnloadPublicHolidays = null;
  private List<Setting> mOnloadSettings = null;
  private WorkTimeDay mLastQuickAccessBreak = null;
  private long mLastWidgetOpen = 0L;
  private MyActivityLifecycleCallbacks mLifeCycle;


  public MainApplication() {
  }
  @Override
  public void onCreate() {
    super.onCreate();
    mPublicHolidaysFactory = new PublicHolidaysFactory();
    mProfilesFactory = new ProfilesFactory();
    mDaysFactory = new DaysFactory();
    mQuickAccessNotification = new QuickAccessNotification(this, NFY_QUICK_ACCESS);
    mDropboxImportExport = new DropboxImportExport();
    mOnloadSettings = new ArrayList<>();

    // Register for activity state changes notifications
    Class<?>[] classes = new Class<?>[] { MainActivity.class, DayActivity.class };
    registerActivityLifecycleCallbacks(mLifeCycle = new MyActivityLifecycleCallbacks(Arrays.asList(classes)));
  }

  public MyActivityLifecycleCallbacks getLifeCycle() {
    return mLifeCycle;
  }


  public static MainApplication getApp(final Context c) {
    return (MainApplication) c.getApplicationContext();
  }

  public boolean openSql(final Context c) {
    boolean ret = false;
    try {
      mSql = new SqlFactory(c);
      mSql.open(c);
      if(mOnloadSettings.isEmpty())
        mSql.settingsLoad(mOnloadSettings);
      mPublicHolidaysFactory.reload(mSql);
      mDaysFactory.reload(mSql);
      mProfilesFactory.reload(mSql);
      ret = true;
    } catch (final Exception e) {
      Log.e(getClass().getSimpleName(), "Error: " + e.getMessage(), e);
      UIHelper.showAlertDialog(this, R.string.error, getString(R.string.error) + ": " + e.getMessage());
    }
    return ret;
  }

  public Calendar getCurrentDate() {
    if(mCurrentDate == null) {
      mCurrentDate = Calendar.getInstance();
      mCurrentDate.setTimeZone(TimeZone.getTimeZone("GMT"));
      mCurrentDate.setTime(new Date());
    }
    return mCurrentDate;
  }

  public WorkTimeDay getLastQuickAccessBreak() {
    return mLastQuickAccessBreak;
  }

  public void setLastQuickAccessBreak(WorkTimeDay lastQuickAccessBreak) {
    mLastQuickAccessBreak = lastQuickAccessBreak;
  }

  public QuickAccessNotification getQuickAccessNotification() {
    return mQuickAccessNotification;
  }

  public ProfilesFactory getProfilesFactory() {
    return mProfilesFactory;
  }
  public DaysFactory getDaysFactory() {
    return mDaysFactory;
  }
  public PublicHolidaysFactory getPublicHolidaysFactory() {
    return mPublicHolidaysFactory;
  }

  public SqlFactory getSql() {
    return mSql;
  }

  public WorkTimeDay getEstimatedHours(List<DayEntry> wDays) {
    WorkTimeDay w = new WorkTimeDay();
    for(int i = 0; i < wDays.size(); ++i)
      w.addTime(wDays.get(i).getLegalWorktime());
    return w;
  }

  public void setQuickAccessPause(boolean quickAccessPause) {
    mQuickAccessPause = quickAccessPause;
  }

  public boolean isQuickAccessPause() {
    return mQuickAccessPause;
  }

  public void setResumeAfterActivity(boolean resumeAfterActivity) {
    mResumeAfterActivity = resumeAfterActivity;
  }
  public boolean isResumeAfterActivity() {
    return mResumeAfterActivity;
  }

  public void setLastFirstVisibleItem(int lastFirstVisibleItem) {
    mLastFirstVisibleItem = lastFirstVisibleItem;
  }

  public int getLastFirstVisibleItem() {
    return mLastFirstVisibleItem;
  }

  public DropboxImportExport getDropboxImportExport() {
    return mDropboxImportExport;
  }

  /* ----------------------------------
   * Global configuration
   * ----------------------------------
   */
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

  /* ----------------------------------
   * Database management
   * ----------------------------------
   */
  public void initOnLoadTables() {
    if(mOnloadSettings == null)
      mOnloadSettings = new ArrayList<>();
    if(mOnloadProfiles == null)
      mOnloadProfiles = mSql.getProfiles();
    if(mOnloadDays == null)
      mOnloadDays = mSql.getDays();
    if(mOnloadPublicHolidays == null)
      mOnloadPublicHolidays = mSql.getPublicHolidays();
    if(mOnloadSettings.isEmpty()) {
      mSql.settingsLoad(mOnloadSettings);
    }
  }

  public void disableDbUpdateFromOnloadSettings() {
    for(Setting s : mOnloadSettings) {
      if(s.getName().equals(DropboxAutoExportService.KEY_NEED_UPDATE)) {
        s.disable();
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
    List<DayEntry> profiles = mSql.getProfiles();
    List<DayEntry> days = mSql.getDays();
    List<DayEntry> publicHolidays = mSql.getPublicHolidays();
    mSql.settingsSave();
    List<Setting> settings = new ArrayList<>();
    mSql.settingsLoad(settings);
    return !listEquals(profiles, mOnloadProfiles) || !listEquals(mOnloadDays, days) || !listEquals(mOnloadPublicHolidays, publicHolidays) || (!listEquals(mOnloadSettings, settings));
  }

  /* ----------------------------------
   * Widget management 
   * ----------------------------------
   */
  public long getLastWidgetOpen() {
    return mLastWidgetOpen;
  }
  public void setLastWidgetOpen(long lastWidgetOpen) {
    mLastWidgetOpen = lastWidgetOpen;
  }

}
