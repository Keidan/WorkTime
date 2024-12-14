package fr.ralala.worktime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;


import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import fr.ralala.worktime.dropbox.DropboxHelper;
import fr.ralala.worktime.dropbox.DropboxImportExport;
import fr.ralala.worktime.factories.DaysFactory;
import fr.ralala.worktime.factories.ProfilesFactory;
import fr.ralala.worktime.factories.PublicHolidaysFactory;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.sql.SqlFactory;
import fr.ralala.worktime.sql.SqlHelper;
import fr.ralala.worktime.ui.activities.DayActivity;
import fr.ralala.worktime.ui.activities.MainActivity;
import fr.ralala.worktime.ui.changelog.ChangeLog;
import fr.ralala.worktime.ui.changelog.ChangeLogIds;
import fr.ralala.worktime.ui.fragments.settings.SettingsDatabaseFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsDisplayFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsExcelExportFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsLearningFragment;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.Log;
import fr.ralala.worktime.utils.MyActivityLifecycleCallbacks;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Application context
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ApplicationCtx extends Application {
  private static final String PREFS_KEY_LAST_EXPORT = "pKeyLastExportType";
  public static final String PREFS_VAL_LAST_EXPORT_DROPBOX = "dropbox";
  private PublicHolidaysFactory mPublicHolidaysFactory;
  private ProfilesFactory mProfilesFactory;
  private DaysFactory mDaysFactory;
  private SqlFactory mSql = null;
  private Calendar mCurrentDate = null;
  private int mLastFirstVisibleItem = 0;
  private DropboxImportExport mDropboxImportExport = null;
  private long mLastWidgetOpen = 0L;
  private MyActivityLifecycleCallbacks mLifeCycle;
  private static int mResumedCounter = 0;
  private String mDbMD5 = null;
  private ChangeLog mChangeLog;

  private DropboxHelper mDropboxHelper;
  private final Log mLog = new Log();

  /**
   * Called by Android to create the application context.
   */
  @Override
  public void onCreate() {
    super.onCreate();
    mDropboxHelper = new DropboxHelper(this);
    mChangeLog = new ChangeLog(new ChangeLogIds(
      R.raw.changelog,
      R.string.changelog_ok_button,
      R.string.background_color,
      R.string.changelog_title,
      R.string.changelog_full_title,
      R.string.changelog_show_full), this);
    mPublicHolidaysFactory = new PublicHolidaysFactory();
    mProfilesFactory = new ProfilesFactory();
    mDaysFactory = new DaysFactory();
    mDropboxImportExport = new DropboxImportExport();
    // Register for activity state changes notifications
    Class<?>[] classes = new Class<?>[]{MainActivity.class, DayActivity.class};
    mLifeCycle = new MyActivityLifecycleCallbacks(Arrays.asList(classes));
    registerActivityLifecycleCallbacks(mLifeCycle);
    reloadDatabaseMD5();
  }

  public DropboxHelper getDropboxHelper() {
    return mDropboxHelper;
  }

  public ChangeLog getChangeLog() {
    return mChangeLog;
  }

  public Log getLog() {
    return mLog;
  }

  /**
   * Returns the onResumed method call counter.
   *
   * @return int
   */
  public static int getResumedCounter() {
    return mResumedCounter;
  }

  /**
   * Increments the onResumed method call counter.
   */
  public static void incResumedCounter() {
    mResumedCounter++;
  }

  /**
   * Returns the application ActivityLifecycleCallbacks
   *
   * @return MyActivityLifecycleCallbacks
   */
  public MyActivityLifecycleCallbacks getLifeCycle() {
    return mLifeCycle;
  }

  /**
   * Returns the current date time.
   *
   * @return Calendar
   */
  public Calendar getCurrentDate() {
    if (mCurrentDate == null) {
      mCurrentDate = Calendar.getInstance();
      mCurrentDate.setTimeZone(TimeZone.getTimeZone("GMT"));
      mCurrentDate.setTime(new Date());
    }
    return mCurrentDate;
  }

  /**
   * Returns the instance of the profiles factory.
   *
   * @return ProfilesFactory
   */
  public ProfilesFactory getProfilesFactory() {
    return mProfilesFactory;
  }

  /**
   * Returns the instance of the days factory.
   *
   * @return DaysFactory
   */
  public DaysFactory getDaysFactory() {
    return mDaysFactory;
  }

  /**
   * Returns the instance of the public holidays factory.
   *
   * @return PublicHolidaysFactory
   */
  public PublicHolidaysFactory getPublicHolidaysFactory() {
    return mPublicHolidaysFactory;
  }

  /**
   * Returns the instance of the SQLite object.
   *
   * @return SqlFactory
   */
  public SqlFactory getSql() {
    return mSql;
  }

  /**
   * Returns the estimated hours in accordance to the input list.
   *
   * @param wDays The input list.
   * @return WorkTimeDay
   */
  public WorkTimeDay getEstimatedHours(List<DayEntry> wDays) {
    WorkTimeDay w = new WorkTimeDay();
    for (int i = 0; i < wDays.size(); ++i)
      w.addTime(wDays.get(i).getLegalWorkTime());
    return w;
  }

  /**
   * Sets the last visible day (first of the display).
   *
   * @param lastFirstVisibleItem The new index.
   */
  public void setLastFirstVisibleItem(int lastFirstVisibleItem) {
    mLastFirstVisibleItem = lastFirstVisibleItem;
  }

  /**
   * Returns the index of the last visible day (first of the display).
   *
   * @return int
   */
  public int getLastFirstVisibleItem() {
    return mLastFirstVisibleItem;
  }

  /**
   * Returns the DropboxImportExport object.
   *
   * @return DropboxImportExport
   */
  public DropboxImportExport getDropboxImportExport() {
    return mDropboxImportExport;
  }

  /* ----------------------------------
   * Global configuration
   * ----------------------------------
   */

  /**
   * Returns the last export mode used.
   * Will change the behavior of the export icon
   *
   * @return String (see PREF_VAL_LAST_EXPORT_xxxxxxx)
   */
  public String getLastExportType() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(PREFS_KEY_LAST_EXPORT, PREFS_VAL_LAST_EXPORT_DROPBOX);
  }

  /**
   * Sets the last export mode used.
   * Will change the behavior of the export icon
   *
   * @param last see PREF_VAL_LAST_EXPORT_xxxxxxx
   */
  public void setLastExportType(String last) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    SharedPreferences.Editor e = prefs.edit();
    e.putString(PREFS_KEY_LAST_EXPORT, last);
    e.apply();
  }

  /**
   * Returns the periodicity with which the application must backup the databases (Only with dropbox and whether automatic export is enabled).
   *
   * @return int
   */
  public int getExportAutoSavePeriodicity() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsDatabaseFragment.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY, SettingsDatabaseFragment.PREFS_DEF_VAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY));
  }

  /**
   * Tests whether the application should enable automatic saving.
   *
   * @return boolean
   */
  public boolean isExportAutoSave() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsDatabaseFragment.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE, SettingsDatabaseFragment.PREFS_DEF_VAL_IMPORT_EXPORT_AUTO_SAVE);
  }

  /**
   * Returns the default home that the application should display.
   *
   * @return int
   */
  public int getDefaultHome() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsDisplayFragment.PREFS_KEY_DEFAULT_HOME, SettingsDisplayFragment.PREFS_DEF_VAL_DEFAULT_HOME));
  }

  /**
   * Returns the depth of weight used by learning profiles.
   *
   * @return int
   */
  public int getProfilesWeightDepth() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsLearningFragment.PREFS_KEY_PROFILES_WEIGHT_DEPTH, SettingsLearningFragment.PREFS_DEF_VAL_PROFILES_WEIGHT_DEPTH));
  }

  /**
   * Returns the row height of the days view (WorkTimeFragment).
   *
   * @return int
   */
  public int getDayRowsHeight() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Integer.parseInt(prefs.getString(SettingsDisplayFragment.PREFS_KEY_DAY_ROWS_HEIGHT, SettingsDisplayFragment.PREFS_DEF_VAL_DAY_ROWS_HEIGHT));
  }

  /**
   * Returns the legal work time value by day.
   *
   * @return WorkTimeDay
   */
  public WorkTimeDay getLegalWorkTimeByDay() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    String[] split = prefs.getString(SettingsFragment.PREFS_KEY_WORK_TIME_BY_DAY, SettingsFragment.PREFS_DEF_VAL_WORK_TIME_BY_DAY).split(":");
    return new WorkTimeDay(0, 0, 0, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

  /**
   * Returns the default amount by hour.
   *
   * @return double
   */
  public double getAmountByHour() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Double.parseDouble(prefs.getString(SettingsFragment.PREFS_KEY_AMOUNT_BY_HOUR, SettingsFragment.PREFS_DEF_VAL_AMOUNT_BY_HOUR).replace(",", "."));
  }

  /**
   * Returns the currency that must be used by the application.
   *
   * @return String
   */
  public String getCurrency() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(SettingsFragment.PREFS_KEY_CURRENCY, SettingsFragment.PREFS_DEF_VAL_CURRENCY);
  }

  /**
   * Returns the email address that the application must use for the export function.
   *
   * @return String
   */
  public String getEMail() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(SettingsExcelExportFragment.PREFS_KEY_EMAIL, SettingsExcelExportFragment.PREFS_DEF_VAL_EMAIL);
  }

  /**
   * Tests whether the exported file should be sent by e-mail.
   *
   * @return boolean
   */
  public boolean isExportMailEnabled() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsExcelExportFragment.PREFS_KEY_EMAIL_ENABLE, SettingsExcelExportFragment.PREFS_DEF_VAL_EMAIL_ENABLE);
  }

  /**
   * Tests if the application (DayActivity) should display the wage part.
   *
   * @return boolean
   */
  public boolean isHideWage() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsDisplayFragment.PREFS_KEY_HIDE_WAGE, SettingsDisplayFragment.PREFS_DEF_VAL_HIDE_WAGE);
  }

  /**
   * Tests whether the wage section should be displayed when exporting work time.
   *
   * @return boolean
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isExportHideWage() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsExcelExportFragment.PREFS_KEY_EXPORT_HIDE_WAGE, SettingsExcelExportFragment.PREFS_DEF_VAL_EXPORT_HIDE_WAGE);
  }

  /**
   * Tests whether the application (WorkTimeFragment) should scroll to the current day.
   *
   * @return boolean
   */
  public boolean isScrollToCurrentDay() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsDisplayFragment.PREFS_KEY_SCROLL_TO_CURRENT_DAY, SettingsDisplayFragment.PREFS_DEF_VAL_SCROLL_TO_CURRENT_DAY);
  }

  /**
   * Tests whether the exit button must be displayed or hidden.
   *
   * @return boolean
   */
  public boolean isHideExitButton() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsDisplayFragment.PREFS_KEY_HIDE_EXIT_BUTTON, SettingsDisplayFragment.PREFS_DEF_VAL_HIDE_EXIT_BUTTON);
  }

  /**
   * Tests if the week number must be displayed.
   *
   * @return boolean
   */
  public boolean isDisplayWeek() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsDisplayFragment.PREFS_KEY_DISPLAY_WEEK, SettingsDisplayFragment.PREFS_DEF_VAL_DISPLAY_WEEK);
  }

  /* ----------------------------------
   * Database management
   * ----------------------------------
   */

  /**
   * Detects a change in the SQLite tables.
   *
   * @return boolean
   */
  public boolean isTablesChanges() {
    String md5 = SqlHelper.getDatabaseMD5(this);
    Log.info(this, "isTablesChanges", "Database MD5: " + md5);
    return md5 != null && mDbMD5 != null && !mDbMD5.equals(md5);
  }

  /**
   * Reloads the database MD5.
   */
  public void reloadDatabaseMD5() {
    if (isExportAutoSave()) {
      mDbMD5 = SqlHelper.getDatabaseMD5(this);
      Log.info(this, "reloadDatabaseMD5", "Database MD5: " + mDbMD5);
    }
  }

  /**
   * Opens the SQLite connection and loads the application databases.
   *
   * @param c The Android context.
   * @return false on error.
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean openSql(final Context c) {
    try {
      mSql = new SqlFactory(c);
      mSql.open();
      mPublicHolidaysFactory.setSqlFactory(mSql);
      mDaysFactory.setSqlFactory(mSql);
      mProfilesFactory.setSqlFactory(mSql);
      return true;
    } catch (final Exception e) {
      Log.error(this, "openSql", "Error: " + e.getMessage(), e);
      UIHelper.showAlertDialog(this, R.string.error, getString(R.string.error) + ": " + e.getMessage());
    }
    return false;
  }
  /* ----------------------------------
   * Widget management
   * ----------------------------------
   */

  /**
   * Returns the time, in milliseconds, at which the widget opened the day activity.
   *
   * @return long.
   */
  public long getLastWidgetOpen() {
    return mLastWidgetOpen;
  }

  /**
   * Sets the time, in milliseconds, at which the widget opened the day activity.
   *
   * @param lastWidgetOpen The time in milliseconds.
   */
  public void setLastWidgetOpen(long lastWidgetOpen) {
    mLastWidgetOpen = lastWidgetOpen;
  }

}
