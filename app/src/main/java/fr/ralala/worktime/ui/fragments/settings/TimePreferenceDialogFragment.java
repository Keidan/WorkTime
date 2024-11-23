package fr.ralala.worktime.ui.fragments.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.preference.DialogPreference;
import androidx.preference.PreferenceDialogFragmentCompat;

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
public class TimePreferenceDialogFragment extends PreferenceDialogFragmentCompat {

  private TimePicker mTimePicker;

  public static TimePreferenceDialogFragment newInstance(String key) {
    final TimePreferenceDialogFragment
      fragment = new TimePreferenceDialogFragment();
    final Bundle b = new Bundle(1);
    b.putString(ARG_KEY, key);
    fragment.setArguments(b);

    return fragment;
  }

  @Override
  protected void onBindDialogView(@NonNull View view) {
    super.onBindDialogView(view);

    mTimePicker = view.findViewById(R.id.time_picker);

    String time = null;
    DialogPreference preference = getPreference();
    if (preference instanceof TimePreference) {
      time = ((TimePreference) preference).getTime();
    }

    if (time != null) {
      int hours = TimePreference.getHour(time);
      int minutes = TimePreference.getMinute(time);
      boolean is24hour = DateFormat.is24HourFormat(getContext());

      mTimePicker.setIs24HourView(is24hour);
      mTimePicker.setHour(hours);
      mTimePicker.setMinute(minutes);
    }

    view.findViewById(R.id.btCancel).setOnClickListener(v -> {
      Dialog dialog = getDialog();
      if (dialog != null) {
        onClick(dialog, DialogInterface.BUTTON_NEGATIVE);
        dialog.dismiss();
      }
    });

    view.findViewById(R.id.btOk).setOnClickListener(v -> {
      Dialog dialog = getDialog();
      if (dialog != null) {
        onClick(dialog, DialogInterface.BUTTON_POSITIVE);
        dialog.dismiss();
      }
    });
  }

  @Override
  public void onDialogClosed(boolean positiveResult) {
    if (positiveResult) {
      DialogPreference preference = getPreference();
      if (preference instanceof TimePreference) {
        TimePreference timePreference = (TimePreference) preference;
        timePreference.setTime(mTimePicker);
      }
    }
  }
}
