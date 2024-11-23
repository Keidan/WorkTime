package fr.ralala.worktime.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.ProfileEntry;
import fr.ralala.worktime.models.PublicHolidayEntry;
import fr.ralala.worktime.models.Setting;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.services.AutoExportService;
import fr.ralala.worktime.ui.fragments.settings.SettingsDatabaseFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsDisplayFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsExcelExportFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsFragment;
import fr.ralala.worktime.ui.fragments.settings.SettingsLearningFragment;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.utils.PublicHolidayEntrySortComparator;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * SQL factory functions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SqlFactory implements SqlConstants {
  private static final String WHERE = "WHERE";
  private static final String AND = "AND";
  private static final String QAND = "=? " + AND;
  private static final String SELECT_ALL_FROM = "SELECT * FROM";
  private SQLiteDatabase mDdd = null;
  private final SqlHelper mHelper;
  private final Context mContext;

  /**
   * Returns the Android context.
   *
   * @return Context
   */
  public Context getContext() {
    return mContext;
  }

  /**
   * Creates the factory.
   *
   * @param context The Android context.
   */
  public SqlFactory(final Context context) {
    mHelper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
    if (!mHelper.isSupported()) {
      AndroidHelper.restartApplication(context, R.string.db_unsupported);
    } else if (mHelper.isUpdated())
      AndroidHelper.restartApplication(context, R.string.db_updated);
    mContext = context;
  }

  /**
   * Returns the BDD context.
   *
   * @return SQLiteDatabase
   */
  private SQLiteDatabase getBdd() {
    if (mDdd == null || !mDdd.isOpen())
      mDdd = mHelper.getWritableDatabase();
    return mDdd;
  }

  /**
   * Opens the SQLite connection.
   */
  public void open() {
    getBdd();
  }


  /**
   * Closes the SQLite connection.
   */
  public void close() {
    getBdd().close();
  }

  /**
   * Saves the application settings.
   */
  public void settingsSave() {
    getBdd().delete(TABLE_SETTINGS, null, null);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    addSetting(prefs, SettingsDisplayFragment.PREFS_KEY_DEFAULT_HOME, SettingsDisplayFragment.PREFS_DEF_VAL_DEFAULT_HOME);
    addSetting(prefs, SettingsLearningFragment.PREFS_KEY_PROFILES_WEIGHT_DEPTH, SettingsLearningFragment.PREFS_DEF_VAL_PROFILES_WEIGHT_DEPTH);
    addSetting(prefs, SettingsDisplayFragment.PREFS_KEY_DAY_ROWS_HEIGHT, SettingsDisplayFragment.PREFS_DEF_VAL_DAY_ROWS_HEIGHT);
    addSetting(prefs, SettingsFragment.PREFS_KEY_WORK_TIME_BY_DAY, SettingsFragment.PREFS_DEF_VAL_WORK_TIME_BY_DAY);
    addSetting(prefs, SettingsFragment.PREFS_KEY_AMOUNT_BY_HOUR, SettingsFragment.PREFS_DEF_VAL_AMOUNT_BY_HOUR);
    addSetting(prefs, SettingsFragment.PREFS_KEY_CURRENCY, SettingsFragment.PREFS_DEF_VAL_CURRENCY);
    addSetting(prefs, SettingsExcelExportFragment.PREFS_KEY_EMAIL, SettingsExcelExportFragment.PREFS_DEF_VAL_EMAIL);
    addSetting(prefs, SettingsExcelExportFragment.PREFS_KEY_EMAIL_ENABLE, SettingsExcelExportFragment.PREFS_DEF_VAL_EMAIL_ENABLE);
    addSetting(prefs, SettingsDisplayFragment.PREFS_KEY_HIDE_WAGE, SettingsDisplayFragment.PREFS_DEF_VAL_HIDE_WAGE);
    addSetting(prefs, SettingsExcelExportFragment.PREFS_KEY_EXPORT_HIDE_WAGE, SettingsExcelExportFragment.PREFS_DEF_VAL_EXPORT_HIDE_WAGE);
    addSetting(prefs, SettingsDisplayFragment.PREFS_KEY_SCROLL_TO_CURRENT_DAY, SettingsDisplayFragment.PREFS_DEF_VAL_SCROLL_TO_CURRENT_DAY);
    addSetting(prefs, SettingsDisplayFragment.PREFS_KEY_HIDE_EXIT_BUTTON, SettingsDisplayFragment.PREFS_DEF_VAL_HIDE_EXIT_BUTTON);
    addSetting(prefs, SettingsDisplayFragment.PREFS_KEY_DISPLAY_WEEK, SettingsDisplayFragment.PREFS_DEF_VAL_DISPLAY_WEEK);
    addSetting(prefs, SettingsDatabaseFragment.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE, SettingsDatabaseFragment.PREFS_DEF_VAL_IMPORT_EXPORT_AUTO_SAVE);
    addSetting(prefs, SettingsDatabaseFragment.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY, SettingsDatabaseFragment.PREFS_DEF_VAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY);
    addSetting(prefs, AutoExportService.KEY_NEED_UPDATE, AutoExportService.DEF_VAL_NEED_UPDATE);
  }

  /**
   * Loads the application settings.
   *
   * @param settings The settings list.
   */
  public void settingsLoad(List<Setting> settings) {
    final Cursor c = getBdd().rawQuery(SELECT_ALL_FROM + " " + TABLE_SETTINGS, null);
    if (c.moveToFirst()) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
      SharedPreferences.Editor edit = prefs.edit();
      do {
        String name = c.getString(NUM_SETTINGS_NAME);
        String value = c.getString(NUM_SETTINGS_VALUE);
        if (name.equals(SettingsDisplayFragment.PREFS_KEY_DEFAULT_HOME) || name.equals(SettingsLearningFragment.PREFS_KEY_PROFILES_WEIGHT_DEPTH)
          || name.equals(SettingsDisplayFragment.PREFS_KEY_DAY_ROWS_HEIGHT) || name.equals(SettingsFragment.PREFS_KEY_WORK_TIME_BY_DAY)
          || name.equals(SettingsFragment.PREFS_KEY_AMOUNT_BY_HOUR) || name.equals(SettingsFragment.PREFS_KEY_CURRENCY)
          || name.equals(SettingsExcelExportFragment.PREFS_KEY_EMAIL) || name.equals(SettingsDatabaseFragment.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY))
          edit.putString(name, value);
        else if (name.equals(SettingsExcelExportFragment.PREFS_KEY_EMAIL_ENABLE) || name.equals(SettingsDisplayFragment.PREFS_KEY_HIDE_WAGE)
          || name.equals(SettingsExcelExportFragment.PREFS_KEY_EXPORT_HIDE_WAGE) || name.equals(SettingsDisplayFragment.PREFS_KEY_SCROLL_TO_CURRENT_DAY)
          || name.equals(SettingsDisplayFragment.PREFS_KEY_HIDE_EXIT_BUTTON) || name.equals(SettingsDisplayFragment.PREFS_KEY_DISPLAY_WEEK)
          || name.equals(SettingsDatabaseFragment.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE) || name.equals(AutoExportService.KEY_NEED_UPDATE))
          edit.putBoolean(name, value.equals("1"));
        if (settings != null)
          settings.add(new Setting(name, value));
      } while (c.moveToNext());
      edit.apply();
    }
    c.close();
  }

  /**
   * Adds a setting
   *
   * @param prefs    The application preference.
   * @param key      The preference key.
   * @param defvalue The default value.
   */
  private void addSetting(SharedPreferences prefs, String key, String defvalue) {
    final ContentValues values = new ContentValues();
    values.put(COL_SETTINGS_NAME, key);
    values.put(COL_SETTINGS_VALUE, prefs.getString(key, defvalue));
    getBdd().insert(TABLE_SETTINGS, null, values);
  }

  /**
   * Adds a setting
   *
   * @param prefs    The application preference.
   * @param key      The preference key.
   * @param defvalue The default value.
   */
  private void addSetting(SharedPreferences prefs, String key, boolean defvalue) {
    final ContentValues values = new ContentValues();
    values.put(COL_SETTINGS_NAME, key);
    values.put(COL_SETTINGS_VALUE, prefs.getBoolean(key, defvalue));
    getBdd().insert(TABLE_SETTINGS, null, values);
  }

  /**
   * Insets or updates a profile.
   *
   * @param pe     The profile to add.
   * @param update update or insert.
   */
  public void insertOrUpdateProfile(final ProfileEntry pe, boolean update) {
    final ContentValues values = new ContentValues();
    values.put(COL_PROFILES_NAME, pe.getName().replace("'", "\\'"));
    values.put(COL_PROFILES_CURRENT_YEAR, pe.getDay().getYearString());
    values.put(COL_PROFILES_CURRENT_MONTH, pe.getDay().getMonthString());
    values.put(COL_PROFILES_CURRENT_DAY, pe.getDay().getDayString());
    values.put(COL_PROFILES_START_MORNING, pe.getStartMorning().timeString());
    values.put(COL_PROFILES_END_MORNING, pe.getEndMorning().timeString());
    values.put(COL_PROFILES_START_AFTERNOON, pe.getStartAfternoon().timeString());
    values.put(COL_PROFILES_END_AFTERNOON, pe.getEndAfternoon().timeString());
    values.put(COL_PROFILES_TYPE, pe.getTypeMorning().value() + "|" + pe.getTypeAfternoon().value());
    values.put(COL_PROFILES_AMOUNT, String.valueOf(pe.getAmountByHour()));
    values.put(COL_PROFILES_LEARNING_WEIGHT, String.valueOf(pe.getLearningWeight()));
    values.put(COL_PROFILES_LEGAL_WORKTIME, pe.getLegalWorkTime().timeString());
    values.put(COL_PROFILES_ADDITIONAL_BREAK, pe.getAdditionalBreak().timeString());
    values.put(COL_PROFILES_RECOVERY_TIME, pe.getRecoveryTime().timeString());
    values.put(COL_PROFILES_LATITUDE, getDouble(pe.getLatitude()));
    values.put(COL_PROFILES_LONGITUDE, getDouble(pe.getLongitude()));
    if (!update)
      pe.setID(getBdd().insert(TABLE_PROFILES, null, values));
    else
      getBdd().update(TABLE_PROFILES, values, COL_PROFILES_ID + "=?", new String[]{String.valueOf(pe.getID())});
  }

  /**
   * Insets or updates a day.
   *
   * @param de     The day to add.
   * @param update update or insert.
   */
  public void insertOrUpdateDay(final DayEntry de, boolean update) {
    final ContentValues values = new ContentValues();
    values.put(COL_DAYS_CURRENT_YEAR, de.getDay().getYearString());
    values.put(COL_DAYS_CURRENT_MONTH, de.getDay().getMonthString());
    values.put(COL_DAYS_CURRENT_DAY, de.getDay().getDayString());
    values.put(COL_DAYS_START_MORNING, de.getStartMorning().timeString());
    values.put(COL_DAYS_END_MORNING, de.getEndMorning().timeString());
    values.put(COL_DAYS_START_AFTERNOON, de.getStartAfternoon().timeString());
    values.put(COL_DAYS_END_AFTERNOON, de.getEndAfternoon().timeString());
    values.put(COL_DAYS_TYPE, de.getTypeMorning().value() + "|" + de.getTypeAfternoon().value());
    values.put(COL_DAYS_AMOUNT, String.valueOf(de.getAmountByHour()));
    values.put(COL_DAYS_LEGAL_WORKTIME, de.getLegalWorkTime().timeString());
    values.put(COL_DAYS_ADDITIONAL_BREAK, de.getAdditionalBreak().timeString());
    values.put(COL_DAYS_RECOVERY_TIME, de.getRecoveryTime().timeString());
    if (!update)
      de.setID(getBdd().insert(TABLE_DAYS, null, values));
    else
      getBdd().update(TABLE_DAYS, values, COL_DAYS_ID + "=?", new String[]{String.valueOf(de.getID())});
  }

  /**
   * Inserts or updates a public holiday.
   *
   * @param phe    The public holiday to insert.
   * @param update update or insert.
   */
  public void insertOrUpdatePublicHoliday(final PublicHolidayEntry phe, boolean update) {
    final ContentValues values = new ContentValues();
    values.put(COL_PUBLIC_HOLIDAYS_NAME, phe.getName().replace("'", "\\'"));
    values.put(COL_PUBLIC_HOLIDAYS_DATE_YEAR, phe.getDay().getYearString());
    values.put(COL_PUBLIC_HOLIDAYS_DATE_MONTH, phe.getDay().getMonthString());
    values.put(COL_PUBLIC_HOLIDAYS_DATE_DAY, phe.getDay().getDayString());
    values.put(COL_PUBLIC_HOLIDAYS_RECURRENCE, phe.isRecurrence() ? "1" : "0");
    if (!update)
      phe.setID(getBdd().insert(TABLE_PUBLIC_HOLIDAYS, null, values));
    else
      getBdd().update(TABLE_PUBLIC_HOLIDAYS, values, COL_PUBLIC_HOLIDAYS_ID + "=?", new String[]{String.valueOf(phe.getID())});
  }

  /**
   * Builds and executes the SQLite query.
   *
   * @param tablename The associated table name.
   * @param colYear   The year column name (null if year = -1).
   * @param year      The current year (-1 for all years).
   * @param colMonth  The month column name (null if month = -1).
   * @param month     The current month (-1 to ignore months).
   * @param colDay    The day column name (null if day = -1).
   * @param day       The current day (-1 to ignore days).
   * @return Cursor
   */
  private Cursor buildsAndExecutesQuery(String tablename, String colYear, int year, String colMonth, int month, String colDay, int day) {
    String query = SELECT_ALL_FROM + " " + tablename;
    List<String> args = new ArrayList<>();
    if (year != -1 && colYear != null) {
      query += " " + WHERE + " " + colYear + "=?";
      args.add(String.format(Locale.US, "%04d", year));
    }
    if (month != -1 && colMonth != null) {
      if (year != -1)
        query += " " + AND;
      else
        query += " " + WHERE;
      query += " " + colMonth + "=?";
      args.add(String.format(Locale.US, "%02d", month));
    }
    if (day != -1 && colDay != null) {
      if (year != -1 || month != -1)
        query += " " + AND;
      else
        query += " " + WHERE;
      query += " " + colDay + "=?";
      args.add(String.format(Locale.US, "%02d", day));
    }
    return getBdd().rawQuery(query, args.toArray(new String[]{}));
  }

  /**
   * Fills a public holiday using the result set cursor.
   *
   * @param c The result set cursor.
   * @return PublicHolidayEntry
   */
  private PublicHolidayEntry fillPublicHoliday(Cursor c) {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.setDay(Integer.parseInt(c.getString(NUM_PUBLIC_HOLIDAYS_DATE_DAY)));
    wtd.setMonth(Integer.parseInt(c.getString(NUM_PUBLIC_HOLIDAYS_DATE_MONTH)));
    wtd.setYear(Integer.parseInt(c.getString(NUM_PUBLIC_HOLIDAYS_DATE_YEAR)));
    PublicHolidayEntry phe = new PublicHolidayEntry(mContext, wtd, DayType.PUBLIC_HOLIDAY, DayType.PUBLIC_HOLIDAY);
    phe.setID(c.getLong(NUM_PUBLIC_HOLIDAYS_ID));
    //noinspection RegExpRedundantEscape
    phe.setName(c.getString(NUM_PUBLIC_HOLIDAYS_NAME).replace("\\'", "'"));
    phe.setRecurrence(c.getString(NUM_PUBLIC_HOLIDAYS_RECURRENCE).equals("1"));
    return phe;
  }

  /**
   * Fills a day using the result set cursor.
   *
   * @param c The result set cursor.
   * @return DayEntry
   */
  private DayEntry fillDay(Cursor c) {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.setDay(Integer.parseInt(c.getString(NUM_DAYS_CURRENT_DAY)));
    wtd.setMonth(Integer.parseInt(c.getString(NUM_DAYS_CURRENT_MONTH)));
    wtd.setYear(Integer.parseInt(c.getString(NUM_DAYS_CURRENT_YEAR)));
    String types = c.getString(NUM_DAYS_TYPE);
    DayType dta;
    DayType dtb;
    if (types != null && types.contains("|")) {
      String[] sp = types.split("\\|");
      dta = DayType.compute(getInt(sp[0], DayType.ERROR.value()));
      dtb = DayType.compute(getInt(sp[1], DayType.ERROR.value()));
    } else {
      dta = DayType.compute(c.getInt(NUM_DAYS_TYPE));
      dtb = DayType.compute(c.getInt(NUM_DAYS_TYPE));
    }
    final DayEntry de = new DayEntry(mContext, wtd, dta, dtb);
    de.setID(c.getLong(NUM_DAYS_ID));
    de.setStartMorning(c.getString(NUM_DAYS_START_MORNING));
    de.setEndMorning(c.getString(NUM_DAYS_END_MORNING));
    de.setStartAfternoon(c.getString(NUM_DAYS_START_AFTERNOON));
    de.setEndAfternoon(c.getString(NUM_DAYS_END_AFTERNOON));
    de.setLegalWorkTime(c.getString(NUM_DAYS_LEGAL_WORKTIME));
    de.setAdditionalBreak(c.getString(NUM_DAYS_ADDITIONAL_BREAK));
    de.setRecoveryTime(c.getString(NUM_DAYS_RECOVERY_TIME));
    String s = c.getString(NUM_DAYS_AMOUNT);
    if (s != null && !s.isEmpty())
      de.setAmountByHour(Double.parseDouble(s));
    return de;
  }

  /**
   * Fills a profile using the result set cursor.
   *
   * @param c The result set cursor.
   * @return ProfileEntry
   */
  private ProfileEntry fillProfile(Cursor c) {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.setDay(Integer.parseInt(c.getString(NUM_PROFILES_CURRENT_DAY)));
    wtd.setMonth(Integer.parseInt(c.getString(NUM_PROFILES_CURRENT_MONTH)));
    wtd.setYear(Integer.parseInt(c.getString(NUM_PROFILES_CURRENT_YEAR)));

    String types = c.getString(NUM_PROFILES_TYPE);
    DayType dta;
    DayType dtb;
    if (types != null && types.contains("|")) {
      String[] sp = types.split("\\|");
      dta = DayType.compute(getInt(sp[0], DayType.ERROR.value()));
      dtb = DayType.compute(getInt(sp[1], DayType.ERROR.value()));
    } else {
      dta = DayType.compute(c.getInt(NUM_PROFILES_TYPE));
      dtb = dta;
    }
    final ProfileEntry pe = new ProfileEntry(mContext, wtd, dta, dtb);
    pe.setID(c.getLong(NUM_PROFILES_ID));
    pe.setLatitude(getDouble(c.getString(NUM_PROFILES_LATITUDE)));
    pe.setLongitude(getDouble(c.getString(NUM_PROFILES_LONGITUDE)));
    pe.setStartMorning(c.getString(NUM_PROFILES_START_MORNING));
    pe.setEndMorning(c.getString(NUM_PROFILES_END_MORNING));
    pe.setStartAfternoon(c.getString(NUM_PROFILES_START_AFTERNOON));
    pe.setEndAfternoon(c.getString(NUM_PROFILES_END_AFTERNOON));
    String s = c.getString(NUM_PROFILES_AMOUNT);
    if (s != null && !s.isEmpty())
      pe.setAmountByHour(Double.parseDouble(s));
    s = c.getString(NUM_PROFILES_LEARNING_WEIGHT);
    if (s != null && !s.isEmpty())
      pe.setLearningWeight(Integer.parseInt(s));
    //noinspection RegExpRedundantEscape
    pe.setName(c.getString(NUM_PROFILES_NAME).replace("\\'", "'"));

    pe.setLegalWorkTime(c.getString(NUM_PROFILES_LEGAL_WORKTIME));
    pe.setAdditionalBreak(c.getString(NUM_PROFILES_ADDITIONAL_BREAK));
    pe.setRecoveryTime(c.getString(NUM_PROFILES_RECOVERY_TIME));
    return pe;
  }

  /**
   * Returns the public holiday by name.
   *
   * @param name The name.
   * @param date The date.
   * @return PublicHolidayEntry or null
   */
  public PublicHolidayEntry getPublicHoliday(String name, String date) {
    /* day, month, year */
    String[] d = date.split("/");
    final Cursor c = getBdd().rawQuery(SELECT_ALL_FROM + " " + TABLE_PUBLIC_HOLIDAYS +
      " " + WHERE + " " + COL_PUBLIC_HOLIDAYS_NAME + QAND + " " +
      COL_PUBLIC_HOLIDAYS_DATE_YEAR + QAND + " " +
      COL_PUBLIC_HOLIDAYS_DATE_MONTH + QAND + " " +
      COL_PUBLIC_HOLIDAYS_DATE_DAY + "=?", new String[]{name, d[2], d[1], d[0]});
    PublicHolidayEntry phee = null;
    if (c.moveToFirst()) {
      phee = fillPublicHoliday(c);
    }
    c.close();
    return phee;
  }

  /**
   * Returns the list of public holidays.
   *
   * @param year  The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day   The current day (-1 to ignore days).
   * @return List<PublicHolidayEntry>
   */
  public List<PublicHolidayEntry> getPublicHolidays(int year, int month, int day) {
    final List<PublicHolidayEntry> list = new ArrayList<>();
    final Cursor c = buildsAndExecutesQuery(TABLE_PUBLIC_HOLIDAYS, COL_PUBLIC_HOLIDAYS_DATE_YEAR, year,
      COL_PUBLIC_HOLIDAYS_DATE_MONTH, month, COL_PUBLIC_HOLIDAYS_DATE_DAY, day);
    if (c.moveToFirst()) {
      do {
        list.add(fillPublicHoliday(c));
      } while (c.moveToNext());
    }
    c.close();

    list.sort(PublicHolidayEntrySortComparator.comparator(
      PublicHolidayEntrySortComparator.getComparator(PublicHolidayEntrySortComparator.SORT_BY_RECURRENCE,
        PublicHolidayEntrySortComparator.SORT_BY_DATE)));
    return list;
  }

  /**
   * Returns an integer value from a string.
   *
   * @param s   The int in String
   * @param def The default value.
   * @return int
   */
  private int getInt(String s, int def) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return def;
    }
  }

  /**
   * Returns the 'double' value of a string and if the string is equal to SqlConstants.NAN then Double.NaN is returned.
   *
   * @param str The string to convert.
   * @return double
   */
  private double getDouble(String str) {
    return (str.equals(NAN)) ? Double.NaN : Double.parseDouble(str);
  }

  /**
   * Returns the 'String' value of a double and if the string is equal to Double.NaN then SqlConstants.NAN is returned.
   *
   * @param d The double to convert.
   * @return String
   */
  private String getDouble(double d) {
    return (Double.isNaN(d)) ? NAN : String.valueOf(d);
  }

  /**
   * Returns the public holiday by name.
   *
   * @param date The date.
   * @return DayEntry or null
   */
  public DayEntry getDay(String date) {
    /* day, month, year */
    String[] d = date.split("/");
    final Cursor c = getBdd().rawQuery(SELECT_ALL_FROM + " " + TABLE_DAYS +
      " " + WHERE + " " + COL_DAYS_CURRENT_YEAR + QAND + " " +
      COL_DAYS_CURRENT_MONTH + QAND + " " +
      COL_DAYS_CURRENT_DAY + "=?", new String[]{d[2], d[1], d[0]});
    DayEntry de = null;
    if (c.moveToFirst()) {
      de = fillDay(c);
    }
    c.close();
    return de;
  }

  /**
   * Returns the list of days.
   *
   * @param year  The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day   The current day (-1 to ignore days).
   * @return List<DayEntry>
   */
  public List<DayEntry> getDays(int year, int month, int day) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = buildsAndExecutesQuery(TABLE_DAYS, COL_DAYS_CURRENT_YEAR, year,
      COL_DAYS_CURRENT_MONTH, month, COL_DAYS_CURRENT_DAY, day);
    if (c.moveToFirst()) {
      do {
        list.add(fillDay(c));
      } while (c.moveToNext());
    }
    c.close();
    Comparator<DayEntry> comp = (lhs, rhs) -> lhs.getDay().compareTo(rhs.getDay());
    list.sort(comp);
    return list;
  }

  /**
   * Returns the years (unique values only) from the days table.
   *
   * @return List<Integer>
   */
  public List<Integer> getDaysYears() {
    List<Integer> list = new ArrayList<>();
    final Cursor c = getBdd().rawQuery("SELECT DISTINCT " + COL_DAYS_CURRENT_YEAR + " FROM " + TABLE_DAYS, null);
    if (c.moveToFirst()) {
      do {
        list.add(Integer.parseInt(c.getString(0)));
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.naturalOrder());
    return list;
  }

  /**
   * Returns the months (unique values only) from the days table.
   *
   * @param year The reference year.
   * @return List<Integer>
   */
  public List<Integer> getDaysMonths(int year) {
    List<Integer> list = new ArrayList<>();
    final Cursor c = getBdd().rawQuery("SELECT DISTINCT " + COL_DAYS_CURRENT_MONTH + " FROM " + TABLE_DAYS +
      " " + WHERE + " " + COL_DAYS_CURRENT_YEAR + "=?", new String[]{String.format(Locale.US, "%04d", year)});
    if (c.moveToFirst()) {
      do {
        list.add(Integer.parseInt(c.getString(0)));
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.naturalOrder());
    return list;
  }

  /**
   * Returns the list of profiles.
   *
   * @param year  The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day   The current day (-1 to ignore days).
   * @return List<ProfileEntry>
   */
  public List<ProfileEntry> getProfiles(int year, int month, int day) {
    final List<ProfileEntry> list = new ArrayList<>();
    final Cursor c = buildsAndExecutesQuery(TABLE_PROFILES, COL_PROFILES_CURRENT_YEAR, year,
      COL_PROFILES_CURRENT_MONTH, month, COL_PROFILES_CURRENT_DAY, day);
    if (c.moveToFirst()) {
      do {
        list.add(fillProfile(c));
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.comparing(ProfileEntry::getName));
    return list;
  }

  /**
   * Returns profile by name.
   *
   * @param name The profile name.
   * @return ProfileEntry or null if not found.
   */
  public ProfileEntry getProfile(String name) {
    ProfileEntry profile = null;
    final Cursor c = getBdd().rawQuery(SELECT_ALL_FROM + " " + TABLE_PROFILES +
      " " + WHERE + " " + COL_PROFILES_NAME + "=?", new String[]{name.replace("'", "\\'")});
    if (c.moveToFirst()) {
      profile = fillProfile(c);
    }
    c.close();
    return profile;
  }

  /**
   * Removes a public holiday.
   *
   * @param phe The entry to remove.
   */
  public void removePublicHoliday(final PublicHolidayEntry phe) {
    getBdd().delete(TABLE_PUBLIC_HOLIDAYS, COL_PUBLIC_HOLIDAYS_ID + "=?", new String[]{String.valueOf(phe.getID())});
  }

  /**
   * Removes a day.
   *
   * @param de The entry to remove.
   */
  public void removeDay(final DayEntry de) {
    getBdd().delete(TABLE_DAYS, COL_DAYS_ID + "=?", new String[]{String.valueOf(de.getID())});
  }

  /**
   * Removes a profile.
   *
   * @param pe The entry to remove.
   */
  public void removeProfile(final ProfileEntry pe) {
    getBdd().delete(TABLE_PROFILES, COL_PROFILES_ID + "=?", new String[]{String.valueOf(pe.getID())});
  }

}
