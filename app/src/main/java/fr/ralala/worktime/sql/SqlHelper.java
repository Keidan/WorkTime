package fr.ralala.worktime.sql;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;

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
    + COL_PROFILES_START_MORNING
    + " TEXT NOT NULL, "
    + COL_PROFILES_END_MORNING
    + " TEXT NOT NULL, "
    + COL_PROFILES_START_AFTERNOON
    + " TEXT NOT NULL, "
    + COL_PROFILES_END_AFTERNOON
    + " TEXT NOT NULL, "
    + COL_PROFILES_TYPE
    + " TEXT NOT NULL, "
    + COL_PROFILES_AMOUNT
    + " TEXT NOT NULL, "
    + COL_PROFILES_LEARNING_WEIGHT
    + " TEXT NOT NULL, "
    + COL_PROFILES_LEGAL_WORKTIME
    + " TEXT NOT NULL, "
    + COL_PROFILES_ADDITIONAL_BREAK
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_DAYS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_DAYS
    + " ("
    + COL_DAYS_CURRENT
    + " TEXT NOT NULL, "
    + COL_DAYS_START_MORNING
    + " TEXT NOT NULL, "
    + COL_DAYS_END_MORNING
    + " TEXT NOT NULL, "
    + COL_DAYS_START_AFTERNOON
    + " TEXT NOT NULL, "
    + COL_DAYS_END_AFTERNOON
    + " TEXT NOT NULL, "
    + COL_DAYS_TYPE
    + " TEXT NOT NULL, "
    + COL_DAYS_AMOUNT
    + " TEXT NOT NULL, "
    + COL_DAYS_LEGAL_WORKTIME
    + " TEXT NOT NULL, "
    + COL_DAYS_ADDITIONAL_BREAK
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_PUBLIC_HOLIDAYS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_PUBLIC_HOLIDAYS
    + " ("
    + COL_PUBLIC_HOLIDAYS_NAME
    + " TEXT NOT NULL, "
    + COL_PUBLIC_HOLIDAYS_DATE
    + " TEXT NOT NULL, "
    + COL_PUBLIC_HOLIDAYS_RECURRENCE
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_SETTINGS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_SETTINGS
    + " ("
    + COL_SETTINGS_NAME
    + " TEXT NOT NULL, "
    + COL_SETTINGS_VALUE
    + " TEXT NOT NULL);";

  /**
   * Creates a new SDLite helper.
   * @param context The Android context.
   * @param name The data base name.
   * @param factory The database factory.
   * @param version The database version.
   */
  SqlHelper(final Context context, final String name,
                   final SQLiteDatabase.CursorFactory factory, final int version) {
    super(context, name, factory, version);
  }


  /**
   * Called when the database must be created.
   * @param db The db context.
   */
  @Override
  public void onCreate(final SQLiteDatabase db) {
    db.execSQL(CREATE_BDD_PUBLIC_HOLIDAYS);
    db.execSQL(CREATE_BDD_DAYS);
    db.execSQL(CREATE_BDD_PROFILES);
    db.execSQL(CREATE_BDD_SETTINGS);
  }

  /**
   * Called when the database is opened.
   * @param db The db context.
   */
  @Override
  public void onOpen(final SQLiteDatabase db) {
    onCreate(db);
  }

  /**
   * Called when the database need an upgrade.
   * @param db The db context.
   * @param oldVersion The old db version.
   * @param newVersion The new db version.
   */
  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
                        final int newVersion) {
    if(oldVersion < 5) {
      Log.e(getClass().getSimpleName(), "Version not supported");
      Process.killProcess(0);
    }
    else if(oldVersion == 5 && newVersion == 6) {
      db.execSQL("ALTER TABLE " + TABLE_PROFILES + " RENAME TO " + TABLE_PROFILES + "_v" + oldVersion);
      db.execSQL("ALTER TABLE " + TABLE_DAYS + " RENAME TO " + TABLE_DAYS + "_v" + oldVersion);
      db.execSQL(CREATE_BDD_PROFILES);
      db.execSQL(CREATE_BDD_DAYS);
    }
  }

  /**
   * Loads the database.
   * @param c The Android contexte.
   * @param name The database name.
   * @param folder The output folder.
   * @throws Exception If an exception is reached.
   */
  public static String copyDatabase(final Context c, final String name,
                                    final String folder) throws Exception {
    MainApplication.getApp(c).getSql().settingsSave();
    final String databasePath = c.getDatabasePath(name).getPath();
    final File f = new File(databasePath);
    OutputStream myOutput = null;
    InputStream myInput = null;
    Log.d(SqlHelper.class.getSimpleName(), " db path " + databasePath + ", exist " + f.exists());
    Exception exception = null;
    String output = null;
    if (f.exists()) {
      try {

        final File directory = new File(folder);
        if (!directory.exists())
          //noinspection ResultOfMethodCallIgnored
          directory.mkdir();
        File out = new File(directory, new SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(new Date()) + "_" + name);
        myOutput = new FileOutputStream(out);
        myInput = new FileInputStream(databasePath);

        final byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
          myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        output = out.getAbsolutePath();
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
    return output;
  }

  /**
   * Loads the database.
   * @param c The Android contexte.
   * @param name The database name.
   * @param in The input file.
   * @throws Exception If an exception is reached.
   */
  public static void loadDatabase(Context c, final String name, File in) throws Exception{
    MainApplication.getApp(c).getSql().settingsLoad(null);
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
