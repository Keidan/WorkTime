package fr.ralala.worktime.ui.fragments;

import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.ui.activities.MainActivity;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the charts fragment view
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class StatisticsFragment extends Fragment {
  private MainApplication mApp = null;
  private MainActivity mActivity;
  private SparseArray<SummaryEntry> mSummaries;
  private TableLayout mTable;

  private static class SummaryEntry {
    double nbWork;
    double nbHolidays;
    double nbSickness;
    double nbUnpaid;
    double nbRecovery;
    long hWork;
    long hWorkOver;
  }

  /**
   * Called when the fragment is created.
   *
   * @param inflater           The fragment inflater.
   * @param container          The fragment container.
   * @param savedInstanceState The saved instance state.
   * @return The created view.
   */
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_statistics, container, false);
    mActivity = (MainActivity) getActivity();
    assert mActivity != null;
    mApp = (MainApplication) mActivity.getApplication();
    mTable = rootView.findViewById(R.id.table);
    return rootView;
  }

  /**
   * Called when the view is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    mActivity.runOnUiThread(() -> mActivity.progressShow(true));
    new Thread(() -> {
      redrawChart();
      mActivity.runOnUiThread(() -> {
        rebuildTable();
        mActivity.progressDismiss();
      });
    }).start();
  }

  private void rebuildTable() {
    mTable.removeAllViews();
    SortedSet<Integer> years = toSortedSet(mSummaries);
    WorkTimeDay wtd = WorkTimeDay.now();
    if (years.isEmpty()) {
      UIHelper.toast(mActivity, getString(R.string.statistics_summary_empty));
    } else {
      for (Integer year : years) {
        SummaryEntry se = mSummaries.get(year);
        /* Year */
        TableRow tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        TextView textView = new TextView(mActivity);
        textView.setTypeface(null, Typeface.BOLD);
        textView.setText(String.valueOf(year));
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* hWork */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.work_time);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setText(wtd.fromTimeMS(se.hWork).timeString());
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* hWorkOver */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.overtime);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setTextColor(getResources().getColor(R.color.color_over, mActivity.getTheme()));
        textView.setText(wtd.fromTimeMS(se.hWorkOver).timeString());
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* nbWork */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.at_work);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setText(fixDouble(se.nbWork));
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* nbHolidays */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.holidays);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setTextColor(getResources().getColor(R.color.color_holiday, mActivity.getTheme()));
        textView.setText(fixDouble(se.nbHolidays));
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* nbSickness */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.sickness);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setTextColor(getResources().getColor(R.color.color_under, mActivity.getTheme()));
        textView.setText(fixDouble(se.nbSickness));
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* nbUnpaid */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.unpaid);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setTextColor(getResources().getColor(R.color.color_pause, mActivity.getTheme()));
        textView.setText(fixDouble(se.nbUnpaid));
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
        /* nbRecovery */
        tableRow = new TableRow(mActivity);
        tableRow.setLayoutParams(new LinearLayout.LayoutParams
          (ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        tableRow.addView(new TextView(mActivity)); /* empty view */
        textView = new TextView(mActivity);
        textView.setText(R.string.recovery);
        tableRow.addView(textView);
        textView = new TextView(mActivity);
        textView.setTextColor(getResources().getColor(R.color.color_public_holiday, mActivity.getTheme()));
        textView.setText(fixDouble(se.nbRecovery));
        textView.setTypeface(null, Typeface.BOLD);
        tableRow.addView(textView);
        mTable.addView(tableRow);
      }
    }
  }

  /**
   * Called when the configuration is changed.
   *
   * @param newConfig The new configuration.
   */
  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    redrawChart();
  }

  /**
   * Converts entries keys to SortedSet
   *
   * @param entries The entries.
   * @return SortedSet<Integer>
   */
  private SortedSet<Integer> toSortedSet(SparseArray<?> entries) {
    SortedSet<Integer> keys = new TreeSet<>();
    for (int i = 0; i < entries.size(); i++)
      keys.add(entries.keyAt(i));
    return keys;
  }

  /**
   * Normalizes the value of a double avoiding the fractional part if it is equal to zero.
   *
   * @param d The double to normalize.
   * @return String
   */
  private String fixDouble(double d) {
    return (d % 1) == 0 ? String.valueOf((int) d) : String.valueOf(d);
  }

  /**
   * Redraw the charts view.
   */
  public void redrawChart() {
    if (mSummaries != null)
      mSummaries.clear();
    mSummaries = new SparseArray<>();
    List<Integer> years = mApp.getDaysFactory().getYears();
    for (int nYear = 0; nYear < years.size(); nYear++) {
      Integer year = years.get(nYear);
      processYear(year);
    }
  }

  private void processYear(int year) {
    List<Integer> months = mApp.getDaysFactory().getMonths(year);
    for (int nMonth = 0; nMonth < months.size(); nMonth++) {
      Integer month = months.get(nMonth);
      processMonth(year, month);
    }
  }

  private void processMonth(int year, int month) {
    Map<Integer, List<DayEntry>> weeks = mApp.getDaysFactory().getWeeksAndDays(year, month);
    SortedSet<Integer> keysWeeks = new TreeSet<>(weeks.keySet());
    for (Integer week : keysWeeks) {
      processWeek(year, month, weeks, week);
    }
  }

  private void processWeek(int year, int month, Map<Integer, List<DayEntry>> weeks, int week) {
    /* Includes public holiday for the days view */
    mApp.getPublicHolidaysFactory().list(-1, month).forEach(ph -> {
      if (ph.getDay().getYear() == year || (ph.getDay().getYear() != year && ph.isRecurrence())) {
        DayEntry dePh = ph.toDayEntry();
        dePh.getDay().setYear(year);
        int w = dePh.getDay().toCalendar().get(Calendar.WEEK_OF_YEAR);
        if (w != week)
          return;
        updateSummary(year, dePh);
      }
    });
    List<DayEntry> days = weeks.get(week);
    if (days != null)
      for (int nDay = 0; nDay < days.size(); nDay++) {
        DayEntry de = days.get(nDay);
        updateSummary(year, de);
      }
  }

  private void updateSummary(Integer key, DayEntry de) {
    if (AndroidHelper.notContainsKey(mSummaries, key)) {
      mSummaries.put(key, new SummaryEntry());
    }
    SummaryEntry se = mSummaries.get(key);
    processType(se, de.getTypeMorning());
    processType(se, de.getTypeAfternoon());
    se.hWork += de.getWorkTime().getTimeMs();
    if(de.getTypeMorning() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.AT_WORK)
      se.hWorkOver += de.getOverTime().getTimeMs();
  }

  private void processType(SummaryEntry se, DayType dt) {
    switch (dt) {
      case AT_WORK:
        se.nbWork += 0.5;
        break;
      case HOLIDAY:
        se.nbHolidays += 0.5;
        break;
      case SICKNESS:
        se.nbSickness += 0.5;
        break;
      case UNPAID:
        se.nbUnpaid += 0.5;
        break;
      case RECOVERY:
        se.nbRecovery += 0.5;
        break;
      default:
        break;
    }
  }
}
