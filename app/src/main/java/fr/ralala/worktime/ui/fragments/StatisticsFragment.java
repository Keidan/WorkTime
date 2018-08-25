package fr.ralala.worktime.ui.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.chart.PieChart;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.SimpleSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.MainActivity;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the charts fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class StatisticsFragment extends Fragment implements View.OnClickListener {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private MainApplication mApp = null;
  private LinearLayout mChartContainer = null;
  private CurrentView mCurrentView = CurrentView.YEARS;
  private DisplayMetrics mMetrics = null;
  private int mCurrentYearIndex = 0;
  private int mCurrentMonthIndex = 0;
  private int mCurrentWeekIndex = 0;
  private int mCurrentDayIndex = 0;
  private XYMultipleSeriesRenderer mMultiRenderer = null;
  private XYMultipleSeriesDataset mDatasetBar = null;
  private GraphicalView mChartView = null;
  private View mRootView = null;
  private MainActivity mActivity;

  private enum CurrentView{
    YEARS,
    MONTHS,
    WEEKS,
    DAYS,
    DETAIL
  }

  private class ChartEntry {
    WorkTimeDay work = new WorkTimeDay();
    WorkTimeDay over = new WorkTimeDay();
    WorkTimeDay pause = new WorkTimeDay();
    WorkTimeDay morning = new WorkTimeDay();
    WorkTimeDay afternoon = new WorkTimeDay();
    String x_label;
    double dover;
    double dwork;
  }

  private class AnnotationEntry {
    int idx;
    String annotation;
    double x;
    double y;

    AnnotationEntry(int idx, String annotation, double x, double y) {
      this.idx = idx;
      this.annotation = annotation;
      this.x = x;
      this.y = y;
    }
  }

  /**
   * Called when backPressed is consumed.
   * @return boolean
   */
  public boolean consumeBackPressed() {
    if(mCurrentView == CurrentView.YEARS) {
      if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        return false;
      } else {
        UIHelper.toast(mActivity, R.string.on_double_back_back_text);
        AndroidHelper.vibrate(mActivity);
      }
      mLastBackPressed = System.currentTimeMillis();
    }
    else if(mCurrentView == CurrentView.DETAIL)
      mCurrentView = CurrentView.DAYS;
    else if(mCurrentView == CurrentView.DAYS)
      mCurrentView = CurrentView.WEEKS;
    else if(mCurrentView == CurrentView.WEEKS)
      mCurrentView = CurrentView.MONTHS;
    else if(mCurrentView == CurrentView.MONTHS)
      mCurrentView = CurrentView.YEARS;
    redrawChart();
    return true;
  }

  /**
   * Adds a day entry.
   * @param entries The entries list.
   * @param key The list key.
   * @param de The current day entry.
   * @param x_label The x label.
   */
  private void addDayEntry(SparseArray<ChartEntry> entries, Integer key, DayEntry de, String x_label) {
    if (AndroidHelper.notContainsKey(entries, key)) {
      entries.put(key, new ChartEntry());
    }
    entries.get(key).work.addTime(de.getWorkTime());
    entries.get(key).over.addTime(de.getOverTime());
    entries.get(key).x_label = x_label;
    WorkTimeDay wm = de.isValidMorningType() ? de.getEndMorning().clone() : new WorkTimeDay();
    if(!wm.timeString().equals("00:00"))
      wm.delTime(de.isValidMorningType() ? de.getStartMorning().clone() : new WorkTimeDay());
    entries.get(key).morning = wm;
    WorkTimeDay wa = de.isValidAfternoonType() ? de.getEndAfternoon().clone() : new WorkTimeDay();
    if(!wa.timeString().equals("00:00"))
      wa.delTime(de.isValidAfternoonType() ? de.getStartAfternoon().clone() : new WorkTimeDay());
    entries.get(key).afternoon = wa;
    entries.get(key).pause = de.getPause();
  }

  /**
   * Called when the fragment is created.
   * @param inflater The fragment inflater.
   * @param container The fragment container.
   * @param savedInstanceState The saved instance state.
   * @return The created view.
   */
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_statistics, container, false);
    this.mRootView = rootView;
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
    mApp = MainApplication.getInstance();
    Button cancel = rootView.findViewById(R.id.cancel);
    cancel.setOnClickListener(this);
    mChartContainer = rootView.findViewById(R.id.graph);
    mMetrics = new DisplayMetrics();
    mActivity.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
    /* uses 80% of the screen */
    mChartContainer.getLayoutParams().height = (int)getPercentOf(mMetrics.heightPixels, 85);
    redrawChart();
    return rootView;
  }


  /**
   * Called when the user click on a bar.
   * @param view The current view.
   */
  @Override
  public void onClick(View view) {
    mCurrentView = CurrentView.YEARS;
    redrawChart();
  }

  /**
   * Called when the configuration is changed.
   * @param newConfig The new configuration.
   */
  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    redrawChart();
  }

  /**
   * Redraw the charts view.
   */
  public void redrawChart() {
    String title = null;

    if(mCurrentView == CurrentView.YEARS)
      mRootView.findViewById(R.id.cancel).setVisibility(View.GONE);
    else
      mRootView.findViewById(R.id.cancel).setVisibility(View.VISIBLE);
    SparseArray<ChartEntry> entries = new SparseArray<>();

    List<Integer> years = mApp.getDaysFactory().getYears();
    for(int nYear = 0; nYear < years.size(); nYear++) {
      Integer year = years.get(nYear);
      if(mCurrentView == CurrentView.YEARS || mCurrentYearIndex == nYear) {
        if (mCurrentView != CurrentView.YEARS) title = "" + year;
        List<Integer> months = mApp.getDaysFactory().getMonths(year);
        for(int nMonth = 0; nMonth < months.size(); nMonth++) {
          Integer month = months.get(nMonth);
          if(mCurrentView == CurrentView.YEARS || mCurrentView == CurrentView.MONTHS || mCurrentMonthIndex == nMonth) {
            Map<Integer, List<DayEntry>> weeks = mApp.getDaysFactory().getWeeksAndDays(year, month);
            SortedSet<Integer> keysWeeks = new TreeSet<>(weeks.keySet());
            int nWeek = 0;
            for (Integer week : keysWeeks) {
              if(mCurrentView == CurrentView.YEARS || mCurrentView == CurrentView.MONTHS || mCurrentView == CurrentView.WEEKS || mCurrentWeekIndex == nWeek) {
                List<DayEntry> days = weeks.get(week);
                for (int nDay = 0; nDay < days.size(); nDay++) {
                  DayEntry de = days.get(nDay);
                  String s = parseDay(de, nDay, year, month, week, entries);
                  if(s != null)
                    title = s;
                }
              }
              nWeek++;
            }
          }
        }
      }
    }
    if((mCurrentView != CurrentView.DETAIL))
      drawBarChart(mMetrics.widthPixels, title, getString(R.string.statistics_hours), entries);
    else
      drawPieChart(title, entries);

  }

  /**
   * Parse a day an adds the entry to the list.
   * @param de The current DayEntry.
   * @param nDay The current day index.
   * @param year The current year.
   * @param month The current month.
   * @param week The current week.
   * @param entries The entries list.
   * @return The title (or null).
   */
  private String parseDay(DayEntry de, int nDay, int year, int month, int week, SparseArray<ChartEntry> entries) {
    String title = null;
    int day = de.getDay().getDay();
    if (mCurrentView == CurrentView.YEARS || mCurrentView == CurrentView.MONTHS || mCurrentView == CurrentView.WEEKS || mCurrentView == CurrentView.DAYS || mCurrentDayIndex == nDay) {
      if ((de.getTypeMorning() == DayType.PUBLIC_HOLIDAY && de.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) || (de.getTypeMorning() == DayType.HOLIDAY && de.getTypeAfternoon() == DayType.HOLIDAY))
        return null;
      if (!de.isValidMorningType() && !de.isValidAfternoonType())
        return null;
      Integer e = 0;
      String x_label = "";
      if (mCurrentView == CurrentView.YEARS) {
        e = year;
        x_label = "" + e;
      } else if (mCurrentView == CurrentView.MONTHS) {
        e = month;
        x_label = getResources().getStringArray(R.array.month_letter)[e - 1];
        title = String.format(Locale.US, "%d", year);
      } else if (mCurrentView == CurrentView.WEEKS) {
        e = week;
        x_label = getString(R.string.week_letter) + " " + e;
        title = String.format(Locale.US, "%d %s", year, getResources().getStringArray(R.array.month_short)[month - 1]);
      } else if (mCurrentView == CurrentView.DAYS) {
        e = de.getDay().toCalendar().get(Calendar.DAY_OF_WEEK) - 1;
        x_label = getResources().getStringArray(R.array.days_letter)[e - 1] + " " + de.getDay().getDay();
        title = String.format(Locale.US, "%d %s %s %d", year, getResources().getStringArray(R.array.month_short)[month - 1], getString(R.string.week), week);
      } else if (mCurrentView == CurrentView.DETAIL) {
        e = day;
        title = String.format(Locale.US, "%d %s %d (%s %d)", day, getResources().getStringArray(R.array.month_short)[month - 1], year, getString(R.string.week), week);
      }
      addDayEntry(entries, e, de, x_label);
    }
    return title;
  }

  /**
   * Called when the view is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    redrawChart();
  }

  /**
   * Converts the time to a double value.
   * @param w The time to convert.
   * @return double
   */
  private double toDouble(WorkTimeDay w) {
    return Double.parseDouble(w.timeString().replaceAll(":", "\\."));
  }

  /**
   * Converts a double to a time string.
   * @param d The double to convert.
   * @return String
   */
  private String fromDouble(double d) {
    DecimalFormat df = new DecimalFormat("#00.00");
    return df.format(d).replaceAll(",", ":");
  }

  /**
   * Initializes the multi render.
   * @param title Milti render title.
   * @param legendHours Legend used for the hours.
   */
  private void initMultiRender(final String title, final String legendHours) {
    int defaultColor = getResources().getColor(R.color.half_black, mActivity.getTheme());
    if(mMultiRenderer != null) {
      mMultiRenderer.clearXTextLabels();
      mMultiRenderer.clearYTextLabels();
      mMultiRenderer.removeAllRenderers();
    }
    if(mChartContainer != null)
      mChartContainer.removeAllViews();

    mMultiRenderer = new XYMultipleSeriesRenderer();
    mMultiRenderer.setXLabels(0);
    mMultiRenderer.setZoomButtonsVisible(false);
    mMultiRenderer.setPanEnabled(false, false);
    mMultiRenderer.setPanEnabled(false, false);
    mMultiRenderer.setZoomRate(0.2f);
    mMultiRenderer.setZoomEnabled(false, false);
    mMultiRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
    mMultiRenderer.setLegendTextSize(30);
    mMultiRenderer.setChartTitleTextSize(30);
    mMultiRenderer.setAxisTitleTextSize(30);
    mMultiRenderer.setLabelsTextSize(40);
    mMultiRenderer.setFitLegend(true);
    mMultiRenderer.setShowGrid(false);
    mMultiRenderer.setZoomEnabled(false);
    mMultiRenderer.setExternalZoomEnabled(false);
    mMultiRenderer.setAntialiasing(true);
    mMultiRenderer.setInScroll(false);
    mMultiRenderer.setShowLegend(false);
    mMultiRenderer.setLegendHeight(30);
    mMultiRenderer.setXLabelsAlign(Paint.Align.CENTER);
    //mMultiRenderer.setYLabelsAlign(Paint.Align.LEFT);

    mMultiRenderer.setYAxisAlign(Paint.Align.LEFT, 0);
    mMultiRenderer.setYLabelsAlign(Paint.Align.LEFT, 0);
    mMultiRenderer.setYLabelsColor(0, defaultColor);
    mMultiRenderer.setXLabelsColor(defaultColor);
    mMultiRenderer.setLabelsColor(defaultColor);
    mMultiRenderer.setGridColor(defaultColor);
    mMultiRenderer.setAxesColor(defaultColor);

    mMultiRenderer.setYLabels(10);
    //setting used to move the graph on xaxiz to .5 to the right
    mMultiRenderer.setXAxisMin(-.5);
    //setting bar size or space between two bars
    mMultiRenderer.setBarSpacing(0.5);
    //Setting background color of the graph to transparent
    mMultiRenderer.setBackgroundColor(Color.TRANSPARENT);
    mMultiRenderer.setApplyBackgroundColor(true);
    //setting the margin size for the graph in the order top, left, bottom, right
    mMultiRenderer.setMargins(new int[]{30, 30, 30, 30});

    if(mCurrentView != CurrentView.DETAIL) {
      mMultiRenderer.setChartTitle("");
      mMultiRenderer.setXTitle(title);
    } else {
      mMultiRenderer.setChartTitle(title);
      mMultiRenderer.setXTitle("");
    }
    mMultiRenderer.setChartTitleTextSize(40);
    mMultiRenderer.setYTitle(legendHours == null ? "" : legendHours);
  }

  /**
   * Draw the bars chart.
   * @param width Chart width.
   * @param title Chart title.
   * @param legendHours Legend for the hours.
   * @param values Bars value.
   */
  private void drawBarChart(final int width, final String title, final String legendHours, final @NonNull SparseArray<ChartEntry> values) {
    if(mDatasetBar != null)
      mDatasetBar.clear();
    initMultiRender(title, legendHours);

    mDatasetBar = new XYMultipleSeriesDataset();

    SortedSet<Integer> keys = toSortedSet(values);
    //setting max values to be display in x axis
    mMultiRenderer.setXAxisMax(keys.size() + 1);
    mMultiRenderer.setBarWidth((int) getPercentOf(width / mMultiRenderer.getXAxisMax(), 35));
    int i = 1;
    double max = 0.0, min = Double.MAX_VALUE;

    int defaultColor = getResources().getColor(R.color.half_black, mActivity.getTheme());
    int cunder = getResources().getColor(R.color.color_under, mActivity.getTheme());
    int cover = getResources().getColor(R.color.color_over, mActivity.getTheme());
    int cwork = getResources().getColor(R.color.color_work, mActivity.getTheme());
    int seriesIdx = 0;
    List<AnnotationEntry> annotations = new ArrayList<>();
    for (Integer key : keys) {
      ChartEntry ce = values.get(key);
      WorkTimeDay withOver = ce.work; /* time include over */
      WorkTimeDay work = withOver.clone();
      work.delTime(ce.over);
      ce.dover = toDouble(withOver);
      ce.dwork = toDouble(work);

      if (ce.dover >= ce.dwork) {
        String annotation;
        if (ce.dover == ce.dwork)
          annotation = ce.work.timeString();
        else {
          annotation = ce.work.timeString() + "\n(+" + ce.over.timeString() + ")";
        }
      /* over */
        addBarChartEntry(defaultColor, cover, key, i, ce.dover);
        seriesIdx++;
      /* work */
        addBarChartEntry(defaultColor, cwork, key, i, ce.dwork);
        annotations.add(new AnnotationEntry(seriesIdx - 1, annotation, i, ce.dover));
        seriesIdx++;
      } else {
        String annotation = ce.work.timeString() + "\n(" + ce.over.timeString() + ")";
      /* under */
        addBarChartEntry(defaultColor, cunder, key, i, ce.dwork);
        seriesIdx++;
      /* work */
        addBarChartEntry(defaultColor, cwork, key, i, ce.dover);
        annotations.add(new AnnotationEntry(seriesIdx - 1, annotation, i, ce.dwork));
        seriesIdx++;
      }

      mMultiRenderer.addXTextLabel(i, ce.x_label);
      if (ce.dover >= ce.dwork) {
        if (max < ce.dover) max = ce.dover;
        if (min > ce.dwork) min = ce.dwork;
      } else {
        if (max < ce.dwork) max = ce.dwork;
        if (min > ce.dover) min = ce.dover;
      }
      i++;
    }

    int orientation = mActivity.getResources().getConfiguration().orientation;
    int offsetPercent = orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 6;
    for (AnnotationEntry ae : annotations) {
      mDatasetBar.getSeriesAt(ae.idx).addAnnotation(ae.annotation, ae.x, ae.y + getPercentOf(max, offsetPercent));
    }

    //setting min max values to be display in y axis +- 10%
    mMultiRenderer.setYAxisMax(max + getPercentOf(max, 10));
    if (mCurrentView == CurrentView.DAYS || mCurrentView == CurrentView.WEEKS || mCurrentView == CurrentView.MONTHS)
      mMultiRenderer.setYAxisMin(0);
    else
      mMultiRenderer.setYAxisMin(min - getPercentOf(min, 10));

    BarChart bchart = new BarChart(mDatasetBar, mMultiRenderer, BarChart.Type.STACKED);
    mChartView = new GraphicalView(mActivity, bchart);

    mMultiRenderer.setSelectableBuffer(100);
    mChartView.setOnClickListener((v) -> {
      // handle the click event on the chart
      SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
      if (seriesSelection == null) {
        Log.e(getClass().getSimpleName(), "No chart element");
      } else {
        int x = ((int) seriesSelection.getXValue()) - 1;
        if (mCurrentView == CurrentView.YEARS) {
          mCurrentView = CurrentView.MONTHS;
          mCurrentYearIndex = x;
        } else if (mCurrentView == CurrentView.MONTHS) {
          mCurrentView = CurrentView.WEEKS;
          mCurrentMonthIndex = x;
        } else if (mCurrentView == CurrentView.WEEKS) {
          mCurrentView = CurrentView.DAYS;
          mCurrentWeekIndex = x;
        } else if (mCurrentView == CurrentView.DAYS) {
          mCurrentView = CurrentView.DETAIL;
          mCurrentDayIndex = x;
        } else return;
        redrawChart();
      }
    });

    mChartContainer.addView(mChartView);
  }

  /**
   * Draw a pie chart.
   * @param title Chart title.
   * @param values Pie values.
   */
  private void drawPieChart(final String title, final @NonNull SparseArray<ChartEntry> values) {
    initMultiRender(title, null);
    final CategorySeries dataset = new CategorySeries("title");
    SortedSet<Integer> keys = toSortedSet(values);
    int cm = getResources().getColor(R.color.color_morning, mActivity.getTheme());
    int ca = getResources().getColor(R.color.color_afternoon, mActivity.getTheme());
    int cp = getResources().getColor(R.color.color_pause, mActivity.getTheme());
    final String [] titles = new String[3];
    for (Integer key : keys) {
      int i = 0;
      ChartEntry ce = values.get(key);
      titles[i++] = getString(R.string.statistics_morning);
      titles[i++] = getString(R.string.statistics_break);
      titles[i] = getString(R.string.statistics_afternoon);
      addPieChartEntry(dataset, cm, getString(R.string.statistics_morning), toDouble(ce.morning));
      addPieChartEntry(dataset, cp, getString(R.string.statistics_break), toDouble(ce.pause));
      addPieChartEntry(dataset, ca, getString(R.string.statistics_afternoon), toDouble(ce.afternoon));
    }
    PieChart pchart = new PieChart(dataset, mMultiRenderer);
    mChartView = new GraphicalView(mActivity, pchart);
    mMultiRenderer.setSelectableBuffer(3);
    mChartView.setOnClickListener((v) -> {
      // handle the click event on the chart
      SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
      if (seriesSelection == null) {
        Log.e(getClass().getSimpleName(), "No chart element");
      } else {
        int idx = seriesSelection.getPointIndex();
        SimpleSeriesRenderer ssr = mMultiRenderer.getSeriesRendererAt(idx);
        ssr.setHighlighted(!ssr.isHighlighted());
        if(!ssr.isHighlighted())
          dataset.set(idx, titles[idx], seriesSelection.getValue());
        else
          dataset.set(idx, fromDouble(seriesSelection.getValue()), seriesSelection.getValue());
        mChartView.invalidate();
      }
    });
    mChartContainer.addView(mChartView);
  }

  /**
   * Gets the percentage of a value.
   * @param max Max value.
   * @param percent Required percent.
   * @return double.
   */
  private double getPercentOf(final double max, final int percent) {
    return (max * percent / 100);
  }

  /**
   * Adds a bar entry.
   * @param textColor The text color.
   * @param inColor The bar color.
   * @param key The XYSeries title.
   * @param index The XYSeries index.
   * @param value The XYSeries value.
   */
  private void addBarChartEntry(int textColor, int inColor, int key, int index, double value) {
    XYSeriesRenderer renderer = new XYSeriesRenderer();
    renderer.setChartValuesTextSize(30);
    renderer.setAnnotationsTextSize(30);
    renderer.setColor(inColor);
    renderer.setAnnotationsColor(textColor);
    renderer.setFillPoints(true);
    renderer.setDisplayChartValues(false);
    XYSeries series = new XYSeries(""+key);
    series.add(index, value);
    mDatasetBar.addSeries(series);
    mMultiRenderer.addSeriesRenderer(renderer);
  }

  /**
   * Adds pie entry.
   * @param dataset The owner dataset.
   * @param inColor The background color.
   * @param category The category name.
   * @param value The category value.
   */
  private void addPieChartEntry(CategorySeries dataset, int inColor, String category, double value) {
    SimpleSeriesRenderer  renderer = new SimpleSeriesRenderer();
    renderer.setColor(inColor);
    renderer.setShowLegendItem(true);
    dataset.add(category, value);
    renderer.setHighlighted(false);
    renderer.setChartValuesFormat(NumberFormat.getPercentInstance());// Setting percentage
    mMultiRenderer.setChartTitleTextSize(30);
    mMultiRenderer.addSeriesRenderer(renderer);
  }

  /**
   * Converts entries keys to SortedSet
   * @param entries The entries.
   * @return SortedSet<Integer>
   */
  private SortedSet<Integer> toSortedSet(SparseArray<ChartEntry> entries) {
    SortedSet<Integer> keys = new TreeSet<>();
    for(int i = 0; i < entries.size(); i++)
      keys.add(entries.keyAt(i));
    return keys;
  }

}
