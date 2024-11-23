package fr.ralala.worktime.ui.fragments.settings;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.TimePicker;

import androidx.preference.DialogPreference;

import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Create a time preference object.</a>
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class TimePreference extends DialogPreference {
  private int mLastHour = 0;
  private int mLastMinute = 0;

  public TimePreference(Context context) {
    this(context, null);
  }

  public TimePreference(Context context, AttributeSet attrs) {
    this(context, attrs, android.R.attr.preferenceStyle);
  }

  public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, defStyleAttr);
  }

  public TimePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  /**
   * Extract the hours part of the time (00:00)
   *
   * @param time The input time.
   * @return int
   */
  protected static int getHour(String time) {
    String[] split = time.split(":");
    return (Integer.parseInt(split[0]));
  }

  /**
   * Extract the minutes part of the time (00:00)
   *
   * @param time The input time.
   * @return int
   */
  protected static int getMinute(String time) {
    String[] split = time.split(":");
    return (Integer.parseInt(split[1]));
  }

  public String getTime() {
    return mLastHour + ":" + mLastMinute;
  }

  public void setTime(TimePicker tp) {
    mLastHour = tp.getHour();
    mLastMinute = tp.getMinute();

    String time = mLastHour + ":" + mLastMinute;

    if (callChangeListener(time)) {
      persistString(time);
    }
  }

  @Override
  public void setDefaultValue(Object defaultValue) {
    super.setDefaultValue(defaultValue);
    mLastHour = 0;
    mLastMinute = 0;
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getString(index);
  }

  @Override
  public int getDialogLayoutResource() {
    return R.layout.preference_dialog_time;
  }

  @Override
  protected void onSetInitialValue(Object defaultValue) {
    onSetInitialValue(false, defaultValue);
  }

  @Override
  @SuppressWarnings("deprecation")
  protected void onSetInitialValue(boolean restore, Object defaultValue) {
    String time;

    if (restore) {
      if (defaultValue == null)
        time = getPersistedString("00:00");
      else
        time = getPersistedString(defaultValue.toString());
    } else {
      time = defaultValue.toString();
    }

    mLastHour = getHour(time);
    mLastMinute = getMinute(time);
  }
}