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
import java.security.DigestInputStream;
import java.security.MessageDigest;
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
    private boolean mUpdated = false;
    private boolean mUnsupported = false;

  private static final String CREATE_BDD_PROFILES = "CREATE TABLE IF NOT EXISTS "
    + TABLE_PROFILES
    + " ("
    + COL_PROFILES_NAME
    + " TEXT NOT NULL, "
    + COL_PROFILES_CURRENT_YEAR
    + " TEXT NOT NULL, "
    + COL_PROFILES_CURRENT_MONTH
    + " TEXT NOT NULL, "
    + COL_PROFILES_CURRENT_DAY
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
    + " TEXT NOT NULL, "
    + COL_PROFILES_RECOVERY_TIME
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_DAYS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_DAYS
    + " ("
    + COL_DAYS_CURRENT_YEAR
    + " TEXT NOT NULL, "
    + COL_DAYS_CURRENT_MONTH
    + " TEXT NOT NULL, "
    + COL_DAYS_CURRENT_DAY
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
    + " TEXT NOT NULL, "
    + COL_DAYS_RECOVERY_TIME
    + " TEXT NOT NULL);";

  private static final String CREATE_BDD_PUBLIC_HOLIDAYS = "CREATE TABLE IF NOT EXISTS "
    + TABLE_PUBLIC_HOLIDAYS
    + " ("
    + COL_PUBLIC_HOLIDAYS_NAME
    + " TEXT NOT NULL, "
    + COL_PUBLIC_HOLIDAYS_DATE_YEAR
    + " TEXT NOT NULL, "
    + COL_PUBLIC_HOLIDAYS_DATE_MONTH
    + " TEXT NOT NULL, "
    + COL_PUBLIC_HOLIDAYS_DATE_DAY
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
      mUnsupported = true;
      Log.e(getClass().getSimpleName(), "Version not supported");
      Process.killProcess(0);
    }
    else if(oldVersion == 5 && newVersion == 6) {
      mUpdated = true;
      /* profiles */
      db.execSQL("ALTER TABLE " + TABLE_PROFILES+ " ADD COLUMN " + COL_PROFILES_ADDITIONAL_BREAK + " TEXT DEFAULT '00:00' NOT NULL");
      /* days */
      db.execSQL("ALTER TABLE " + TABLE_DAYS+ " ADD COLUMN " + COL_DAYS_ADDITIONAL_BREAK + " TEXT DEFAULT '00:00' NOT NULL");
    }
    else if(oldVersion == 6 && newVersion == 7) {
      mUpdated = true;
      /* profiles */
      db.execSQL("ALTER TABLE " + TABLE_PROFILES+ " ADD COLUMN " + COL_PROFILES_RECOVERY_TIME + " TEXT DEFAULT '00:00' NOT NULL");
      /* days */
      db.execSQL("ALTER TABLE " + TABLE_DAYS+ " ADD COLUMN " + COL_DAYS_RECOVERY_TIME + " TEXT DEFAULT '00:00' NOT NULL");
    } else if(oldVersion == 7 && newVersion == 8) {
      Log.i(getClass().getSimpleName(), "Ready to migrate from v7 to v8");
      /* The date columns of the previous databases (d_current, p_current and ph_date) are divided into 3 dedicated columns ([prev_column_name]_year, [prev_column_name]_month, [prev_column_name]_day) */
      String oldTable = TABLE_DAYS + "_v" + oldVersion;
      db.execSQL("ALTER TABLE days RENAME TO " + oldTable);
      db.execSQL(CREATE_BDD_DAYS);
      db.execSQL("INSERT INTO " + TABLE_DAYS + "(" + COL_DAYS_CURRENT_YEAR + ", " +  COL_DAYS_CURRENT_MONTH + ", " +  COL_DAYS_CURRENT_DAY + ", " +  COL_DAYS_START_MORNING + ", " +  COL_DAYS_END_MORNING
              + ", " + COL_DAYS_START_AFTERNOON + ", " + COL_DAYS_END_AFTERNOON + ", " + COL_DAYS_TYPE + ", " + COL_DAYS_AMOUNT + ", " + COL_DAYS_LEGAL_WORKTIME + ", " + COL_DAYS_ADDITIONAL_BREAK + ", " +  COL_DAYS_RECOVERY_TIME
              + ") SELECT SUBSTR(d_current,7,4),SUBSTR(d_current,4,2), SUBSTR(d_current,1,2), d_start_morning, d_end_morning, d_start_afternoon, d_end_afternoon, "
              + "d_type, d_amount,  d_legal_worktime, d_add_break,  d_rec_time FROM " + oldTable);
      db.execSQL("DROP TABLE " + oldTable);
      Log.i(getClass().getSimpleName(), "Days database, migrated");

      oldTable = TABLE_PROFILES + "_v" + oldVersion;
      db.execSQL("ALTER TABLE profiles RENAME TO " + oldTable);
      db.execSQL(CREATE_BDD_PROFILES);
      db.execSQL("INSERT INTO " + TABLE_PROFILES + "(" + COL_PROFILES_NAME + ", " + COL_PROFILES_CURRENT_YEAR + ", " + COL_PROFILES_CURRENT_MONTH + ", " + COL_PROFILES_CURRENT_DAY + ", " + COL_PROFILES_START_MORNING
              + ", " + COL_PROFILES_END_MORNING + ", " + COL_PROFILES_START_AFTERNOON + ", " + COL_PROFILES_END_AFTERNOON + ", " + COL_PROFILES_TYPE + ", " + COL_PROFILES_AMOUNT + ", " + COL_PROFILES_LEARNING_WEIGHT
              + ", " + COL_PROFILES_LEGAL_WORKTIME + ", " + COL_PROFILES_ADDITIONAL_BREAK + ", " + COL_PROFILES_RECOVERY_TIME
              + ") SELECT p_name, SUBSTR(p_current,7,4),SUBSTR(p_current,4,2), SUBSTR(p_current,1,2), p_start_morning, p_end_morning, p_start_afternoon, p_end_afternoon, "
              + "p_type, p_amount, p_learning_weight,  p_legal_worktime, p_add_break,  p_rec_time FROM " + oldTable);
      db.execSQL("DROP TABLE " + oldTable);
      Log.i(getClass().getSimpleName(), "Profiles database, migrated");

      oldTable = TABLE_PUBLIC_HOLIDAYS + "_v" + oldVersion;
      db.execSQL("ALTER TABLE public_holidays RENAME TO " + oldTable);
      db.execSQL(CREATE_BDD_PUBLIC_HOLIDAYS);
      db.execSQL("INSERT INTO " + TABLE_PUBLIC_HOLIDAYS + "(" + COL_PUBLIC_HOLIDAYS_NAME + ", " + COL_PUBLIC_HOLIDAYS_DATE_YEAR + ", " + COL_PUBLIC_HOLIDAYS_DATE_MONTH
              + ", " + COL_PUBLIC_HOLIDAYS_DATE_DAY + ", " + COL_PUBLIC_HOLIDAYS_RECURRENCE
              + ") SELECT ph_name, SUBSTR(ph_date,7,4),SUBSTR(ph_date,4,2), SUBSTR(ph_date,1,2), ph_recurrence FROM " + oldTable);
      db.execSQL("DROP TABLE " + oldTable);
      Log.i(getClass().getSimpleName(), "Public holidays database, migrated");
    }
  }

  /**
   * Tests whether or not an upgrade is performed.
   * @return boolean
   */
  protected boolean isUpdated() {
    return mUpdated;
  }

  /**
   * Tests whether the upgrade is supported or not.
   * @return boolean
   */
  protected boolean isSupported() {
    return !mUnsupported;
  }

  /**
   * Returns the database MD5.
   * @param c The android context.
   * @return The MD5 or null on error.
   */
  public static String getDatabaseMD5(final Context c) {
    final String databasePath = c.getDatabasePath(DB_NAME).getPath();
    try {
      MessageDigest messageDigest = MessageDigest.getInstance("MD5");
      FileInputStream inputStream = new FileInputStream(databasePath);
      DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
      byte[] buffer = new byte[4096];
      //noinspection StatementWithEmptyBody
      while (digestInputStream.read(buffer) > -1);
      MessageDigest digest = digestInputStream.getMessageDigest();
      digestInputStream.close();
      byte[] md5 = digest.digest();
      StringBuilder sb = new StringBuilder();
      for (byte b : md5) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      Log.e(SqlHelper.class.getSimpleName(), "Exception: " + e.getMessage(), e);
    }
    return null;

  }
  /**
   * Loads the database.
   * @param c The Android context.
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
   * @param c The Android context.
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
