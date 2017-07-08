package fr.ralala.worktime.sql;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.R;
import fr.ralala.worktime.activities.SettingsActivity;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.Setting;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.services.DropboxAutoExportService;
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
  private SQLiteDatabase bdd     = null;
  private SqlHelper           helper  = null;
  private Context context = null;

  public Context getContext() {
    return context;
  }

  public SqlFactory(final Context context) throws Exception {
    helper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
    this.context = context;
  }

  public void open(Context c) {
    bdd = helper.getWritableDatabase();
    int version = 0;

    if(isTableExists(TABLE_PROFILES + "_v1")) {
      version = 1;
      Log.d(getClass().getSimpleName(), "TABLE_PROFILES found");
      final List<DayEntry> listOld = readV1("profiles", 1, 5, 2, 3, 4, 6, 0);
      final List<DayEntry> listNew = new ArrayList<>();
      v1tov2(listOld, listNew);
      Log.d(getClass().getSimpleName(), "delete table profiles");
      bdd.delete(TABLE_PROFILES, null, null);
      Log.d(getClass().getSimpleName(), "table profiles deleted");
      for(DayEntry de : listNew)
        insertProfile(de);
      removeTable(TABLE_PROFILES + "_v" + version);
    }
    if(isTableExists(TABLE_DAYS + "_v1")) {
      version = 1;
      Log.d(getClass().getSimpleName(), "TABLE_DAYS found");
      final List<DayEntry> listOld = readV1("days", 0, 4, 1, 2, 3, 5, -1);
      final List<DayEntry> listNew = new ArrayList<>();
      v1tov2(listOld, listNew);
      Log.d(getClass().getSimpleName(), "delete table days");
      bdd.delete(TABLE_DAYS, null, null);
      Log.d(getClass().getSimpleName(), "table days deleted");
      for(DayEntry de : listNew)
        insertDay(de);
      removeTable(TABLE_DAYS + "_v" + version);
    }

    if(isTableExists(TABLE_PROFILES + "_v2")) {
      version = 2;
      Log.d(getClass().getSimpleName(), "TABLE_PROFILES found");
      final List<DayEntry> listOld = readProfilesV2(0, 1, 2, 3, 4, 5, 6, 7);
      Log.d(getClass().getSimpleName(), "delete table profiles");
      bdd.delete(TABLE_PROFILES, null, null);
      Log.d(getClass().getSimpleName(), "table profiles deleted");
      for(DayEntry de : listOld)
        insertProfile(de);
      removeTable(TABLE_PROFILES + "_v" + version);
    }
    if(isTableExists(TABLE_PUBLIC_HOLIDAYS + "_v3")) {
      version = 3;
      Log.e(getClass().getSimpleName(), "PUBLIC_HOLIDAYS found");
      final List<DayEntry> listOld = getPublicHolidays(TABLE_PUBLIC_HOLIDAYS + "_v" + version);
      Log.e(getClass().getSimpleName(), "delete table public holidays");
      bdd.delete(TABLE_PUBLIC_HOLIDAYS, null, null);
      Log.e(getClass().getSimpleName(), "table public holidays deleted");
      for(DayEntry de : listOld) {
        insertPublicHoliday(de);
      }
      removeTable(TABLE_PUBLIC_HOLIDAYS + "_v" + version);
    }
    if(isTableExists(TABLE_DAYS + "_v3")) {
      version = 3;
      Log.e(getClass().getSimpleName(), "TABLE_DAYS found");
      final List<DayEntry> listOld = getDays(TABLE_DAYS + "_v" + version);
      Log.e(getClass().getSimpleName(), "delete table days");
      bdd.delete(TABLE_DAYS, null, null);
      Log.e(getClass().getSimpleName(), "table days deleted");
      for(DayEntry de : listOld) {
        insertDay(de);
      }
      removeTable(TABLE_DAYS + "_v" + version);
    }
    if(isTableExists(TABLE_PROFILES + "_v3")) {
      version = 3;
      Log.e(getClass().getSimpleName(), "TABLE_PROFILES found");
      final List<DayEntry> listOld = getProfiles(TABLE_PROFILES + "_v" + version);
      Log.e(getClass().getSimpleName(), "delete table profiles");
      bdd.delete(TABLE_PROFILES, null, null);
      Log.e(getClass().getSimpleName(), "table profiles deleted");
      for(DayEntry de : listOld) {
        insertProfile(de);
      }
      removeTable(TABLE_PROFILES + "_v" + version);
    }
    if(isTableExists(TABLE_SETTINGS + "_v4")) {
      version = 4;
      removeTable(TABLE_SETTINGS + "_v" + version);
      settingsSave();
    }
    if(version != 0)
      AndroidHelper.restartApplication(c, context.getString(R.string.restart_from_db_update_vn) + " " + (version + 1));
  }

  public void close() {
    bdd.close();
  }

  private void v1tov2(List<DayEntry> listOld, List<DayEntry> listNew) {
    for(DayEntry de : listOld) {
      final DayEntry d = new DayEntry(context, de.getDay(), de.getTypeMorning(), de.getTypeAfternoon());
      d.copy(de);
      WorkTimeDay start = de.getStartMorning();
      WorkTimeDay end = de.getEndMorning();
      WorkTimeDay pause = de.getStartAfternoon();

      WorkTimeDay w = end.clone();
      w.delTime(start.clone());
      w.delTime(pause.clone());

      long l = w.getTimeMs();

      d.setStartMorning(start.timeString());
      d.setEndMorning(new WorkTimeDay(start.getTimeMs() + l/2).timeString());
      d.setStartAfternoon(new WorkTimeDay(d.getEndMorning().getTimeMs()+pause.getTimeMs()).timeString());
      d.setEndAfternoon(end.timeString());
      listNew.add(d);
    }
  }

  private List<DayEntry> readV1(final String tablename_v1, final int c_current, final int c_type, final int c_start, final int c_end, final int c_pause, final int c_amount, final int c_name) {
    final List<DayEntry> list = new ArrayList<>();
    Cursor c = bdd.rawQuery("SELECT * FROM " + tablename_v1 + "_v1", null);
    if (c.moveToFirst()) {
      do {
        String[] split = c.getString(c_current).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));
        final DayEntry de = new DayEntry(context, wtd, DayType.compute(c.getInt(c_type)), DayType.compute(c.getInt(c_type)));
        de.setStartMorning(c.getString(c_start)); /* start */
        de.setEndMorning(c.getString(c_end)); /* end */
        de.setStartAfternoon(c.getString(c_pause)); /* pause */
        String s = c.getString(c_amount);
        if (s != null && !s.isEmpty())
          de.setAmountByHour(Double.parseDouble(s));
        if(c_name != -1)
          de.setName(c.getString(c_name));

        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
    Collections.sort(list, new Comparator<DayEntry>() {
      @Override
      public int compare(final DayEntry lhs, final DayEntry rhs) {
        return c_name != -1 ? lhs.getName().compareTo(rhs.getName()) : lhs.getDay().compareTo(rhs.getDay());
      }
    });
    return list;
  }

  private List<DayEntry> readProfilesV2(final int c_name, final int c_current, final int c_start_m, final int c_end_m, final int c_start_a, final int c_end_a, final int c_type, final int c_amount) {
    final List<DayEntry> listProfiles = new ArrayList<>();
    Cursor c = bdd.rawQuery("SELECT * FROM " + TABLE_PROFILES + "_v2", null);
    if (c.moveToFirst()) {
      do {
        String[] split = c.getString(c_current).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));
        final DayEntry de = new DayEntry(context, wtd, DayType.compute(c.getInt(c_type)), DayType.compute(c.getInt(c_type)));
        de.setStartMorning(c.getString(c_start_m)); /* start morning*/
        de.setEndMorning(c.getString(c_end_m)); /* end morning*/
        de.setStartAfternoon(c.getString(c_start_a)); /* start afternoon*/
        de.setEndAfternoon(c.getString(c_end_a)); /* end afternoon*/
        String s = c.getString(c_amount);
        if (s != null && !s.isEmpty())
          de.setAmountByHour(Double.parseDouble(s));
        de.setName(c.getString(c_name));
        listProfiles.add(de);
      } while (c.moveToNext());
    }
    c.close();
    Collections.sort(listProfiles, new Comparator<DayEntry>() {
      @Override
      public int compare(final DayEntry lhs, final DayEntry rhs) {
        return lhs.getName().compareTo(rhs.getName());
      }
    });
    return listProfiles;
  }

  public boolean isTableExists(String tableName) {
    Cursor cursor = bdd.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
    if(cursor!=null) {
      if(cursor.getCount()>0) {
        cursor.close();
        return true;
      }
      cursor.close();
    }
    return false;
  }

  public long updateProfile(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PROFILES_LEARNING_WEIGHT, "" + de.getLearningWeight());
    return bdd.update(TABLE_PROFILES, values, COL_PROFILES_NAME + "='"+de.getName().replaceAll("'", "\\'") + "'", null);
  }

  public void settingsSave() {
    if(bdd.getVersion() < VERSION_MIN_SETTINGS) return;
    bdd.delete(TABLE_SETTINGS, null, null);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
    addSetting(prefs, SettingsActivity.PREFS_KEY_DEFAULT_HOME, SettingsActivity.PREFS_DEFVAL_DEFAULT_HOME);
    addSetting(prefs, SettingsActivity.PREFS_KEY_PROFILES_WEIGHT_DEPTH, SettingsActivity.PREFS_DEFVAL_PROFILES_WEIGHT_DEPTH);
    addSetting(prefs, SettingsActivity.PREFS_KEY_DAY_ROWS_HEIGHT, SettingsActivity.PREFS_DEFVAL_DAY_ROWS_HEIGHT);
    addSetting(prefs, SettingsActivity.PREFS_KEY_WORKTIME_BY_DAY, SettingsActivity.PREFS_DEFVAL_WORKTIME_BY_DAY);
    addSetting(prefs, SettingsActivity.PREFS_KEY_AMOUNT_BY_HOUR, SettingsActivity.PREFS_DEFVAL_AMOUNT_BY_HOUR);
    addSetting(prefs, SettingsActivity.PREFS_KEY_CURRENCY, SettingsActivity.PREFS_DEFVAL_CURRENCY);
    addSetting(prefs, SettingsActivity.PREFS_KEY_EMAIL, SettingsActivity.PREFS_DEFVAL_EMAIL);
    addSetting(prefs, SettingsActivity.PREFS_KEY_EMAIL_ENABLE, SettingsActivity.PREFS_DEFVAL_EMAIL_ENABLE.equals("true"));
    addSetting(prefs, SettingsActivity.PREFS_KEY_HIDE_WAGE, SettingsActivity.PREFS_DEFVAL_HIDE_WAGE.equals("true"));
    addSetting(prefs, SettingsActivity.PREFS_KEY_EXPORT_HIDE_WAGE, SettingsActivity.PREFS_DEFVAL_EXPORT_HIDE_WAGE.equals("true"));
    addSetting(prefs, SettingsActivity.PREFS_KEY_SCROLL_TO_CURRENT_DAY, SettingsActivity.PREFS_DEFVAL_SCROLL_TO_CURRENT_DAY.equals("true"));
    addSetting(prefs, SettingsActivity.PREFS_KEY_HIDE_EXIT_BUTTON, SettingsActivity.PREFS_DEFVAL_HIDE_EXIT_BUTTON.equals("true"));
    addSetting(prefs, SettingsActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE, SettingsActivity.PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE.equals("false"));
    addSetting(prefs, SettingsActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY, SettingsActivity.PREFS_DEFVAL_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY);
    addSetting(prefs, DropboxAutoExportService.KEY_NEED_UPDATE, DropboxAutoExportService.DEFVAL_NEED_UPDATE.equals("false"));
  }

  public void settingsLoad(List<Setting> settings) {
    if(bdd.getVersion() < VERSION_MIN_SETTINGS) return;
    final Cursor c = bdd.rawQuery("SELECT * FROM " + TABLE_SETTINGS, null);
    if (c.moveToFirst()) {
      do {
        String name = c.getString(NUM_SETTINGS_NAME);
        String value = c.getString(NUM_SETTINGS_VALUE);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        SharedPreferences.Editor edit = prefs.edit();
        if(name.equals(SettingsActivity.PREFS_KEY_DEFAULT_HOME) || name.equals(SettingsActivity.PREFS_KEY_PROFILES_WEIGHT_DEPTH)
            || name.equals(SettingsActivity.PREFS_KEY_DAY_ROWS_HEIGHT) || name.equals(SettingsActivity.PREFS_KEY_WORKTIME_BY_DAY)
            || name.equals(SettingsActivity.PREFS_KEY_AMOUNT_BY_HOUR) || name.equals(SettingsActivity.PREFS_KEY_CURRENCY )
            || name.equals(SettingsActivity.PREFS_KEY_EMAIL) || name.equals(SettingsActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE_PERIODICITY))
          edit.putString(name, value);
        else if(name.equals(SettingsActivity.PREFS_KEY_EMAIL_ENABLE) || name.equals(SettingsActivity.PREFS_KEY_HIDE_WAGE)
            || name.equals(SettingsActivity.PREFS_KEY_EXPORT_HIDE_WAGE) || name.equals(SettingsActivity.PREFS_KEY_SCROLL_TO_CURRENT_DAY)
            || name.equals(SettingsActivity.PREFS_KEY_HIDE_EXIT_BUTTON) || name.equals(SettingsActivity.PREFS_KEY_IMPORT_EXPORT_AUTO_SAVE)
            || name.equals(DropboxAutoExportService.KEY_NEED_UPDATE))
          edit.putBoolean(name, value.equals("1"));
        if(settings != null)
          settings.add(new Setting(name, value));
        edit.apply();
      } while (c.moveToNext());
    }
    c.close();
  }

  private long addSetting(SharedPreferences prefs, String key, String defvalue) {
    final ContentValues values = new ContentValues();
    values.put(COL_SETTINGS_NAME, key);
    values.put(COL_SETTINGS_VALUE, prefs.getString(key, defvalue));
    return bdd.insert(TABLE_SETTINGS, null, values);
  }
  private long addSetting(SharedPreferences prefs, String key, boolean defvalue) {
    final ContentValues values = new ContentValues();
    values.put(COL_SETTINGS_NAME, key);
    values.put(COL_SETTINGS_VALUE, prefs.getBoolean(key, defvalue));
    return bdd.insert(TABLE_SETTINGS, null, values);
  }

  public long insertProfile(final DayEntry de) {
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
    return bdd.insert(TABLE_PROFILES, null, values);
  }

  public long insertDay(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_DAYS_CURRENT, de.getDay().dateString());
    values.put(COL_DAYS_START_MORNING, de.getStartMorning().timeString());
    values.put(COL_DAYS_END_MORNING, de.getEndMorning().timeString());
    values.put(COL_DAYS_START_AFTERNOON, de.getStartAfternoon().timeString());
    values.put(COL_DAYS_END_AFTERNOON, de.getEndAfternoon().timeString());
    values.put(COL_DAYS_TYPE, de.getTypeMorning().value() + "|" + de.getTypeAfternoon().value());
    values.put(COL_DAYS_AMOUNT, String.valueOf(de.getAmountByHour()));
    values.put(COL_DAYS_LEGAL_WORKTIME, de.getLegalWorktime().timeString());
    return bdd.insert(TABLE_DAYS, null, values);
  }

  public long insertPublicHoliday(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PUBLIC_HOLIDAYS_NAME, de.getName().replaceAll("'", "\\'"));
    values.put(COL_PUBLIC_HOLIDAYS_DATE, de.getDay().dateString());
    values.put(COL_PUBLIC_HOLIDAYS_RECURRENCE, de.isRecurrence() ? "1" : "0");
    return bdd.insert(TABLE_PUBLIC_HOLIDAYS, null, values);
  }

  public List<DayEntry> getPublicHolidays() {
    return getPublicHolidays(TABLE_PUBLIC_HOLIDAYS);
  }

  public List<DayEntry> getPublicHolidays(String tablename) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = bdd.rawQuery("SELECT * FROM " + tablename, null);
    if (c.moveToFirst()) {
      do {
        String [] split = c.getString(NUM_PUBLIC_HOLIDAYS_DATE).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));
        final DayEntry de = new DayEntry(context, wtd, DayType.PUBLIC_HOLIDAY, DayType.PUBLIC_HOLIDAY);
        de.setName(c.getString(NUM_PUBLIC_HOLIDAYS_NAME).replaceAll("\\'", "'"));
        if(bdd.getVersion() >= VERSION_MIN_RECURRENCE_LEGAL_WT && c.getColumnCount() > NUM_PUBLIC_HOLIDAYS_RECURRENCE)
          de.setRecurrence(c.getString(NUM_PUBLIC_HOLIDAYS_RECURRENCE).equals("1"));
        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
    return list;
  }

  private int getInt(String s, int def) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return def;
    }
  }
  public List<DayEntry> getDays() {
    return getDays(TABLE_DAYS);
  }

  public List<DayEntry> getDays(String tablename) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = bdd.rawQuery("SELECT * FROM " + tablename, null);
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
        final DayEntry de = new DayEntry(context, wtd, dta, dtb);
        de.setStartMorning(c.getString(NUM_DAYS_START_MORNING));
        de.setEndMorning(c.getString(NUM_DAYS_END_MORNING));
        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_DAYS_AMOUNT)
          de.setStartAfternoon(c.getString(NUM_DAYS_START_AFTERNOON));
        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_DAYS_AMOUNT)
          de.setEndAfternoon(c.getString(NUM_DAYS_END_AFTERNOON));
        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_DAYS_LEGAL_WORKTIME)
          de.setLegalWorktime(c.getString(NUM_DAYS_LEGAL_WORKTIME));
        String s = c.getString(NUM_DAYS_AMOUNT);
        if(s != null && !s.isEmpty())
          de.setAmountByHour(Double.parseDouble(s));
        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
    Collections.sort(list, new Comparator<DayEntry>() {
      @Override
      public int compare(final DayEntry lhs, final DayEntry rhs) {
        return lhs.getDay().compareTo(rhs.getDay());
      }
    });
    return list;
  }

  public List<DayEntry> getProfiles() {
    return getProfiles(TABLE_PROFILES);
  }
  public List<DayEntry> getProfiles(String tablename) {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = bdd.rawQuery("SELECT * FROM " + tablename, null);
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
        final DayEntry de = new DayEntry(context, wtd, dta, dtb);
        de.setStartMorning(c.getString(NUM_PROFILES_START_MORNING));
        de.setEndMorning(c.getString(NUM_PROFILES_END_MORNING));
        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_PROFILES_AMOUNT)
          de.setStartAfternoon(c.getString(NUM_PROFILES_START_AFTERNOON));
        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_PROFILES_AMOUNT)
          de.setEndAfternoon(c.getString(NUM_PROFILES_END_AFTERNOON));
        String s = c.getString(NUM_PROFILES_AMOUNT);
        if(s != null && !s.isEmpty())
          de.setAmountByHour(Double.parseDouble(s));
        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_PROFILES_LEARNING_WEIGHT)
          s = c.getString(NUM_PROFILES_LEARNING_WEIGHT);
        if(s != null && !s.isEmpty())
          de.setLearningWeight(Integer.parseInt(s));
        de.setName(c.getString(NUM_PROFILES_NAME).replaceAll("\\'", "'"));

        if(bdd.getVersion() >= VERSION_MIN_MORNING_AFTERNOON && c.getColumnCount() > NUM_PROFILES_LEGAL_WORKTIME)
          de.setLegalWorktime(c.getString(NUM_PROFILES_LEGAL_WORKTIME));
        list.add(de);
      } while (c.moveToNext());
    }
    c.close();
    Collections.sort(list, new Comparator<DayEntry>() {
      @Override
      public int compare(final DayEntry lhs, final DayEntry rhs) {
        return lhs.getName().compareTo(rhs.getName());
      }
    });
    return list;
  }

  public int removePublicHoliday(final DayEntry de) {
    return bdd.delete(TABLE_PUBLIC_HOLIDAYS, COL_PUBLIC_HOLIDAYS_NAME + " = \"" + de.getName() + "\"", null);
  }

  public int removeDay(final DayEntry de) {
    return bdd.delete(TABLE_DAYS, COL_DAYS_CURRENT + " = \"" + de.getDay().dateString() + "\"", null);
  }

  public int removeProfile(final DayEntry de) {
    return bdd.delete(TABLE_PROFILES, COL_PROFILES_NAME + " = \"" + de.getName() + "\"", null);
  }

  public void removeTable(final String tablename) {
    bdd.execSQL("DROP TABLE IF EXISTS " + tablename + ";");
  }

  public void removeAll() {
    bdd.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLIC_HOLIDAYS + ";");
    bdd.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYS + ";");
    bdd.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES + ";");
  }
}
