package fr.ralala.worktime.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
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


  public MonthDetailsDialog(final Activity activity, final MainApplication app) {
    this.activity = activity;
    this.app = app;
  }

  public void reloadDetails(int month, int year, int wMin, int wMax) {
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
    for(int w = wMin; w <= wMax; ++w, ++row) {
      WorkTimeDay wtdWorkTimeFromWeek =  app.getDaysFactory().getWorkTimeDayFromWeek(w);
      WorkTimeDay wtdEstimatedHours = app.getEstimatedHours(app.getDaysFactory().getWorkDayFromWeek(w, true));
      WorkTimeDay wtdOver  = wtdWorkTimeFromWeek.clone().delTime(wtdEstimatedHours);
      double wage = app.getDaysFactory().getWageFromWeek(w);
      totalWage += wage;
      addRow(r, gl, row, w,
        wtdWorkTimeFromWeek.timeString(),
        wtdOver.timeString(), wage, currency);
      wtdTotalWorkTime.addTime(wtdWorkTimeFromWeek.clone());
      wtdTotalOverHours.addTime(wtdOver.clone());
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
          tvWeek.setText(r.getString(R.string.week_letter) + " " + String.format(Locale.US, "%02d", week) + ":");
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

  public MonthDetailsDialog open() {
    alertDialog.show();
    return this;
  }

  /*public DayEntryDialog close() {
    alertDialog.dismiss();
    return this;
  }*/

  public void onClick(DialogInterface dialog, int whichButton) {
        /* Click on the Positive button (OK) */
    /*if(whichButton == DialogInterface.BUTTON_POSITIVE) {

    }*/
    dialog.dismiss();
  }

}
