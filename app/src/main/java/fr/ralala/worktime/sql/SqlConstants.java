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
  int    VERSION_BDD                   = 2;
  String DB_NAME                       = "work_time.sqlite3";
  String TABLE_PUBLIC_HOLIDAYS         = "public_holidays";
  String TABLE_DAYS                    = "days";
  String TABLE_PROFILES                = "profiles";
  String COL_PUBLIC_HOLIDAYS_NAME      = "ph_name";
  String COL_PUBLIC_HOLIDAYS_DATE      = "ph_date";
  String COL_DAYS_CURRENT              = "d_current";
  String COL_DAYS_START_MORNING        = "d_start_morning";
  String COL_DAYS_END_MORNING          = "d_end_morning";
  String COL_DAYS_START_AFTERNOON      = "d_start_afternoon";
  String COL_DAYS_END_AFTERNOON        = "d_end_afternoon";
  String COL_DAYS_TYPE                 = "d_type";
  String COL_DAYS_AMOUNT               = "d_amount";
  String COL_PROFILES_NAME             = "p_name";
  String COL_PROFILES_CURRENT          = "p_current";
  String COL_PROFILES_START_MORNING    = "p_start_morning";
  String COL_PROFILES_END_MORNING      = "p_end_morning";
  String COL_PROFILES_START_AFTERNOON  = "p_start_afternoon";
  String COL_PROFILES_END_AFTERNOON    = "p_end_afternoon";
  String COL_PROFILES_TYPE             = "p_type";
  String COL_PROFILES_AMOUNT           = "p_amount";
  int    NUM_PUBLIC_HOLIDAYS_NAME      = 0;
  int    NUM_PUBLIC_HOLIDAYS_DATE      = 1;
  int    NUM_DAYS_CURRENT              = 0;
  int    NUM_DAYS_START_MORNING        = 1;
  int    NUM_DAYS_END_MORNING          = 2;
  int    NUM_DAYS_START_AFTERNOON      = 3;
  int    NUM_DAYS_END_AFTERNOON        = 4;
  int    NUM_DAYS_TYPE                 = 5;
  int    NUM_DAYS_AMOUNT               = 6;
  int    NUM_PROFILES_NAME             = 0;
  int    NUM_PROFILES_CURRENT          = 1;
  int    NUM_PROFILES_START_MORNING    = 2;
  int    NUM_PROFILES_END_MORNING      = 3;
  int    NUM_PROFILES_START_AFTERNOON  = 4;
  int    NUM_PROFILES_END_AFTERNOON    = 5;
  int    NUM_PROFILES_TYPE             = 6;
  int    NUM_PROFILES_AMOUNT           = 7;
}
