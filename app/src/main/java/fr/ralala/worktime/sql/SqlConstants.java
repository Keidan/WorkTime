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
  int    VERSION_BDD                   = 8;
  String DB_NAME                       = "work_time.sqlite3";
  String TABLE_PUBLIC_HOLIDAYS         = "public_holidays";
  String TABLE_DAYS                    = "days";
  String TABLE_PROFILES                = "profiles";
  String TABLE_SETTINGS                = "settings";
  String COL_PUBLIC_HOLIDAYS_NAME      = "ph_name";
  String COL_PUBLIC_HOLIDAYS_DATE_YEAR = "ph_date_year";
  String COL_PUBLIC_HOLIDAYS_DATE_MONTH= "ph_date_month";
  String COL_PUBLIC_HOLIDAYS_DATE_DAY  = "ph_date_day";
  String COL_PUBLIC_HOLIDAYS_RECURRENCE= "ph_recurrence";
  String COL_DAYS_CURRENT_YEAR         = "d_current_year";
  String COL_DAYS_CURRENT_MONTH        = "d_current_month";
  String COL_DAYS_CURRENT_DAY          = "d_current_day";
  String COL_DAYS_START_MORNING        = "d_start_morning";
  String COL_DAYS_END_MORNING          = "d_end_morning";
  String COL_DAYS_START_AFTERNOON      = "d_start_afternoon";
  String COL_DAYS_END_AFTERNOON        = "d_end_afternoon";
  String COL_DAYS_TYPE                 = "d_type";
  String COL_DAYS_AMOUNT               = "d_amount";
  String COL_DAYS_LEGAL_WORKTIME       = "d_legal_worktime";
  String COL_DAYS_ADDITIONAL_BREAK     = "d_add_break";
  String COL_DAYS_RECOVERY_TIME        = "d_rec_time";
  String COL_PROFILES_NAME             = "p_name";
  String COL_PROFILES_CURRENT_YEAR     = "p_current_year";
  String COL_PROFILES_CURRENT_MONTH    = "p_current_month";
  String COL_PROFILES_CURRENT_DAY      = "p_current_day";
  String COL_PROFILES_START_MORNING    = "p_start_morning";
  String COL_PROFILES_END_MORNING      = "p_end_morning";
  String COL_PROFILES_START_AFTERNOON  = "p_start_afternoon";
  String COL_PROFILES_END_AFTERNOON    = "p_end_afternoon";
  String COL_PROFILES_TYPE             = "p_type";
  String COL_PROFILES_AMOUNT           = "p_amount";
  String COL_PROFILES_LEARNING_WEIGHT  = "p_learning_weight";
  String COL_PROFILES_LEGAL_WORKTIME   = "p_legal_worktime";
  String COL_PROFILES_ADDITIONAL_BREAK = "p_add_break";
  String COL_PROFILES_RECOVERY_TIME    = "p_rec_time";
  String COL_SETTINGS_NAME             = "s_name";
  String COL_SETTINGS_VALUE            = "s_value";
  int    NUM_PUBLIC_HOLIDAYS_NAME      = 0;
  int    NUM_PUBLIC_HOLIDAYS_DATE_YEAR = 1;
  int    NUM_PUBLIC_HOLIDAYS_DATE_MONTH= 2;
  int    NUM_PUBLIC_HOLIDAYS_DATE_DAY  = 3;
  int    NUM_PUBLIC_HOLIDAYS_RECURRENCE= 4;
  int    NUM_DAYS_CURRENT_YEAR         = 0;
  int    NUM_DAYS_CURRENT_MONTH        = 1;
  int    NUM_DAYS_CURRENT_DAY          = 2;
  int    NUM_DAYS_START_MORNING        = 3;
  int    NUM_DAYS_END_MORNING          = 4;
  int    NUM_DAYS_START_AFTERNOON      = 5;
  int    NUM_DAYS_END_AFTERNOON        = 6;
  int    NUM_DAYS_TYPE                 = 7;
  int    NUM_DAYS_AMOUNT               = 8;
  int    NUM_DAYS_LEGAL_WORKTIME       = 9;
  int    NUM_DAYS_ADDITIONAL_BREAK     = 10;
  int    NUM_DAYS_RECOVERY_TIME        = 11;
  int    NUM_PROFILES_NAME             = 0;
  int    NUM_PROFILES_CURRENT_YEAR     = 1;
  int    NUM_PROFILES_CURRENT_MONTH    = 2;
  int    NUM_PROFILES_CURRENT_DAY      = 3;
  int    NUM_PROFILES_START_MORNING    = 4;
  int    NUM_PROFILES_END_MORNING      = 5;
  int    NUM_PROFILES_START_AFTERNOON  = 6;
  int    NUM_PROFILES_END_AFTERNOON    = 7;
  int    NUM_PROFILES_TYPE             = 8;
  int    NUM_PROFILES_AMOUNT           = 9;
  int    NUM_PROFILES_LEARNING_WEIGHT  = 10;
  int    NUM_PROFILES_LEGAL_WORKTIME   = 11;
  int    NUM_PROFILES_ADDITIONAL_BREAK = 12;
  int    NUM_PROFILES_RECOVERY_TIME    = 13;
  int    NUM_SETTINGS_NAME             = 0;
  int    NUM_SETTINGS_VALUE            = 1;
}
