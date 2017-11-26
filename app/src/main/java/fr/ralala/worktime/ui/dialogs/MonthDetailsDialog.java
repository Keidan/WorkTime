package fr.ralala.worktime.ui.dialogs;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the dialog box containing the month details
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MonthDetailsDialog implements DialogInterface.OnClickListener {

  private Activity activity = null;
  private MainApplication app = null;
  private AlertDialog alertDialog = null;

  private class Item {
    List<DayEntry> wDays = new ArrayList<>();
    WorkTimeDay w = new WorkTimeDay();
    double wage = 0.0;
  }


  public MonthDetailsDialog(final Activity activity, final MainApplication app) {
    this.activity = activity;
    this.app = app;
  }

  public void reloadDetails(int month, int year) {
    final String currency = app.getCurrency();
    Resources r = activity.getResources();
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
    dialogBuilder.setTitle(AndroidHelper.getMonthString(month) + " " + year);
    GridLayout gl = new GridLayout(activity);
    gl.setPadding(
      r.getDimensionPixelOffset(R.dimen.activity_horizontal_margin),
      r.getDimensionPixelOffset(R.dimen.activity_vertical_margin),
      r.getDimensionPixelOffset(R.dimen.activity_horizontal_margin),
      r.getDimensionPixelOffset(R.dimen.activity_vertical_margin));

    GridLayout.LayoutParams param =new GridLayout.LayoutParams();
    param.height = GridLayout.LayoutParams.MATCH_PARENT;
    param.width = GridLayout.LayoutParams.MATCH_PARENT;
    gl.setLayoutParams(param);
    dialogBuilder.setView(gl);
    WorkTimeDay wtdTotalWorkTime = new WorkTimeDay();
    WorkTimeDay wtdTotalOverHours = new WorkTimeDay();
    /* Get the components */
    int row = 0;
    double totalWage = 0.0f;
    Calendar ctime = Calendar.getInstance();
    ctime.setTimeZone(TimeZone.getTimeZone("GMT"));
    ctime.setFirstDayOfWeek(Calendar.MONDAY);
    ctime.set(Calendar.YEAR, year);
    ctime.set(Calendar.MONTH, month);
    ctime.set(Calendar.DAY_OF_MONTH, 1);
    Map<String, DayEntry> map = app.getDaysFactory().toDaysMap();
    int maxDay = ctime.getMaximum(Calendar.DATE);
    @SuppressLint("UseSparseArrays") Map<Integer, Item> weeks = new HashMap<>();
    for(int day = 1; day <= maxDay; ++day) {
      ctime.set(Calendar.DAY_OF_MONTH, day);
      DayEntry de = map.get(String.format(Locale.US, "%02d/%02d/%04d", ctime.get(Calendar.DAY_OF_MONTH), ctime.get(Calendar.MONTH) + 1, ctime.get(Calendar.YEAR)));
      if(de != null && (de.getTypeMorning() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.AT_WORK)) {
        Item i;
        if (!weeks.containsKey(ctime.get(Calendar.WEEK_OF_YEAR)))
          weeks.put(ctime.get(Calendar.WEEK_OF_YEAR), new Item());
        i = weeks.get(ctime.get(Calendar.WEEK_OF_YEAR));

        i.w.addTime(de.getWorkTime());
        i.wDays.add(de);
        i.wage += de.getWorkTimePay();
      }
    }
    List<Integer> keys = new ArrayList<>(weeks.keySet());
    Collections.sort(keys);
    for(int idx = 0; idx < keys.size();++idx, ++row) {
      Integer w = keys.get(idx);
      Item i = weeks.get(w);
      WorkTimeDay wtdEstimatedHours = app.getEstimatedHours(i.wDays);
      WorkTimeDay wtdOver = i.w.clone().delTime(wtdEstimatedHours);
      totalWage += i.wage;
      addRow(r, gl, row, w,
        i.w.timeString(),
        wtdOver.timeString(), i.wage, currency);
      wtdTotalWorkTime.addTime(i.w);
      wtdTotalOverHours.addTime(wtdOver);
    }
    addRow(r, gl, row, -1, wtdTotalWorkTime.timeString(), wtdTotalOverHours.timeString(), totalWage, currency);

    /* attach the listeners and init the default values */
    dialogBuilder.setPositiveButton(R.string.ok, this);
    if(alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
    alertDialog = dialogBuilder.create();
  }

  private void addRow(Resources r, GridLayout gl, int row, int week, String wt, String ot, double wage, String currency) {
    GridLayout.LayoutParams param;
    for(int j = 0; j < 6; ++j) {
      TextView tvWeek = new TextView(activity);
      param =new GridLayout.LayoutParams();
      param.height = GridLayout.LayoutParams.WRAP_CONTENT;
      param.width = GridLayout.LayoutParams.WRAP_CONTENT;
      param.rightMargin = 20;
      param.topMargin = 5;
      param.setGravity(Gravity.START);
      param.columnSpec = GridLayout.spec(j);
      param.rowSpec = GridLayout.spec(row);
      param.setGravity(Gravity.FILL);
      tvWeek.setLayoutParams(param);
      if(j == 0 || week == -1) tvWeek.setTypeface(tvWeek.getTypeface(), Typeface.BOLD);
      if(j != 0)
        tvWeek.setGravity(Gravity.END);
      if(j == 0) {
        if(week == -1) {
          String s = r.getString(R.string.total) + ":";
          tvWeek.setText(s);
        } else
          tvWeek.setText((r.getString(R.string.week_letter) + " " + String.format(Locale.US, "%02d", week) + ":"));
      } else if (j == 1)
          tvWeek.setText(wt);
      else if (j == 2)
          tvWeek.setText("(");
      else if (j == 3)
          tvWeek.setText(ot);
      else if (j == 4) {
          tvWeek.setText(") ");
          tvWeek.setGravity(Gravity.START);
      } else if (!app.isHideWage() && j == 5) {
        tvWeek.setText(String.format(Locale.US, "%.02f%s", wage, currency));
      }
      gl.addView(tvWeek);
    }
  }

  public void open() {
    alertDialog.show();
  }

  public void onClick(DialogInterface dialog, int whichButton) {
    dialog.dismiss();
  }

}
