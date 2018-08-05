package fr.ralala.worktime.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Process;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.settings.SettingsActivity;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.Setting;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.services.DropboxAutoExportService;
import fr.ralala.worktime.ui.activities.settings.SettingsDatabaseActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsDisplayActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsExcelExportActivity;
import fr.ralala.worktime.ui.activities.settings.SettingsLearningActivity;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.utils.SortComparator;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * SQL factory functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlFactory implements SqlConstants {
  private SQLiteDatabase mDdd = null;
  private SqlHelper mHelper;
  private Context mContext;

  /**
   * Returns the Android context.
   * @return Context
   */
  public Context getContext() {
    return mContext;
  }

  /**
   * Creates the factory.
   * @param context The Android context.
   */
  public SqlFactory(final Context context) {
    mHelper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
    if(!mHelper.isSupported()) {
      AndroidHelper.restartApplication(context, R.string.db_unsupported);
    } else if(mHelper.isUpdated())
      AndroidHelper.restartApplication(context, R.string.db_updated);
    mContext = context;
  }

  /**
   * Returns the BDD context.
   * @return SQLiteDatabase
   */
  private SQLiteDatabase getBdd() {
    if(mDdd == null || !mDdd.isOpen())
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
   * Updates a profile.
   * @param de The profile to update.
   */
  public void updateProfile(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PROFILES_LEARNING_WEIGHT, "" + de.getLearningWeight());
    getBdd().update(TABLE_PROFILES, values, COL_PROFILES_NAME + "='"+de.getName().replaceAll("'", "\\'") + "'", null);
  }

  /**
   * Saves the application settings.
   */
  public void settingsSave() {
    getBdd().delete(TABLE_SETTINGS, null, null);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
    addSetting(prefs, SettingsDisplayActivity.PREFS_KEY_DEFAULT_HOME, SettingsDisplayActivity.PREFS_DEFVAL_DEFAULT_HOME);
    addSetting(prefs, SettingsLearningActivity.PREFS_KEY_PROFILES_WEIGHT_DEPTH, SettingsLearningActivity.PREFS_DEFVAL_PROFILES_WEIGHT_DEPTH);
    addSetting(prefs, SettingsDisplayActivity.PREFS_KEY_DAY_ROWS_HEIGHT, SettingsDisplayActivity.PREFS_DEFVAL_DAY_ROWS_HEIGHT);
    addSetting(prefs, SettingsActivity.PREFS_KEY_WORKTIME_BY_DAY, SettingsActivity.PREFS_DEFVAL_WORKTIME_BY_DAY);
    addSetting(prefs, SettingsActivity.PREFS_KEY_AMOUNT_BY_HOUR, SettingsActivity.PREFS_DEFVAL_AMOUNT_BY_HOUR);
    addSetting(prefs, SettingsActivity.PREFS_KEY_CURRENCY, SettingsActivity.PREFS_DEFVAL_CURRENCY);
    addSetting(prefs, SettingsExcelExportActivity.PREFS_KEY_EMAIL, SettingsExcelExportActivity.PREFS_DEFVAL_EMAIL);
    addSetting(prefs, SettingsExcelExportActivity.PREFS_KEY_EMAIL_ENABLE, SettingsExcelExportActivity.PREFS_DEFVAL_EMAIL_ENABLE.equals("true"));
    addSetting(prefs, SettingsDisplayActivity.PREFS_KEY_HIDE_WAGE, SettingsDisplayActivity.PREFS_DEFVAL_HIDE_WAGE.equals("true"));
    addSetting(prefs, SettingsExcelExportActivity.PREFS_KEY_EXPORT_HIDE_WAGE, SettingsExcelExportActivity.PREFS_DEFVAL_EXPORT_HIDE_WAGE.equals("true"));
    addSetting(prefs, SettingsDisplayActivity.PREFS_KEY_SCROLL_TO_CURRENT_DAY, SettingsDisplayActivity.PREFS_DEFVAL_SCROLL_TO_CURRENT_DAY.equals("true"));
    addSetting(prefs, SettingsDisplayActivity.PREFS_KEY_HIDE_EXIT_BUTTON, SettingsDisplayActivity.PREFS_DEFVAL_HIDE_EXIT_BUTTON.equals("true"));
    addSetting(prefs, SettingsDatabaseActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE, SettingsDatabaseActivity.PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE.equals("false"));
    addSetting(prefs, SettingsDatabaseActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY, SettingsDatabaseActivity.PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY);
    addSetting(prefs, DropboxAutoExportService.KEY_NEED_UPDATE, DropboxAutoExportService.DEFVAL_NEED_UPDATE.equals("false"));
  }

  /**
   * Loads the application settings.
   * @param settings The settings list.
   */
  public void settingsLoad(List<Setting> settings) {
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + TABLE_SETTINGS, null);
    if (c.moveToFirst()) {
      do {
        String name = c.getString(NUM_SETTINGS_NAME);
        String value = c.getString(NUM_SETTINGS_VALUE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        SharedPreferences.Editor edit = prefs.edit();
        if(name.equals(SettingsDisplayActivity.PREFS_KEY_DEFAULT_HOME) || name.equals(SettingsLearningActivity.PREFS_KEY_PROFILES_WEIGHT_DEPTH)
            || name.equals(SettingsDisplayActivity.PREFS_KEY_DAY_ROWS_HEIGHT) || name.equals(SettingsActivity.PREFS_KEY_WORKTIME_BY_DAY)
            || name.equals(SettingsActivity.PREFS_KEY_AMOUNT_BY_HOUR) || name.equals(SettingsActivity.PREFS_KEY_CURRENCY )
            || name.equals(SettingsExcelExportActivity.PREFS_KEY_EMAIL) || name.equals(SettingsDatabaseActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY))
          edit.putString(name, value);
        else if(name.equals(SettingsExcelExportActivity.PREFS_KEY_EMAIL_ENABLE) || name.equals(SettingsDisplayActivity.PREFS_KEY_HIDE_WAGE)
            || name.equals(SettingsExcelExportActivity.PREFS_KEY_EXPORT_HIDE_WAGE) || name.equals(SettingsDisplayActivity.PREFS_KEY_SCROLL_TO_CURRENT_DAY)
            || name.equals(SettingsDisplayActivity.PREFS_KEY_HIDE_EXIT_BUTTON) || name.equals(SettingsDatabaseActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE)
            || name.equals(DropboxAutoExportService.KEY_NEED_UPDATE))
          edit.putBoolean(name, value.equals("1"));
        if(settings != null)
          settings.add(new Setting(name, value));
        edit.apply();
      } while (c.moveToNext());
    }
    c.close();
  }

  /**
   * Adds a setting
   * @param prefs The application preference.
   * @param key The preference key.
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
   * @param prefs The application preference.
   * @param key The preference key.
   * @param defvalue The default value.
   */
  private void addSetting(SharedPreferences prefs, String key, boolean defvalue) {
    final ContentValues values = new ContentValues();
    values.put(COL_SETTINGS_NAME, key);
    values.put(COL_SETTINGS_VALUE, prefs.getBoolean(key, defvalue));
    getBdd().insert(TABLE_SETTINGS, null, values);
  }

  /**
   * Insets a profile.
   * @param de The profile to add.
   */
  public void insertProfile(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PROFILES_NAME, de.getName().replaceAll("'", "\\'"));
    values.put(COL_PROFILES_CURRENT_YEAR, de.getDay().getYearString());
    values.put(COL_PROFILES_CURRENT_MONTH, de.getDay().getMonthString());
    values.put(COL_PROFILES_CURRENT_DAY, de.getDay().getDayString());
    values.put(COL_PROFILES_START_MORNING, de.getStartMorning().timeString());
    values.put(COL_PROFILES_END_MORNING, de.getEndMorning().timeString());
    values.put(COL_PROFILES_START_AFTERNOON, de.getStartAfternoon().timeString());
    values.put(COL_PROFILES_END_AFTERNOON, de.getEndAfternoon().timeString());
    values.put(COL_PROFILES_TYPE, de.getTypeMorning().value() + "|" + de.getTypeAfternoon().value());
    values.put(COL_PROFILES_AMOUNT, String.valueOf(de.getAmountByHour()));
    values.put(COL_PROFILES_LEARNING_WEIGHT, String.valueOf(de.getLearningWeight()));
    values.put(COL_PROFILES_LEGAL_WORKTIME, de.getLegalWorktime().timeString());
    values.put(COL_PROFILES_ADDITIONAL_BREAK, de.getAdditionalBreak().timeString());
    values.put(COL_PROFILES_RECOVERY_TIME, de.getRecoveryTime().timeString());
    getBdd().insert(TABLE_PROFILES, null, values);
  }

  /**
   * Inserts a day.
   * @param de The day to insert.
   */
  public void insertDay(final DayEntry de) {
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
    values.put(COL_DAYS_LEGAL_WORKTIME, de.getLegalWorktime().timeString());
    values.put(COL_DAYS_ADDITIONAL_BREAK, de.getAdditionalBreak().timeString());
    values.put(COL_DAYS_RECOVERY_TIME, de.getRecoveryTime().timeString());
    getBdd().insert(TABLE_DAYS, null, values);
  }

  /**
   * Inserts a public holiday.
   * @param de The public holiday to insert.
   */
  public void insertPublicHoliday(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PUBLIC_HOLIDAYS_NAME, de.getName().replaceAll("'", "\\'"));
    values.put(COL_PUBLIC_HOLIDAYS_DATE_YEAR, de.getDay().getYearString());
    values.put(COL_PUBLIC_HOLIDAYS_DATE_MONTH, de.getDay().getMonthString());
    values.put(COL_PUBLIC_HOLIDAYS_DATE_DAY, de.getDay().getDayString());
    values.put(COL_PUBLIC_HOLIDAYS_RECURRENCE, de.isRecurrence() ? "1" : "0");
    getBdd().insert(TABLE_PUBLIC_HOLIDAYS, null, values);
  }

  /**
   * Builds and executes the SQLite query.
   * @param tablename The associated table name.
   * @param colYear The year column name (null if year = -1).
   * @param year The current year (-1 for all years).
   * @param colMonth The month column name (null if month = -1).
   * @param month The current month (-1 to ignore months).
   * @param colDay The day column name (null if day = -1).
   * @param day The current day (-1 to ignore days).
   * @return Cursor
   */
  private Cursor buildsAndExecutesQuery(String tablename, String colYear, int year, String colMonth, int month, String colDay, int day) {
    String query = "SELECT * FROM " + tablename;
    List<String> args = new ArrayList<>();
    if(year != -1 && colYear != null) {
      query += " WHERE " + colYear + "=?";
      args.add(String.format(Locale.US, "%04d", year));
    }
    if(month != -1 && colMonth != null) {
      if(year != -1)
        query += " and";
      else
        query += " WHERE";
      query += " " + colMonth + "=?";
      args.add(String.format(Locale.US, "%02d", month));
    }
    if(day != -1 && colDay != null) {
      if(year != -1 || month != -1)
        query += " and";
      else
        query += " WHERE";
      query += " " + colDay + "=?";
      args.add(String.format(Locale.US, "%02d", day));
    }

    return getBdd().rawQuery(query, args.toArray(new String[]{}));
  }

  /**
   * Fills a public holiday using the result set cursor.
   * @param c The result set cursor.
   * @return DayEntry
   */
  private DayEntry fillPublicHoliday(Cursor c) {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.setDay(Integer.parseInt(c.getString(NUM_PUBLIC_HOLIDAYS_DATE_DAY)));
    wtd.setMonth(Integer.parseInt(c.getString(NUM_PUBLIC_HOLIDAYS_DATE_MONTH)));
    wtd.setYear(Integer.parseInt(c.getString(NUM_PUBLIC_HOLIDAYS_DATE_YEAR)));
    DayEntry de = new DayEntry(mContext, wtd, DayType.PUBLIC_HOLIDAY, DayType.PUBLIC_HOLIDAY);
    //noinspection RegExpRedundantEscape
    de.setName(c.getString(NUM_PUBLIC_HOLIDAYS_NAME).replaceAll("\\'", "'"));
    de.setRecurrence(c.getString(NUM_PUBLIC_HOLIDAYS_RECURRENCE).equals("1"));
    return de;
  }

  /**
   * Returns the public holiday by name.
   * @param name The name.
   * @param date The date.
   * @return DayEntry or null
   */
  public DayEntry getPublicHoliday(String name, String date) {
    /* day, month, year */
    String d [] = date.split("/");
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + TABLE_PUBLIC_HOLIDAYS +
        " WHERE " + COL_PUBLIC_HOLIDAYS_NAME + "=? AND " +
        COL_PUBLIC_HOLIDAYS_DATE_YEAR + "=? AND " +
        COL_PUBLIC_HOLIDAYS_DATE_MONTH + "=? AND " +
        COL_PUBLIC_HOLIDAYS_DATE_DAY + "=?", new String[]{ name, d[2], d[1], d[0] });
    DayEntry de = null;
    if (c.moveToFirst()) {
      de = fillPublicHoliday(c);
    }
    c.close();
    return de;
  }

  /**
   * Returns the list of public holidays.
   * @param year The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day The current day (-1 to ignore days).
   * @return List<DayEntry>
   */
  public List<DayEntry> getPublicHolidays(int year, int month, int day) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = buildsAndExecutesQuery(TABLE_PUBLIC_HOLIDAYS, COL_PUBLIC_HOLIDAYS_DATE_YEAR, year,
        COL_PUBLIC_HOLIDAYS_DATE_MONTH, month, COL_PUBLIC_HOLIDAYS_DATE_DAY, day);
    if (c.moveToFirst()) {
      do {
        list.add(fillPublicHoliday(c));
      } while (c.moveToNext());
    }
    c.close();

    list.sort(SortComparator.comparator(
        SortComparator.getComparator(SortComparator.SORT_BY_RECURRENCE,
            SortComparator.SORT_BY_DATE)));
    return list;
  }

  /**
   * Returns an integer value from a string.
   * @param s The int in String
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
   * Fills a day using the result set cursor.
   * @param c The result set cursor.
   * @return DayEntry
   */
  private DayEntry fillDay(Cursor c) {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.setDay(Integer.parseInt(c.getString(NUM_DAYS_CURRENT_DAY)));
    wtd.setMonth(Integer.parseInt(c.getString(NUM_DAYS_CURRENT_MONTH)));
    wtd.setYear(Integer.parseInt(c.getString(NUM_DAYS_CURRENT_YEAR)));
    String types = c.getString(NUM_DAYS_TYPE);
    DayType dta, dtb;
    if(types != null && !types.isEmpty() && types.contains("|")) {
      String [] sp = types.split("\\|");
      dta = DayType.compute(getInt(sp[0], DayType.ERROR.value()));
      dtb = DayType.compute(getInt(sp[1], DayType.ERROR.value()));
    } else {
      dta = DayType.compute(c.getInt(NUM_DAYS_TYPE));
      dtb = DayType.compute(c.getInt(NUM_DAYS_TYPE));
    }
    final DayEntry de = new DayEntry(mContext, wtd, dta, dtb);
    de.setStartMorning(c.getString(NUM_DAYS_START_MORNING));
    de.setEndMorning(c.getString(NUM_DAYS_END_MORNING));
    de.setStartAfternoon(c.getString(NUM_DAYS_START_AFTERNOON));
    de.setEndAfternoon(c.getString(NUM_DAYS_END_AFTERNOON));
    de.setLegalWorktime(c.getString(NUM_DAYS_LEGAL_WORKTIME));
    de.setAdditionalBreak(c.getString(NUM_DAYS_ADDITIONAL_BREAK));
    de.setRecoveryTime(c.getString(NUM_DAYS_RECOVERY_TIME));
    String s = c.getString(NUM_DAYS_AMOUNT);
    if(s != null && !s.isEmpty())
      de.setAmountByHour(Double.parseDouble(s));
    return de;
  }

  /**
   * Returns the public holiday by name.
   * @param date The date.
   * @return DayEntry or null
   */
  public DayEntry getDay(String date) {
    /* day, month, year */
    String d [] = date.split("/");
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + TABLE_DAYS +
        " WHERE " + COL_DAYS_CURRENT_YEAR + "=? AND " +
        COL_DAYS_CURRENT_MONTH + "=? AND " +
        COL_DAYS_CURRENT_DAY + "=?", new String[]{ d[2], d[1], d[0] });
    DayEntry de = null;
    if (c.moveToFirst()) {
      de = fillDay(c);
    }
    c.close();
    return de;
  }

  /**
   * Returns the list of days.
   * @param year The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day The current day (-1 to ignore days).
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
   * @param year The reference year.
   * @return List<Integer>
   */
  public List<Integer> getDaysMonths(int year) {
    List<Integer> list = new ArrayList<>();
    final Cursor c = getBdd().rawQuery("SELECT DISTINCT " + COL_DAYS_CURRENT_MONTH + " FROM " + TABLE_DAYS +
        " WHERE " + COL_DAYS_CURRENT_YEAR + "=?", new String[]{ String.format(Locale.US, "%04d", year) });
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
   * Fills a profile using the result set cursor.
   * @param c The result set cursor.
   * @return DayEntry
   */
  private DayEntry fillProfile(Cursor c) {
    WorkTimeDay wtd = new WorkTimeDay();
    wtd.setDay(Integer.parseInt(c.getString(NUM_PROFILES_CURRENT_DAY)));
    wtd.setMonth(Integer.parseInt(c.getString(NUM_PROFILES_CURRENT_MONTH)));
    wtd.setYear(Integer.parseInt(c.getString(NUM_PROFILES_CURRENT_YEAR)));

    String types = c.getString(NUM_PROFILES_TYPE);
    DayType dta, dtb;
    if(types != null && !types.isEmpty() && types.contains("|")) {
      String [] sp = types.split("\\|");
      dta = DayType.compute(getInt(sp[0], DayType.ERROR.value()));
      dtb = DayType.compute(getInt(sp[1], DayType.ERROR.value()));
    } else {
      dta = DayType.compute(c.getInt(NUM_PROFILES_TYPE));
      dtb = dta;
    }
    final DayEntry de = new DayEntry(mContext, wtd, dta, dtb);
    de.setStartMorning(c.getString(NUM_PROFILES_START_MORNING));
    de.setEndMorning(c.getString(NUM_PROFILES_END_MORNING));
    de.setStartAfternoon(c.getString(NUM_PROFILES_START_AFTERNOON));
    de.setEndAfternoon(c.getString(NUM_PROFILES_END_AFTERNOON));
    String s = c.getString(NUM_PROFILES_AMOUNT);
    if(s != null && !s.isEmpty())
      de.setAmountByHour(Double.parseDouble(s));
    s = c.getString(NUM_PROFILES_LEARNING_WEIGHT);
    if(s != null && !s.isEmpty())
      de.setLearningWeight(Integer.parseInt(s));
    //noinspection RegExpRedundantEscape
    de.setName(c.getString(NUM_PROFILES_NAME).replaceAll("\\'", "'"));

    de.setLegalWorktime(c.getString(NUM_PROFILES_LEGAL_WORKTIME));
    de.setAdditionalBreak(c.getString(NUM_PROFILES_ADDITIONAL_BREAK));
    de.setRecoveryTime(c.getString(NUM_PROFILES_RECOVERY_TIME));
    return de;
  }

  /**
   * Returns the list of profiles.
   * @param year The current year (-1 to ignore years).
   * @param month The current month (-1 to ignore months).
   * @param day The current day (-1 to ignore days).
   * @return List<DayEntry>
   */
  public List<DayEntry> getProfiles(int year, int month, int day) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = buildsAndExecutesQuery(TABLE_PROFILES, COL_PROFILES_CURRENT_YEAR, year,
        COL_PROFILES_CURRENT_MONTH, month, COL_PROFILES_CURRENT_DAY, day);
    if (c.moveToFirst()) {
      do {
        list.add(fillProfile(c));
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.comparing(DayEntry::getName));
    return list;
  }
  /**
   * Returns profile by name.
   * @param name The profile name.
   * @return DayEntry or null if not found.
   */
  public DayEntry getProfile(String name) {
    DayEntry profile = null;
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + TABLE_PROFILES +
        " WHERE " + COL_PROFILES_NAME + "=?", new String[]{ name.replaceAll("'", "\\'") });
    if (c.moveToFirst()) {
      profile = fillProfile(c);
    }
    c.close();
    return profile;
  }
  /**
   * Removes a public holiday.
   * @param de The entry to remove.
   */
  public void removePublicHoliday(final DayEntry de) {
    getBdd().delete(TABLE_PUBLIC_HOLIDAYS, COL_PUBLIC_HOLIDAYS_NAME + "=?", new String[]{de.getName().replaceAll("'", "\\'")});
  }

  /**
   * Removes a day.
   * @param de The entry to remove.
   */
  public void removeDay(final DayEntry de) {
    WorkTimeDay d = de.getDay();
    getBdd().delete(TABLE_DAYS, COL_DAYS_CURRENT_YEAR + "=? and " + COL_DAYS_CURRENT_MONTH + "=? and " + COL_DAYS_CURRENT_DAY + "=?",
        new String[]{d.getYearString(), d.getMonthString(), d.getDayString()});
  }

  /**
   * Removes a profile.
   * @param de The entry to remove.
   */
  public void removeProfile(final DayEntry de) {
    getBdd().delete(TABLE_PROFILES, COL_PROFILES_NAME + "=?", new String[]{de.getName().replaceAll("'", "\\'")});
  }

}
