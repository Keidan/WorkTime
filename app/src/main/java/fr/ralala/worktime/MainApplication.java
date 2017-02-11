package fr.ralala.worktime;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import fr.ralala.worktime.models.DaysFactory;
import fr.ralala.worktime.models.ProfilesFactory;
import fr.ralala.worktime.models.PublicHolidaysFactory;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.activities.SettingsActivity;
import fr.ralala.worktime.sql.SqlFactory;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Application context
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MainApplication extends Application {

  private final PublicHolidaysFactory publicHolidaysFactory;
  private final ProfilesFactory profilesFactory;
  private final DaysFactory daysFactory;
  private SqlFactory sql = null;

  public MainApplication() {
    publicHolidaysFactory = new PublicHolidaysFactory();
    profilesFactory = new ProfilesFactory();
    daysFactory = new DaysFactory();
  }


  public static MainApplication getApp(final Context c) {
    return (MainApplication) c.getApplicationContext();
  }

  public boolean openSql(final Activity activity) {
    boolean ret = false;
    try {
      sql = new SqlFactory(this);
      sql.open();
      publicHolidaysFactory.reload(sql);
      daysFactory.reload(sql);
      profilesFactory.reload(sql);
      ret = true;
    } catch (final Exception e) {
      //AndroidHelper.showAlertDialog(this, R.string.error, getString(R.string.error) + ": " + e.getMessage());
      AndroidHelper.snack(activity, getString(R.string.error) + ": " + e.getMessage());
    }
    return ret;
  }


  public ProfilesFactory getProfilesFactory() {
    return profilesFactory;
  }

  public DaysFactory getDaysFactory() {
    return daysFactory;
  }

  public PublicHolidaysFactory getPublicHolidaysFactory() {
    return publicHolidaysFactory;
  }

  public SqlFactory getSql() {
    return sql;
  }

  public WorkTimeDay getLegalWorkTimeByDay() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    String [] split = prefs.getString(SettingsActivity.PREFS_KEY_WORKTIME_BY_DAY, "00:00").split(":");
    return new WorkTimeDay(0, 0, 0, Integer.parseInt(split[0]), Integer.parseInt(split[1]));
  }

  public double getAmountByHour() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return Double.parseDouble(prefs.getString(SettingsActivity.PREFS_KEY_AMOUNT_BY_HOUR, "0.0").replaceAll(",", "."));
  }

  public String getCurrency() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(SettingsActivity.PREFS_KEY_CURRENCY, "€");
  }

  public String getEMail() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getString(SettingsActivity.PREFS_KEY_EMAIL, "");
  }

  public boolean isExportMailEnabled() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_EMAIL_ENABLE, true);
  }

  public boolean isHideWage() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_HIDE_WAGE, false);
  }

  public boolean isExportHideWage() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    return prefs.getBoolean(SettingsActivity.PREFS_KEY_EXPORT_HIDE_WAGE, false);
  }

  public WorkTimeDay getEstimatedHours(int wDays) {
    long time = getLegalWorkTimeByDay().toLongTime() * wDays;
    int mins = (int)(time % 60L);
    int hrs = (int)(time / 60L);
    return new WorkTimeDay(0, 0, 0, hrs, mins);
  }
}
