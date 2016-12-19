package fr.ralala.worktime.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;

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

  public void open() {
    bdd = helper.getWritableDatabase();
  }

  public void close() {
    bdd.close();
  }

  public long insertProfile(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PROFILES_NAME, de.getName());
    values.put(COL_PROFILES_CURRENT, de.getDay().dateString());
    values.put(COL_PROFILES_START, de.getStart().timeString());
    values.put(COL_PROFILES_END, de.getEnd().timeString());
    values.put(COL_PROFILES_PAUSE, de.getPause().timeString());
    values.put(COL_PROFILES_TYPE, de.getType().value());
    values.put(COL_PROFILES_AMOUNT, String.valueOf(de.getAmountByHour()));
    return bdd.insert(TABLE_PROFILES, null, values);
  }

  public long insertDay(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_DAYS_CURRENT, de.getDay().dateString());
    values.put(COL_DAYS_START, de.getStart().timeString());
    values.put(COL_DAYS_END, de.getEnd().timeString());
    values.put(COL_DAYS_PAUSE, de.getPause().timeString());
    values.put(COL_DAYS_TYPE, de.getType().value());
    values.put(COL_DAYS_AMOUNT, String.valueOf(de.getAmountByHour()));
    return bdd.insert(TABLE_DAYS, null, values);
  }

  public long insertPublicHoliday(final DayEntry de) {
    final ContentValues values = new ContentValues();
    values.put(COL_PUBLIC_HOLIDAYS_NAME, de.getName());
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
        final DayEntry de = new DayEntry(wtd, DayType.PUBLIC_HOLIDAY);
        de.setName(c.getString(NUM_PUBLIC_HOLIDAYS_NAME));
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
        final DayEntry de = new DayEntry(wtd, DayType.compute(c.getInt(NUM_DAYS_TYPE)));
        de.setStart(c.getString(NUM_DAYS_START));
        de.setEnd(c.getString(NUM_DAYS_END));
        de.setPause(c.getString(NUM_DAYS_PAUSE));
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
        final DayEntry de = new DayEntry(wtd, DayType.compute(c.getInt(NUM_PROFILES_TYPE)));
        de.setStart(c.getString(NUM_PROFILES_START));
        de.setEnd(c.getString(NUM_PROFILES_END));
        de.setPause(c.getString(NUM_PROFILES_PAUSE));
        String s = c.getString(NUM_PROFILES_AMOUNT);
        if(s != null && !s.isEmpty())
          de.setAmountByHour(Double.parseDouble(s));
        de.setName(c.getString(NUM_PROFILES_NAME));

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

  public void removeAll() {
    bdd.execSQL("DROP TABLE IF EXISTS " + TABLE_PUBLIC_HOLIDAYS + ";");
    bdd.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYS + ";");
    bdd.execSQL("DROP TABLE IF EXISTS " + TABLE_PROFILES + ";");
  }
}
