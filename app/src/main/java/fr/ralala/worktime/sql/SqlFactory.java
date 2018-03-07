package fr.ralala.worktime.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
import fr.ralala.worktime.utils.AndroidHelper;

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
  private SqlHelper mHelper = null;
  private Context mContext = null;

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
   * @throws Exception If an exception is reached.
   */
  public SqlFactory(final Context context) throws Exception {
    mHelper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
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
    int version = 0;
    /* Merge V5 */
    if(isTableExists(TABLE_DAYS + "_v5")) {
      version = 5;
      Log.e(getClass().getSimpleName(), "TABLE_DAYS found");
      final List<DayEntry> listOld = getDays(TABLE_DAYS + "_v" + version);
      removeTable(TABLE_DAYS + "_v" + version);
      Log.e(getClass().getSimpleName(), "table days deleted");
      for(DayEntry de : listOld) {
        insertDay(de);
      }
    }
    if(isTableExists(TABLE_PROFILES + "_v5")) {
      version = 5;
      Log.e(getClass().getSimpleName(), "TABLE_PROFILES found");
      final List<DayEntry> listOld = getProfiles(TABLE_PROFILES + "_v" + version);
      removeTable(TABLE_PROFILES + "_v" + version);
      Log.e(getClass().getSimpleName(), "table profiles deleted");
      for(DayEntry de : listOld) {
        insertProfile(de);
      }
    }


    if(version != 0)
      AndroidHelper.restartApplication(mContext, mContext.getString(R.string.restart_from_db_update_vn) + " " + (version + 1));
  }

  /**
   * Closes the SQLite connection.
   */
  public void close() {
    getBdd().close();
  }

  /**
   * Tests if the table exists.
   * @param tableName The table name to test.
   * @return boolean
   */
  private boolean isTableExists(String tableName) {
    Cursor cursor = getBdd().rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
    if(cursor!=null) {
      if(cursor.getCount()>0) {
        cursor.close();
        return true;
      }
      cursor.close();
    }
    return false;
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
    values.put(COL_PROFILES_CURRENT, de.getDay().dateString());
    values.put(COL_PROFILES_START_MORNING, de.getStartMorning().timeString());
    values.put(COL_PROFILES_END_MORNING, de.getEndMorning().timeString());
    values.put(COL_PROFILES_START_AFTERNOON, de.getStartAfternoon().timeString());
    values.put(COL_PROFILES_END_AFTERNOON, de.getEndAfternoon().timeString());
    values.put(COL_PROFILES_TYPE, de.getTypeMorning().value() + "|" + de.getTypeAfternoon().value());
    values.put(COL_PROFILES_AMOUNT, String.valueOf(de.getAmountByHour()));
    values.put(COL_PROFILES_LEARNING_WEIGHT, String.valueOf(de.getLearningWeight()));
    values.put(COL_PROFILES_LEGAL_WORKTIME, de.getLegalWorktime().timeString());
    values.put(COL_PROFILES_ADDITIONAL_BREAK, de.getAdditionalBreak().timeString());
    getBdd().insert(TABLE_PROFILES, null, values);
  }

  /**
   * Inserts a day.
   * @param de The day to insert.
   */
  public void insertDay(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_DAYS_CURRENT, de.getDay().dateString());
    values.put(COL_DAYS_START_MORNING, de.getStartMorning().timeString());
    values.put(COL_DAYS_END_MORNING, de.getEndMorning().timeString());
    values.put(COL_DAYS_START_AFTERNOON, de.getStartAfternoon().timeString());
    values.put(COL_DAYS_END_AFTERNOON, de.getEndAfternoon().timeString());
    values.put(COL_DAYS_TYPE, de.getTypeMorning().value() + "|" + de.getTypeAfternoon().value());
    values.put(COL_DAYS_AMOUNT, String.valueOf(de.getAmountByHour()));
    values.put(COL_DAYS_LEGAL_WORKTIME, de.getLegalWorktime().timeString());
    values.put(COL_DAYS_ADDITIONAL_BREAK, de.getAdditionalBreak().timeString());
    getBdd().insert(TABLE_DAYS, null, values);
  }

  /**
   * Inserts a public holiday.
   * @param de The public holiday to insert.
   */
  public void insertPublicHoliday(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PUBLIC_HOLIDAYS_NAME, de.getName().replaceAll("'", "\\'"));
    values.put(COL_PUBLIC_HOLIDAYS_DATE, de.getDay().dateString());
    values.put(COL_PUBLIC_HOLIDAYS_RECURRENCE, de.isRecurrence() ? "1" : "0");
    getBdd().insert(TABLE_PUBLIC_HOLIDAYS, null, values);
  }

  /**
   * Returns the list of public holidays.
   * @return List<DayEntry>
   */
  public List<DayEntry> getPublicHolidays() {
    return getPublicHolidays(TABLE_PUBLIC_HOLIDAYS);
  }

  /**
   * Returns the list of public holidays.
   * @param tablename The table name.
   * @return List<DayEntry>
   */
  private List<DayEntry> getPublicHolidays(String tablename) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + tablename, null);
    if (c.moveToFirst()) {
      do {
        String [] split = c.getString(NUM_PUBLIC_HOLIDAYS_DATE).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));
        final DayEntry de = new DayEntry(mContext, wtd, DayType.PUBLIC_HOLIDAY, DayType.PUBLIC_HOLIDAY);
        de.setName(c.getString(NUM_PUBLIC_HOLIDAYS_NAME).replaceAll("\\'", "'"));
          de.setRecurrence(c.getString(NUM_PUBLIC_HOLIDAYS_RECURRENCE).equals("1"));
        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
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
   * Returns the list of days.
   * @return List<DayEntry>
   */
  public List<DayEntry> getDays() {
    return getDays(TABLE_DAYS);
  }

  /**
   * Returns the list of days.
   * @param tablename The table name.
   * @return List<DayEntry>
   */
  private List<DayEntry> getDays(String tablename) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + tablename, null);
    if (c.moveToFirst()) {
      do {
        String [] split = c.getString(NUM_DAYS_CURRENT).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));
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
        String s = c.getString(NUM_DAYS_AMOUNT);
        if(s != null && !s.isEmpty())
          de.setAmountByHour(Double.parseDouble(s));
        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
    Comparator<DayEntry> comp = (lhs, rhs) -> lhs.getDay().compareTo(rhs.getDay());
    list.sort(comp);
    return list;
  }

  /**
   * Returns the list of profiles.
   * @return List<DayEntry>
   */
  public List<DayEntry> getProfiles() {
    return getProfiles(TABLE_PROFILES);
  }

  /**
   * Returns the list of profiles.
   * @param tablename The table name.
   * @return List<DayEntry>
   */
  private List<DayEntry> getProfiles(String tablename) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = getBdd().rawQuery("SELECT * FROM " + tablename, null);
    if (c.moveToFirst()) {
      do {
        String [] split = c.getString(NUM_PROFILES_CURRENT).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));

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
        de.setName(c.getString(NUM_PROFILES_NAME).replaceAll("\\'", "'"));

        de.setLegalWorktime(c.getString(NUM_PROFILES_LEGAL_WORKTIME));
        de.setAdditionalBreak(c.getString(NUM_PROFILES_ADDITIONAL_BREAK));
        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
    list.sort(Comparator.comparing(DayEntry::getName));
    return list;
  }

  /**
   * Removes a public holiday.
   * @param de The entry to remove.
   */
  public void removePublicHoliday(final DayEntry de) {
    getBdd().delete(TABLE_PUBLIC_HOLIDAYS, COL_PUBLIC_HOLIDAYS_NAME + " = \"" + de.getName() + "\"", null);
  }

  /**
   * Removes a day.
   * @param de The entry to remove.
   */
  public void removeDay(final DayEntry de) {
    getBdd().delete(TABLE_DAYS, COL_DAYS_CURRENT + " = \"" + de.getDay().dateString() + "\"", null);
  }

  /**
   * Removes a profile.
   * @param de The entry to remove.
   */
  public void removeProfile(final DayEntry de) {
    getBdd().delete(TABLE_PROFILES, COL_PROFILES_NAME + " = \"" + de.getName() + "\"", null);
  }

  /**
   * Removes a table.
   * @param tablename The table name.
   */
  private void removeTable(final String tablename) {
    getBdd().execSQL("DROP TABLE IF EXISTS " + tablename + ";");
  }
}
