package fr.ralala.worktime.ui.dialogs;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

import fr.ralala.worktime.R;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Create a time preference object. see: http://stackoverflow.com/questions/5533078/timepicker-in-preferencescreen
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class TimePreferenceDialog extends DialogPreference {
  private int mLastHour=0;
  private int mLastMinute=0;
  private TimePicker mPicker=null;

  /**
   * Extract the hours part of the time (00:00)
   * @param time The input time.
   * @return int
   */
  private static int getHour(String time) {
    String[] split=time.split(":");
    return(Integer.parseInt(split[0]));
  }

  /**
   * Extract the minutes part of the time (00:00)
   * @param time The input time.
   * @return int
   */
  private static int getMinute(String time) {
    String[] split=time.split(":");
    return(Integer.parseInt(split[1]));
  }

  /**
   * Creates the time dialog used by the preferences
   * @param ctxt The Android context.
   * @param attrs The AttributeSet
   */
  public TimePreferenceDialog(Context ctxt, AttributeSet attrs) {
    super(ctxt, attrs);
    setPositiveButtonText(ctxt.getString(R.string.ok));
    setNegativeButtonText(ctxt.getString(R.string.cancel));
  }

  /**
   * Called to create the dialog view.
   * @return View
   */
  @Override
  protected View onCreateDialogView() {
    mPicker = new TimePicker(getContext());
    mPicker.setIs24HourView(true);
    return mPicker;
  }

  /**
   * Called to bind the dialog view.
   * @param v The view.
   */
  @Override
  protected void onBindDialogView(View v) {
    super.onBindDialogView(v);
    mPicker.setHour(mLastHour);
    mPicker.setMinute(mLastMinute);
  }

  /**
   * Called when the dialog is closed.
   * @param positiveResult True if positive button is clicked.
   */
  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      mLastHour=mPicker.getHour();
      mLastMinute=mPicker.getMinute();

      String time=String.valueOf(mLastHour)+":"+String.valueOf(mLastMinute);

      if (callChangeListener(time)) {
        persistString(time);
      }
    }
  }

  /**
   * Returns the default value.
   * @param a TypedArray
   * @param index index in TypedArray
   * @return Object
   */
  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getString(index);
  }

  /**
   * Sets the initial value.
   * @param restoreValue True if restore is required.
   * @param defaultValue The value to restore.
   */
  @Override
  protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
    String time;

    if (restoreValue) {
      if (defaultValue==null)
        time=getPersistedString("00:00");
      else
        time=getPersistedString(defaultValue.toString());
    }
    else {
      time=defaultValue.toString();
    }

    mLastHour=getHour(time);
    mLastMinute=getMinute(time);
  }
}