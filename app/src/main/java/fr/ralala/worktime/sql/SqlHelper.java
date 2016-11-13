package fr.ralala.worktime.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * SQL helper
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SqlHelper extends SQLiteOpenHelper implements SqlConstants {

  private static final String CREATE_BDD_PROFILES = "CREATE TABLE IF NOT EXISTS "
    + TABLE_PROFILES
    + " ("
    + COL_PROFILES_NAME
    + " TEXT NOT NULL, "
    + COL_PROFILES_CURRENT
    + " TEXT NOT NULL, "
    + COL_PROFILES_START
    + " TEXT NOT NULL, "
    + COL_PROFILES_END
    + " TEXT NOT NULL, "
    + COL_PROFILES_PAUSE
    + " TEXT NOT NULL, "
    + COL_PROFILES_TYPE
    + " TEXT NOT NULL, "
    + COL_PROFILES_AMOUNT
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_DAYS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_DAYS
    + " ("
    + COL_DAYS_CURRENT
    + " TEXT NOT NULL, "
    + COL_DAYS_START
    + " TEXT NOT NULL, "
    + COL_DAYS_END
    + " TEXT NOT NULL, "
    + COL_DAYS_PAUSE
    + " TEXT NOT NULL, "
    + COL_DAYS_TYPE
    + " TEXT NOT NULL, "
    + COL_DAYS_AMOUNT
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_PUBLIC_HOLIDAYS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_PUBLIC_HOLIDAYS
    + " ("
    + COL_PUBLIC_HOLIDAYS_NAME
    + " TEXT NOT NULL, "
    + COL_PUBLIC_HOLIDAYS_DATE
    + " TEXT NOT NULL);";

  public SqlHelper(final Context context, final String name,
                   final SQLiteDatabase.CursorFactory factory, final int version) {
    super(context, name, factory, version);
  }

  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL(CREATE_BDD_PUBLIC_HOLIDAYS);
    db.execSQL(CREATE_BDD_DAYS);
    db.execSQL(CREATE_BDD_PROFILES);
  }

  @Override
  public void onOpen(final SQLiteDatabase db) {
    onCreate(db);
  }

  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                        final int newVersion) {
  }

}
