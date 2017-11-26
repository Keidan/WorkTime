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
  private int lastHour=0;
  private int lastMinute=0;
  private TimePicker picker=null;

  private static int getHour(String time) {
    String[] split=time.split(":");
    return(Integer.parseInt(split[0]));
  }

  private static int getMinute(String time) {
    String[] split=time.split(":");
    return(Integer.parseInt(split[1]));
  }

  public TimePreferenceDialog(Context ctxt, AttributeSet attrs) {
    super(ctxt, attrs);
    setPositiveButtonText(ctxt.getString(R.string.ok));
    setNegativeButtonText(ctxt.getString(R.string.cancel));
  }

  @Override
  protected View onCreateDialogView() {
    picker = new TimePicker(getContext());
    picker.setIs24HourView(true);
    return picker;
  }

  @Override
  protected void onBindDialogView(View v) {
    super.onBindDialogView(v);
    picker.setHour(lastHour);
    picker.setMinute(lastMinute);
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      lastHour=picker.getHour();
      lastMinute=picker.getMinute();

      String time=String.valueOf(lastHour)+":"+String.valueOf(lastMinute);

      if (callChangeListener(time)) {
        persistString(time);
      }
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getString(index);
  }

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

    lastHour=getHour(time);
    lastMinute=getMinute(time);
  }
}