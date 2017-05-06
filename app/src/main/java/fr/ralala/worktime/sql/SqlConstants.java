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
  int    VERSION_BDD                   = 5;
  int VERSION_MIN_MORNING_AFTERNOON    = 2;
  int VERSION_MIN_RECURRENCE_LEGAL_WT  = 4;
  int VERSION_MIN_SETTINGS             = 5;
  String DB_NAME                       = "work_time.sqlite3";
  String TABLE_PUBLIC_HOLIDAYS         = "public_holidays";
  String TABLE_DAYS                    = "days";
  String TABLE_PROFILES                = "profiles";
  String TABLE_SETTINGS                = "settings";
  String COL_PUBLIC_HOLIDAYS_NAME      = "ph_name";
  String COL_PUBLIC_HOLIDAYS_DATE      = "ph_date";
  String COL_PUBLIC_HOLIDAYS_RECURRENCE= "ph_recurrence";
  String COL_DAYS_CURRENT              = "d_current";
  String COL_DAYS_START_MORNING        = "d_start_morning";
  String COL_DAYS_END_MORNING          = "d_end_morning";
  String COL_DAYS_START_AFTERNOON      = "d_start_afternoon";
  String COL_DAYS_END_AFTERNOON        = "d_end_afternoon";
  String COL_DAYS_TYPE                 = "d_type";
  String COL_DAYS_AMOUNT               = "d_amount";
  String COL_DAYS_LEGAL_WORKTIME       = "d_legal_worktime";
  String COL_PROFILES_NAME             = "p_name";
  String COL_PROFILES_CURRENT          = "p_current";
  String COL_PROFILES_START_MORNING    = "p_start_morning";
  String COL_PROFILES_END_MORNING      = "p_end_morning";
  String COL_PROFILES_START_AFTERNOON  = "p_start_afternoon";
  String COL_PROFILES_END_AFTERNOON    = "p_end_afternoon";
  String COL_PROFILES_TYPE             = "p_type";
  String COL_PROFILES_AMOUNT           = "p_amount";
  String COL_PROFILES_LEARNING_WEIGHT  = "p_learning_weight";
  String COL_PROFILES_LEGAL_WORKTIME   = "p_legal_worktime";
  String COL_SETTINGS_NAME             = "s_name";
  String COL_SETTINGS_VALUE            = "s_value";
  int    NUM_PUBLIC_HOLIDAYS_NAME      = 0;
  int    NUM_PUBLIC_HOLIDAYS_DATE      = 1;
  int    NUM_PUBLIC_HOLIDAYS_RECURRENCE= 2;
  int    NUM_DAYS_CURRENT              = 0;
  int    NUM_DAYS_START_MORNING        = 1;
  int    NUM_DAYS_END_MORNING          = 2;
  int    NUM_DAYS_START_AFTERNOON      = 3;
  int    NUM_DAYS_END_AFTERNOON        = 4;
  int    NUM_DAYS_TYPE                 = 5;
  int    NUM_DAYS_AMOUNT               = 6;
  int    NUM_DAYS_LEGAL_WORKTIME       = 7;
  int    NUM_PROFILES_NAME             = 0;
  int    NUM_PROFILES_CURRENT          = 1;
  int    NUM_PROFILES_START_MORNING    = 2;
  int    NUM_PROFILES_END_MORNING      = 3;
  int    NUM_PROFILES_START_AFTERNOON  = 4;
  int    NUM_PROFILES_END_AFTERNOON    = 5;
  int    NUM_PROFILES_TYPE             = 6;
  int    NUM_PROFILES_AMOUNT           = 7;
  int    NUM_PROFILES_LEARNING_WEIGHT  = 8;
  int    NUM_PROFILES_LEGAL_WORKTIME   = 9;
  int    NUM_SETTINGS_NAME             = 0;
  int    NUM_SETTINGS_VALUE            = 1;
}
