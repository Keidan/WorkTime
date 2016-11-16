package fr.ralala.worktime.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

  // Copy to sdcard for debug use
  public static void copyDatabase(final Context c, final String name,
                                     final String folder) throws Exception{
    final String databasePath = c.getDatabasePath(name).getPath();
    final File f = new File(databasePath);
    OutputStream myOutput = null;
    InputStream myInput = null;
    Log.d(SqlHelper.class.getSimpleName(), " db path " + databasePath);
    Log.d(SqlHelper.class.getSimpleName(), " db exist " + f.exists());
    Exception exception = null;
    if (f.exists()) {
      try {

        final File directory = new File(folder);
        if (!directory.exists())
          directory.mkdir();
        File out = new File(directory, new SimpleDateFormat("yyyyMMdd_hhmm", Locale.US).format(new Date()) + "_" + name);
        myOutput = new FileOutputStream(out);
        myInput = new FileInputStream(databasePath);

        final byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
          myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
      } catch (final Exception e) {
        exception = e;
      } finally {
        try {
          if (myOutput != null)
            myOutput.close();
          if (myInput != null)
            myInput.close();
        } catch (final Exception e) {
          Log.e(SqlHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
        }
      }
    }
    if(exception != null) throw exception;
  }

  public static void loadDatabase(Context c, final String name, File in) throws Exception{
    final String databasePath = c.getDatabasePath(name).getPath();
    final File f = new File(databasePath);
    InputStream myInput = null;
    OutputStream myOutput = null;
    Exception exception = null;
    try {
      myInput = new FileInputStream(in.getAbsolutePath());
      String outFileName = f.getAbsolutePath();
      myOutput = new FileOutputStream(outFileName);
      byte[] buffer = new byte[1024];
      int length;
      while ((length = myInput.read(buffer))>0){
        myOutput.write(buffer, 0, length);
      }
      myOutput.flush();
    } catch (final Exception e) {
      exception = e;
    } finally {
      try {
        if (myOutput != null)
          myOutput.close();
        if (myInput != null)
          myInput.close();
      } catch (final Exception e) {
        Log.e(SqlHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
      }
    }
    if(exception != null) throw exception;
  }
}
