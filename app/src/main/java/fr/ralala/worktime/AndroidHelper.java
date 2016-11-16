package fr.ralala.worktime;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Helper functions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class AndroidHelper {

  public static void showConfirmDialog(final Context c, final String title,
                                       String message, final android.view.View.OnClickListener yes,
                                       final android.view.View.OnClickListener no) {
    new AlertDialog.Builder(c)
      .setTitle(title)
      .setMessage(message)
      .setIcon(android.R.drawable.ic_dialog_alert)
      .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          if(yes != null) yes.onClick(null);
        }})
      .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          if(no != null) no.onClick(null);
        }}).show();
  }

  public static void openTimePicker(final Context c, final WorkTimeDay current, final TextView tv) {
    initTimeTextView(current, tv);
    TimePickerDialog timePicker = new TimePickerDialog(c, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
        tv.setText(String.format(Locale.US, "%02d:%02d", selectedHour, selectedMinute));
      }
    }, current.getHours(), current.getMinutes(), true);//Yes 24 hour time
    timePicker.show();
  }

  public static void initTimeTextView(final WorkTimeDay current, final TextView tv) {
    tv.setText(current.timeString());
  }

  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final int message) {
    showAlertDialog(c, title, c.getResources().getString(message));
  }
  /* tool function used to display a message box */
  public static void showAlertDialog(final Context c, final int title, final String message) {
    AlertDialog alertDialog = new AlertDialog.Builder(c).create();
    alertDialog.setTitle(c.getResources().getString(title));
    alertDialog.setMessage(message);
    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, c.getResources().getString(R.string.ok),
      new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          dialog.dismiss();
        }
      });
    alertDialog.show();
  }


  public static void forcePopupMenuIcons(final PopupMenu popup) {
    try {
      Field[] fields = popup.getClass().getDeclaredFields();
      for (Field field : fields) {
        if ("mPopup".equals(field.getName())) {
          field.setAccessible(true);
          Object menuPopupHelper = field.get(popup);
          Class<?> classPopupHelper = Class.forName(menuPopupHelper
            .getClass().getName());
          Method setForceIcons = classPopupHelper.getMethod(
            "setForceShowIcon", boolean.class);
          setForceIcons.invoke(menuPopupHelper, true);
          break;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void toast(final Context c, final String message, final int timer) {
    /* Create a toast with the launcher icon */
    Toast toast = Toast.makeText(c, message, timer);
    TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
    if (null!=tv) {
      Drawable drawable = c.getResources().getDrawable(R.mipmap.ic_launcher);
      final Bitmap b = ((BitmapDrawable) drawable).getBitmap();
      final Bitmap bitmapResized = Bitmap.createScaledBitmap(b, 32, 32, false);
      tv.setCompoundDrawablesWithIntrinsicBounds(new BitmapDrawable(c.getResources(), bitmapResized), null, null, null);
      tv.setCompoundDrawablePadding(5);
    }
    toast.show();
  }

  public static void toast_long(final Context c, final int message) {
    toast(c, c.getResources().getString(message), Toast.LENGTH_LONG);
  }

  public static void toast_long(final Context c, final String message) {
    toast(c, message, Toast.LENGTH_LONG);
  }

  public static void toast(final Context c, final String message) {
    toast(c, message, Toast.LENGTH_SHORT);
  }

  public static void toast(final Context c, final int message) {
    toast(c, c.getResources().getString(message));
  }

  public static void openDayEntryDialog(final Activity activity, final MainApplication app, final DayEntry de, final boolean displayProfile, final DayEntryDialogSuccessListener listener) {
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
    //dialogBuilder.setTitle(R.string.work_days);
    /* prepare the inflater and set the new content */
    LayoutInflater inflater = activity.getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.content_day_dialog_box, null);
    dialogBuilder.setView(dialogView);
    /* Get the components */
    final TextView tvProfile = (TextView)dialogView.findViewById(R.id.tvProfile);
    final Spinner spProfile = (Spinner)dialogView.findViewById(R.id.spProfile);
    final Spinner spType = (Spinner)dialogView.findViewById(R.id.spType);
    final TextView tvDay = (TextView)dialogView.findViewById(R.id.tvDay);
    final ImageView ivDelete = (ImageView)dialogView.findViewById(R.id.ivDelete);
    final TextView tvStart = (TextView)dialogView.findViewById(R.id.tvStart);
    final TextView tvEnd = (TextView)dialogView.findViewById(R.id.tvEnd);
    final TextView tvPause = (TextView)dialogView.findViewById(R.id.tvPause);
    final EditText tvAmount = (EditText)dialogView.findViewById(R.id.tvAmount);
    final TextView tvName = (TextView)dialogView.findViewById(R.id.tvName);
    final EditText etName = (EditText)dialogView.findViewById(R.id.etName);

    /* add click listener for the delete function */
    View.OnClickListener delClick = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        initTimeTextView(new WorkTimeDay(), tvStart);
        initTimeTextView(new WorkTimeDay(), tvEnd);
        initTimeTextView(new WorkTimeDay(), tvPause);
        tvAmount.setText("0.00");
        spType.setSelection(DayType.AT_WORK.value());
        etName.setText("");
        tvDay.setText(de.getDay().dateString());
        if(displayProfile) spProfile.setSelection(0);
      }
    };
    ivDelete.setOnClickListener(delClick);
    /* add click listener for the time picker */
    View.OnClickListener timePickerClick = new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if(view.equals(tvStart))
          AndroidHelper.openTimePicker(activity, de.getStart(), tvStart);
        else if(view.equals(tvEnd))
          AndroidHelper.openTimePicker(activity, de.getEnd(), tvEnd);
        else if(view.equals(tvPause))
          AndroidHelper.openTimePicker(activity, de.getPause(), tvPause);
      }
    };
    tvStart.setOnClickListener(timePickerClick);
    tvEnd.setOnClickListener(timePickerClick);
    tvPause.setOnClickListener(timePickerClick);
    /* manage view for the call from the profile view */
    if(displayProfile) {
      tvDay.setText(de.getDay().dateString());
      int v = View.VISIBLE;
      ivDelete.setVisibility(v);
      tvDay.setVisibility(v);
      spProfile.setVisibility(v);
      tvProfile.setVisibility(v);
      v = View.GONE;
      tvName.setVisibility(v);
      etName.setVisibility(v);
      if(app.getProfilesFactory().list().isEmpty()) {
        spProfile.setVisibility(View.GONE);
        tvProfile.setVisibility(View.GONE);
      }
    } else {
      int v = View.GONE;
      ivDelete.setVisibility(v);
      tvDay.setVisibility(v);
      spProfile.setVisibility(v);
      tvProfile.setVisibility(v);
      v = View.VISIBLE;
      tvName.setVisibility(v);
      etName.setVisibility(v);
      etName.setText(de.getName());
    }
    /* build type spinner */
    final ArrayAdapter<String> spTypeAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
    spTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spType.setAdapter(spTypeAdapter);
    spTypeAdapter.add(DayType.getText(activity, DayType.AT_WORK));
    spTypeAdapter.add(DayType.getText(activity, DayType.HOLIDAY));
    spTypeAdapter.add(DayType.getText(activity, DayType.PUBLIC_HOLIDAY));
    spTypeAdapter.add(DayType.getText(activity, DayType.SICKNESS));
    spTypeAdapter.add(DayType.getText(activity, DayType.UNPAID));
    spType.setSelection(de.getType() == DayType.ERROR ? 0 : de.getType().value());

    if(displayProfile) {
    /* build profiles spinner */
      final ArrayAdapter<String> spProfilesAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
      spProfilesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spProfile.setAdapter(spProfilesAdapter);
      spProfilesAdapter.add("");
      for (DayEntry profile : app.getProfilesFactory().list())
        spProfilesAdapter.add(profile.getName());
      spProfile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
          String name = spProfilesAdapter.getItem(i);
          if(name != null && !name.isEmpty()) {
            DayEntry de = app.getProfilesFactory().getByName(name);
            if (de != null) {
              initTimeTextView(de.getStart(), tvStart);
              initTimeTextView(de.getEnd(), tvEnd);
              initTimeTextView(de.getPause(), tvPause);
              tvAmount.setText(String.format(Locale.US, "%.02f", de.getAmountByHour()).replaceAll(",", "."));
              spType.setSelection(de.getType() == DayType.ERROR ? 0 : de.getType().value());
              etName.setText(de.getName());
              tvDay.setText(de.getDay().dateString());
            }
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
      });
    }

    initTimeTextView(de.getStart(), tvStart);
    initTimeTextView(de.getEnd(), tvEnd);
    initTimeTextView(de.getPause(), tvPause);

    tvAmount.setTypeface(tvPause.getTypeface());
    tvAmount.setTextSize(14);
    tvAmount.setTextColor(tvPause.getTextColors());
    tvAmount.setText(String.format(Locale.US, "%.02f",
      de.getAmountByHour() != 0 ? de.getAmountByHour() : ((MainApplication)activity.getApplicationContext()).getAmountByHour()).replaceAll(",", "."));

    /* Init the common listener. */
    final DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener(){
      public void onClick(DialogInterface dialog, int whichButton) {
        /* Click on the Positive button (OK) */
        if(whichButton == DialogInterface.BUTTON_POSITIVE) {
          DayEntry newEntry = new DayEntry(de.getDay().toCalendar(), DayType.compute(activity, spType.getSelectedItem().toString()));
          String s = tvAmount.getText().toString().trim();
          newEntry.setAmountByHour(s.isEmpty() ? 0.0 : Double.parseDouble(s));
          newEntry.setName(etName.getText().toString());
          newEntry.setEnd(tvEnd.getText().toString());
          newEntry.setStart(tvStart.getText().toString());
          newEntry.setPause(tvPause.getText().toString());
          if(!displayProfile && etName.getText().toString().isEmpty()) {
            AndroidHelper.showAlertDialog(activity, R.string.error, R.string.error_no_name);
            return;
          } else if(!displayProfile && newEntry.getStart().getHours() == 0) {
            AndroidHelper.showAlertDialog(activity, R.string.error, R.string.error_invalid_start);
            return;
          } else if(!displayProfile && newEntry.getEnd().getHours() == 0) {
            AndroidHelper.showAlertDialog(activity, R.string.error, R.string.error_invalid_end);
            return;
          } else if(!displayProfile && tvStart.getText().toString().equals(tvEnd.getText().toString())) {
            AndroidHelper.showAlertDialog(activity, R.string.error, R.string.error_invalid_start_end);
            return;
          }
          if(listener != null)
            listener.dialogAddEntry(de, newEntry);
        }
        dialog.dismiss();
      }
    };
    /* attach the listeners and init the default values */
    dialogBuilder.setPositiveButton(R.string.ok, ocl);
    dialogBuilder.setNegativeButton(R.string.cancel, ocl);
    /* show the dialog */
    AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();
  }

  public interface DayEntryDialogSuccessListener {
    void dialogAddEntry(final DayEntry oldEntry, final DayEntry newEntry);
  }
}
