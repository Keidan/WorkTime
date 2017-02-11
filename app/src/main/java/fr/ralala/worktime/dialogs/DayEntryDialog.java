package fr.ralala.worktime.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the dialog box containing the day entries used for the profiles and the classic insertions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DayEntryDialog implements View.OnClickListener, AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener {

  private Activity activity = null;
  private MainApplication app = null;

  private Spinner spProfile = null;
  private Spinner spType = null;
  private TextView tvDay = null;
  private ImageView ivDelete = null;
  private TextView tvStart = null;
  private TextView tvEnd = null;
  private TextView tvPause = null;
  private EditText etAmount = null;
  private EditText etName = null;
  private DayEntry de = null;
  private boolean displayProfile = false;
  private DayEntryDialogSuccessListener listener = null;
  private ArrayAdapter<String> spProfilesAdapter = null;
  private AlertDialog alertDialog = null;

  private WorkTimeDay wtdStart = null;
  private WorkTimeDay wtdEnd = null;
  private WorkTimeDay wtdPause = null;


  public interface DayEntryDialogSuccessListener {
    void dialogAddEntry(final DayEntry oldEntry, final DayEntry newEntry);
  }

  public DayEntryDialog(final Activity activity, final MainApplication app, final DayEntry de, final boolean displayProfile, final DayEntryDialogSuccessListener listener) {
    this.activity = activity;
    this.app = app;
    this.de = de;
    this.displayProfile = displayProfile;
    this.listener = listener;
    refreshStartEndPause(de);
    init();
  }

  private void refreshStartEndPause(DayEntry de) {
    wtdStart = de.getStart().clone();
    wtdEnd = de.getEnd().clone();
    wtdPause = de.getPause().clone();
  }

  private void init() {
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
    //dialogBuilder.setTitle(R.string.work_days);
    /* prepare the inflater and set the new content */
    LayoutInflater inflater = activity.getLayoutInflater();
    View dialogView = inflater.inflate(R.layout.content_day_dialog_box, null);
    dialogBuilder.setView(dialogView);
    /* Get the components */
    TextView tvProfile = (TextView)dialogView.findViewById(R.id.tvProfile);
    spProfile = (Spinner)dialogView.findViewById(R.id.spProfile);
    spType = (Spinner)dialogView.findViewById(R.id.spType);
    tvDay = (TextView)dialogView.findViewById(R.id.tvDay);
    ivDelete = (ImageView)dialogView.findViewById(R.id.ivDelete);
    tvStart = (TextView)dialogView.findViewById(R.id.tvStart);
    tvEnd = (TextView)dialogView.findViewById(R.id.tvEnd);
    tvPause = (TextView)dialogView.findViewById(R.id.tvPause);
    etAmount = (EditText)dialogView.findViewById(R.id.etAmount);
    TextView tvName = (TextView)dialogView.findViewById(R.id.tvName);
    etName = (EditText)dialogView.findViewById(R.id.etName);

    boolean hw = app.isHideWage();
    etAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    dialogView.findViewById(R.id.tvLblAmount).setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    /* add click listener for the delete function */
    ivDelete.setOnClickListener(this);
    /* add click listener for the time picker */
    tvStart.setOnClickListener(this);
    tvEnd.setOnClickListener(this);
    tvPause.setOnClickListener(this);
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
      spProfilesAdapter = new ArrayAdapter<>(activity, android.R.layout.simple_spinner_item);
      spProfilesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spProfile.setAdapter(spProfilesAdapter);
      spProfilesAdapter.add("");
      for (DayEntry profile : app.getProfilesFactory().list())
        spProfilesAdapter.add(profile.getName());
      spProfile.setOnItemSelectedListener(this);
    }

    AndroidHelper.initTimeTextView(de.getStart(), tvStart);
    AndroidHelper.initTimeTextView(de.getEnd(), tvEnd);
    AndroidHelper.initTimeTextView(de.getPause(), tvPause);

    if(!app.isHideWage()) {
      etAmount.setTypeface(tvPause.getTypeface());
      etAmount.setTextSize(14);
      etAmount.setTextColor(tvPause.getTextColors());
      etAmount.setText(String.format(Locale.US, "%.02f",
        de.getAmountByHour() != 0 ? de.getAmountByHour() : ((MainApplication) activity.getApplicationContext()).getAmountByHour()).replaceAll(",", "."));
    }
    /* attach the listeners and init the default values */
    dialogBuilder.setPositiveButton(R.string.ok, this);
    dialogBuilder.setNegativeButton(R.string.cancel, this);
    alertDialog = dialogBuilder.create();
  }

  public DayEntryDialog open() {
    alertDialog.show();
    return this;
  }

  /*public DayEntryDialog close() {
    alertDialog.dismiss();
    return this;
  }*/

  public void onClick(DialogInterface dialog, int whichButton) {
        /* Click on the Positive button (OK) */
    if(whichButton == DialogInterface.BUTTON_POSITIVE) {
      DayEntry newEntry = new DayEntry(de.getDay().toCalendar(), DayType.compute(activity, spType.getSelectedItem().toString()));
      String s = etAmount.getText().toString().trim();
      if(s.equals(activity.getString(R.string.zero))) s = "";
      newEntry.setAmountByHour(s.isEmpty() ? app.getAmountByHour() : Double.parseDouble(s));
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

  public void onClick(final View v) {
    if(v.equals(ivDelete)) {
      /* add click listener for the delete function */
      AndroidHelper.initTimeTextView(wtdStart = new WorkTimeDay(), tvStart);
      AndroidHelper.initTimeTextView(wtdEnd = new WorkTimeDay(), tvEnd);
      AndroidHelper.initTimeTextView(wtdPause = new WorkTimeDay(), tvPause);
      etAmount.setText(activity.getString(R.string.zero));
      spType.setSelection(DayType.AT_WORK.value());
      etName.setText("");
      tvDay.setText(de.getDay().dateString());
      if (displayProfile) spProfile.setSelection(0);
    }
    else if(v.equals(tvStart))
      AndroidHelper.openTimePicker(activity, wtdStart, tvStart);
    else if(v.equals(tvEnd))
      AndroidHelper.openTimePicker(activity, wtdEnd, tvEnd);
    else if(v.equals(tvPause))
      AndroidHelper.openTimePicker(activity, wtdPause, tvPause);
  }

  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    if(spProfilesAdapter != null) {
      String name = spProfilesAdapter.getItem(i);
      if(name != null && !name.isEmpty()) {
        DayEntry de = app.getProfilesFactory().getByName(name);
        if (de != null) {
          AndroidHelper.initTimeTextView(de.getStart(), tvStart);
          AndroidHelper.initTimeTextView(de.getEnd(), tvEnd);
          AndroidHelper.initTimeTextView(de.getPause(), tvPause);
          etAmount.setText(String.format(Locale.US, "%.02f", de.getAmountByHour()).replaceAll(",", "."));
          spType.setSelection(de.getType() == DayType.ERROR ? 0 : de.getType().value());
          etName.setText(de.getName());
          tvDay.setText(de.getDay().dateString());
          refreshStartEndPause(de);
        }
      }
    }
  }

  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }
}
