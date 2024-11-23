package fr.ralala.worktime.ui.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the dialog box containing the month details
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MonthDetailsDialog implements DialogInterface.OnClickListener {

  private final Activity mActivity;
  private final MainApplication mApp;
  private AlertDialog mAlertDialog = null;

  private static class Item {
    List<DayEntry> wDays = new ArrayList<>();
    WorkTimeDay w = new WorkTimeDay();
    WorkTimeDay rec = new WorkTimeDay();
    double wage = 0.0;
  }


  /**
   * Creates the dialog used to display the details of a month.
   *
   * @param activity The main activity.
   * @param app      The main application.
   */
  public MonthDetailsDialog(final Activity activity, final MainApplication app) {
    mActivity = activity;
    mApp = app;
  }

  /**
   * Reloads the details.
   *
   * @param month The selected month.
   * @param year  The selected year.
   */
  public void reloadDetails(int month, int year) {
    final String currency = mApp.getCurrency();
    Resources r = mActivity.getResources();
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mActivity);
    dialogBuilder.setTitle(AndroidHelper.getMonthString(month) + " " + year);
    GridLayout gl = new GridLayout(mActivity);
    gl.setPadding(
      r.getDimensionPixelOffset(R.dimen.activity_horizontal_margin),
      r.getDimensionPixelOffset(R.dimen.activity_vertical_margin),
      r.getDimensionPixelOffset(R.dimen.activity_horizontal_margin),
      r.getDimensionPixelOffset(R.dimen.activity_vertical_margin));

    GridLayout.LayoutParams param = new GridLayout.LayoutParams();
    param.height = ViewGroup.LayoutParams.MATCH_PARENT;
    param.width = ViewGroup.LayoutParams.MATCH_PARENT;
    gl.setLayoutParams(param);
    dialogBuilder.setView(gl);
    WorkTimeDay wtdTotalWorkTime = new WorkTimeDay();
    WorkTimeDay wtdTotalOverHours = new WorkTimeDay();
    WorkTimeDay wtdTotalRecoveryHours = new WorkTimeDay();
    /* Get the components */
    int row = 0;
    double totalWage = 0.0f;
    Calendar ctime = Calendar.getInstance();
    ctime.setTimeZone(TimeZone.getTimeZone("GMT"));
    ctime.setFirstDayOfWeek(Calendar.MONDAY);
    ctime.set(Calendar.YEAR, year);
    ctime.set(Calendar.MONTH, month);
    List<DayEntry> days = mApp.getDaysFactory().list(year, month + 1, -1);
    SparseArray<Item> weeks = new SparseArray<>();
    for (DayEntry de : days) {
      ctime.set(Calendar.DAY_OF_MONTH, de.getDay().getDay());
      if (de.getTypeMorning() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.AT_WORK) {
        Item i;
        if (AndroidHelper.notContainsKey(weeks, ctime.get(Calendar.WEEK_OF_YEAR)))
          weeks.put(ctime.get(Calendar.WEEK_OF_YEAR), new Item());
        i = weeks.get(ctime.get(Calendar.WEEK_OF_YEAR));

        i.w.addTime(de.getWorkTime());
        i.rec.addTime(de.getRecoveryTime());
        i.wDays.add(de);
        i.wage += de.getWorkTimePay();
      }
    }
    List<Integer> keys = new ArrayList<>();
    for (int i = 0; i < weeks.size(); i++)
      keys.add(weeks.keyAt(i));
    keys.sort(Comparator.naturalOrder());
    for (int idx = 0; idx < keys.size(); ++idx, ++row) {
      Integer w = keys.get(idx);
      Item i = weeks.get(w);
      WorkTimeDay wtdEstimatedHours = mApp.getEstimatedHours(i.wDays);
      WorkTimeDay wtdOver = i.w.copy().delTime(wtdEstimatedHours);
      WorkTimeDay wtdRec = i.rec.copy();
      totalWage += i.wage;
      addRow(r, gl, row, w,
        i.w.timeString(),
        wtdOver.timeString(), wtdRec.timeString(), i.wage, currency);
      wtdTotalWorkTime.addTime(i.w);
      wtdTotalRecoveryHours.addTime(i.rec);
      wtdTotalOverHours.addTime(wtdOver);
    }
    addRow(r, gl, row, -1, wtdTotalWorkTime.timeString(), wtdTotalOverHours.timeString(), wtdTotalRecoveryHours.timeString(), totalWage, currency);

    /* attach the listeners and init the default values */
    dialogBuilder.setPositiveButton(R.string.ok, this);
    if (mAlertDialog != null && mAlertDialog.isShowing()) mAlertDialog.dismiss();
    mAlertDialog = dialogBuilder.create();
  }

  /**
   * Adds a new row.
   *
   * @param r        The application resource.
   * @param gl       GridLayout container.
   * @param row      The current row.
   * @param week     The current week.
   * @param wt       The work time value.
   * @param ot       The over time value.
   * @param rec      The recovery time value.
   * @param wage     The wage value.
   * @param currency The currency.
   */
  private void addRow(Resources r, GridLayout gl, int row, int week, String wt, String ot, String rec, double wage, String currency) {
    GridLayout.LayoutParams param;
    for (int j = 0; j < 9; ++j) {
      TextView tvWeek = new TextView(mActivity);
      param = new GridLayout.LayoutParams();
      param.height = ViewGroup.LayoutParams.WRAP_CONTENT;
      param.width = ViewGroup.LayoutParams.WRAP_CONTENT;
      param.rightMargin = 20;
      param.topMargin = 5;
      param.setGravity(Gravity.START);
      param.columnSpec = GridLayout.spec(j);
      param.rowSpec = GridLayout.spec(row);
      param.setGravity(Gravity.FILL);
      tvWeek.setLayoutParams(param);
      if (j == 0 || week == -1) tvWeek.setTypeface(tvWeek.getTypeface(), Typeface.BOLD);
      if (j != 0)
        tvWeek.setGravity(Gravity.END);

      switch (j) {
        case 0: {
          String s;
          if (week == -1) {
            s = r.getString(R.string.total) + ":";
          } else {
            s = r.getString(R.string.week_letter);
            s += " " + String.format(Locale.US, "%02d", week) + ":";
          }
          tvWeek.setText(s);
          break;
        }
        case 1:
          tvWeek.setText(wt);
          break;
        case 2: {
          String s = "(" + mActivity.getString(R.string.overtime_min) + ": ";
          tvWeek.setText(s);
          break;
        }
        case 3:
          tvWeek.setText(ot);
          break;
        case 4:
          tvWeek.setText(",");
          break;
        case 5: {
          String s = mActivity.getString(R.string.recovery_min) + ": ";
          tvWeek.setText(s);
          break;
        }
        case 6:
          tvWeek.setText(rec);
          break;
        case 7:
          tvWeek.setText(")");
          tvWeek.setGravity(Gravity.START);
          break;
        case 8:
          if (!mApp.isHideWage())
            tvWeek.setText(String.format(Locale.US, "%.02f%s", wage, currency));
          break;
      }
      gl.addView(tvWeek);
    }
  }

  /**
   * Opens the dialog.
   */
  public void open() {
    mAlertDialog.show();
  }

  /**
   * Called when the user click on the OK button.
   *
   * @param dialog      Not used.
   * @param whichButton Not used.
   */
  public void onClick(DialogInterface dialog, int whichButton) {
    dialog.dismiss();
  }

}
