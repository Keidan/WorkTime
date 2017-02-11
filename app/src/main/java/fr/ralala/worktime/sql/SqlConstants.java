package fr.ralala.worktime.sql;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * SQL helper
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public interface SqlConstants {
  int    VERSION_BDD                = 1;
  String DB_NAME                    = "work_time.sqlite3";
  String TABLE_PUBLIC_HOLIDAYS      = "public_holidays";
  String TABLE_DAYS                 = "days";
  String TABLE_PROFILES             = "profiles";
  String COL_PUBLIC_HOLIDAYS_NAME   = "ph_name";
  String COL_PUBLIC_HOLIDAYS_DATE   = "ph_date";
  String COL_DAYS_CURRENT           = "d_current";
  String COL_DAYS_START             = "d_start";
  String COL_DAYS_END               = "d_end";
  String COL_DAYS_PAUSE             = "d_pause";
  String COL_DAYS_TYPE              = "d_type";
  String COL_DAYS_AMOUNT            = "d_amount";
  String COL_PROFILES_NAME          = "p_name";
  String COL_PROFILES_CURRENT       = "p_current";
  String COL_PROFILES_START         = "p_start";
  String COL_PROFILES_END           = "p_end";
  String COL_PROFILES_PAUSE         = "p_pause";
  String COL_PROFILES_TYPE          = "p_type";
  String COL_PROFILES_AMOUNT        = "p_amount";
  int    NUM_PUBLIC_HOLIDAYS_NAME   = 0;
  int    NUM_PUBLIC_HOLIDAYS_DATE   = 1;
  int    NUM_DAYS_CURRENT           = 0;
  int    NUM_DAYS_START             = 1;
  int    NUM_DAYS_END               = 2;
  int    NUM_DAYS_PAUSE             = 3;
  int    NUM_DAYS_TYPE              = 4;
  int    NUM_DAYS_AMOUNT            = 5;
  int    NUM_PROFILES_NAME          = 0;
  int    NUM_PROFILES_CURRENT       = 1;
  int    NUM_PROFILES_START         = 2;
  int    NUM_PROFILES_END           = 3;
  int    NUM_PROFILES_PAUSE         = 4;
  int    NUM_PROFILES_TYPE          = 5;
  int    NUM_PROFILES_AMOUNT        = 6;
}
