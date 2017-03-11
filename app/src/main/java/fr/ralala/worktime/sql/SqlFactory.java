package fr.ralala.worktime.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
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

  public SqlFactory(final Context context) throws Exception {
    helper = new SqlHelper(context, DB_NAME, null, VERSION_BDD);
  }

  public void open(Context c) {
    bdd = helper.getWritableDatabase();
    boolean restart = false;

    if(isTableExists(TABLE_PROFILES + "_v1")) {
      Log.d(getClass().getSimpleName(), "TABLE_PROFILES found");
      final List<DayEntry> listOld = readV1("profiles", 1, 5, 2, 3, 4, 6, 0);
      final List<DayEntry> listNew = new ArrayList<>();
      v1tov2(listOld, listNew);
      Log.d(getClass().getSimpleName(), "delete table profiles");
      bdd.delete(TABLE_PROFILES, null, null);
      Log.d(getClass().getSimpleName(), "table profiles deleted");
      for(DayEntry de : listNew)
        insertProfile(de);
      removeTable(TABLE_PROFILES + "_v1");
      restart = true;
    }
    if(isTableExists(TABLE_DAYS + "_v1")) {
      Log.d(getClass().getSimpleName(), "TABLE_DAYS found");
      final List<DayEntry> listOld = readV1("days", 0, 4, 1, 2, 3, 5, -1);
      final List<DayEntry> listNew = new ArrayList<>();
      v1tov2(listOld, listNew);
      Log.d(getClass().getSimpleName(), "delete table days");
      bdd.delete(TABLE_DAYS, null, null);
      Log.d(getClass().getSimpleName(), "table days deleted");
      for(DayEntry de : listNew)
        insertDay(de);
      removeTable(TABLE_DAYS + "_v1");
      restart = true;
    }

    if(isTableExists(TABLE_PROFILES + "_v2")) {
      Log.d(getClass().getSimpleName(), "TABLE_PROFILES found");
      final List<DayEntry> listOld = readProfilesV2(0, 1, 2, 3, 4, 5, 6, 7);
      Log.d(getClass().getSimpleName(), "delete table profiles");
      bdd.delete(TABLE_PROFILES, null, null);
      Log.d(getClass().getSimpleName(), "table profiles deleted");
      for(DayEntry de : listOld) /* reload weight */
        insertProfile(de);
      removeTable(TABLE_PROFILES + "_v2");
      restart = true;
    }
    if(restart)
      AndroidHelper.restartApplication(c);
  }

  public void close() {
    bdd.close();
  }

  private void v1tov2(List<DayEntry> listOld, List<DayEntry> listNew) {
    for(DayEntry de : listOld) {
      final DayEntry d = new DayEntry(de.getDay(), de.getTypeMorning(), de.getTypeAfternoon());
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
        final DayEntry de = new DayEntry(wtd, DayType.compute(c.getInt(c_type)), DayType.compute(c.getInt(c_type)));
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
        final DayEntry de = new DayEntry(wtd, DayType.compute(c.getInt(c_type)), DayType.compute(c.getInt(c_type)));
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
    return bdd.insert(TABLE_DAYS, null, values);
  }

  public long insertPublicHoliday(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PUBLIC_HOLIDAYS_NAME, de.getName().replaceAll("'", "\\'"));
    values.put(COL_PUBLIC_HOLIDAYS_DATE, de.getDay().dateString());
    return bdd.insert(TABLE_PUBLIC_HOLIDAYS, null, values);
  }

  public List<DayEntry> getPublicHolidays() {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = bdd.rawQuery("SELECT * FROM " + TABLE_PUBLIC_HOLIDAYS, null);
    if (c.moveToFirst()) {
      do {
        String [] split = c.getString(NUM_PUBLIC_HOLIDAYS_DATE).split("/");
        WorkTimeDay wtd = new WorkTimeDay();
        wtd.setDay(Integer.parseInt(split[0]));
        wtd.setMonth(Integer.parseInt(split[1]));
        wtd.setYear(Integer.parseInt(split[2]));
        final DayEntry de = new DayEntry(wtd, DayType.PUBLIC_HOLIDAY, DayType.PUBLIC_HOLIDAY);
        de.setName(c.getString(NUM_PUBLIC_HOLIDAYS_NAME).replaceAll("\\'", "'"));
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

  private int getInt(String s, int def) {
    try {
      return Integer.parseInt(s);
    } catch (Exception e) {
      return def;
    }
  }

  public List<DayEntry> getDays() {
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = bdd.rawQuery("SELECT * FROM " + TABLE_DAYS, null);
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
        final DayEntry de = new DayEntry(wtd, dta, dtb);
        de.setStartMorning(c.getString(NUM_DAYS_START_MORNING));
        de.setEndMorning(c.getString(NUM_DAYS_END_MORNING));
        de.setStartAfternoon(c.getString(NUM_DAYS_START_AFTERNOON));
        de.setEndAfternoon(c.getString(NUM_DAYS_END_AFTERNOON));
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
    final List<DayEntry> list = new ArrayList<>();
    final Cursor c = bdd.rawQuery("SELECT * FROM " + TABLE_PROFILES, null);
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
          String [] sp = types.split("|");
          dta = DayType.compute(getInt(sp[0], DayType.ERROR.value()));
          dtb = DayType.compute(getInt(sp[1], DayType.ERROR.value()));
        } else {
          dta = DayType.compute(c.getInt(NUM_PROFILES_TYPE));
          dtb = dta;
        }
        final DayEntry de = new DayEntry(wtd, dta, dtb);
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
