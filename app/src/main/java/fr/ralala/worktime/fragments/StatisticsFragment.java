package fr.ralala.worktime.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

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
  private static long lastBackPressed = -1;
  private MainApplication app = null;
  private LinearLayout chartContainer = null;
  private CurrentView currentView = CurrentView.YEARS;
  private DisplayMetrics metrics = null;
  private int currentYearIndex = 0;
  private int currentMonthIndex = 0;
  private int currentWeekIndex = 0;
  private int currentDayIndex = 0;
  private XYMultipleSeriesRenderer multiRenderer = null;
  private XYMultipleSeriesDataset datasetBar = null;
  private GraphicalView chartView = null;

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

  public boolean consumeBackPressed() {
    if(currentView == CurrentView.YEARS) {
      if (lastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        return false;
      } else {
        AndroidHelper.toast(getContext(), R.string.on_double_back_back_text);
        AndroidHelper.vibrate(getContext(), 100);
      }
      lastBackPressed = System.currentTimeMillis();
    }
    else if(currentView == CurrentView.DETAIL)
      currentView = CurrentView.DAYS;
    else if(currentView == CurrentView.DAYS)
      currentView = CurrentView.WEEKS;
    else if(currentView == CurrentView.WEEKS)
      currentView = CurrentView.MONTHS;
    else if(currentView == CurrentView.MONTHS)
      currentView = CurrentView.YEARS;
    redrawChart();
    return true;
  }

  private void addDayEntry(Map<Integer, ChartEntry> entries, Integer key, DayEntry de, String x_label) {
    if(!entries.containsKey(key)) {
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

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_statistics, container, false);
    app = (MainApplication)getActivity().getApplicationContext();
    Button cancel = (Button) rootView.findViewById(R.id.cancel);
    cancel.setOnClickListener(this);
    chartContainer = (LinearLayout) rootView.findViewById(R.id.graph);
    metrics = new DisplayMetrics();
    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
    /* uses 80% of the screen */
    chartContainer.getLayoutParams().height = (int)getPercentOf(metrics.heightPixels, 85);
    redrawChart();
    return rootView;
  }


  @Override
  public void onClick(View view) {
    currentView = CurrentView.YEARS;
    redrawChart();
  }

  public void onConfigurationChanged(Configuration newConfig) {
    redrawChart();
  }

  public void redrawChart() {
    String title = null;
    int nYear = 0;
    int nMonth;
    int nWeek;
    int nDay;
    Map<Integer, ChartEntry> entries = new HashMap<>();

    Map<Integer, Map<Integer, Map<Integer, List<DayEntry>>>> years = app.getDaysFactory().getDays();
    SortedSet<Integer> keysYears = new TreeSet<>(years.keySet());

    for (Integer year : keysYears) {
      if(currentView == CurrentView.YEARS || currentYearIndex == nYear) {
        if(currentView != CurrentView.YEARS) title = ""+year;
        Map<Integer, Map<Integer, List<DayEntry>>> months = years.get(year);
        SortedSet<Integer> keysMonths = new TreeSet<>(months.keySet());
        nMonth = 0;
        for (Integer month : keysMonths) {
          if(currentView == CurrentView.YEARS || currentView == CurrentView.MONTHS || currentMonthIndex == nMonth) {
            Map<Integer, List<DayEntry>> weeks = months.get(month);
            SortedSet<Integer> keysWeeks = new TreeSet<>(weeks.keySet());
            nWeek = 0;
            for (Integer week : keysWeeks) {
              if(currentView == CurrentView.YEARS || currentView == CurrentView.MONTHS || currentView == CurrentView.WEEKS || currentWeekIndex == nWeek) {
                List<DayEntry> days = weeks.get(week);
                nDay = 0;
                for (DayEntry de : days) {
                  int day = de.getDay().getDay();
                  if (currentView == CurrentView.YEARS || currentView == CurrentView.MONTHS || currentView == CurrentView.WEEKS || currentView == CurrentView.DAYS || currentDayIndex == nDay) {
                    if ((de.getTypeMorning() == DayType.PUBLIC_HOLIDAY && de.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) || (de.getTypeMorning() == DayType.HOLIDAY && de.getTypeAfternoon() == DayType.HOLIDAY))
                      continue;
                    Integer e = 0;
                    String x_label = "";
                    if (currentView == CurrentView.YEARS) {
                      e = year;
                      x_label = "" + e;
                    } else if (currentView == CurrentView.MONTHS) {
                      e = month;
                      x_label = getResources().getStringArray(R.array.month_letter)[e - 1];
                      title = String.format(Locale.US, "%d", year);
                    } else if (currentView == CurrentView.WEEKS) {
                      e = week;
                      x_label = getString(R.string.week_letter) + " " + e;
                      title = String.format(Locale.US, "%d %s", year, getResources().getStringArray(R.array.month_short)[month - 1]);
                    } else if (currentView == CurrentView.DAYS) {
                      e = de.getDay().toCalendar().get(Calendar.DAY_OF_WEEK) - 1;
                      x_label = getResources().getStringArray(R.array.days_letter)[e - 1] + " " + de.getDay().getDay();
                      title = String.format(Locale.US, "%d %s %s %d", year, getResources().getStringArray(R.array.month_short)[month - 1], getString(R.string.week), week);
                    } else if (currentView == CurrentView.DETAIL) {
                      e = day;
                      title = String.format(Locale.US, "%d %s %d (%s %d)", day, getResources().getStringArray(R.array.month_short)[month - 1], year, getString(R.string.week), week);
                    }
                    addDayEntry(entries, e, de, x_label);
                  }
                  nDay++;
                }
              }
              nWeek++;
            }
          }
          nMonth++;
        }
      }
      nYear++;
    }
    if((currentView != CurrentView.DETAIL))
      drawBarChart(metrics.widthPixels, title, getString(R.string.statistics_hours), entries);
    else
      drawPieChart(metrics.widthPixels, title, entries);

  }

  public void onResume() {
    super.onResume();
    redrawChart();
  }

  private double toDouble(WorkTimeDay w) {
    return Double.parseDouble(w.timeString().replaceAll(":", "\\."));
  }
  private String fromDouble(double d) {
    DecimalFormat df = new DecimalFormat("#00.00");
    return df.format(d).replaceAll(",", ":");
  }

  private void initMultiRender(final String title, final String legendHours) {
    int defaultColor = getResources().getColor(R.color.half_black, getActivity().getTheme());
    if(multiRenderer != null) {
      multiRenderer.clearXTextLabels();
      multiRenderer.clearYTextLabels();
      multiRenderer.removeAllRenderers();
    }
    if(chartContainer != null)
      chartContainer.removeAllViews();

    multiRenderer = new XYMultipleSeriesRenderer();
    multiRenderer.setXLabels(0);
    multiRenderer.setZoomButtonsVisible(false);
    multiRenderer.setPanEnabled(false, false);
    multiRenderer.setPanEnabled(false, false);
    multiRenderer.setZoomRate(0.2f);
    multiRenderer.setZoomEnabled(false, false);
    multiRenderer.setMarginsColor(Color.argb(0x00, 0xff, 0x00, 0x00));
    multiRenderer.setLegendTextSize(30);
    multiRenderer.setChartTitleTextSize(30);
    multiRenderer.setAxisTitleTextSize(30);
    multiRenderer.setLabelsTextSize(40);
    multiRenderer.setFitLegend(true);
    multiRenderer.setShowGrid(false);
    multiRenderer.setZoomEnabled(false);
    multiRenderer.setExternalZoomEnabled(false);
    multiRenderer.setAntialiasing(true);
    multiRenderer.setInScroll(false);
    multiRenderer.setShowLegend(false);
    multiRenderer.setLegendHeight(30);
    multiRenderer.setXLabelsAlign(Paint.Align.CENTER);
    //multiRenderer.setYLabelsAlign(Paint.Align.LEFT);

    multiRenderer.setYAxisAlign(Paint.Align.LEFT, 0);
    multiRenderer.setYLabelsAlign(Paint.Align.LEFT, 0);
    multiRenderer.setYLabelsColor(0, defaultColor);
    multiRenderer.setXLabelsColor(defaultColor);
    multiRenderer.setLabelsColor(defaultColor);
    multiRenderer.setGridColor(defaultColor);
    multiRenderer.setAxesColor(defaultColor);

    multiRenderer.setYLabels(10);
    //setting used to move the graph on xaxiz to .5 to the right
    multiRenderer.setXAxisMin(-.5);
    //setting bar size or space between two bars
    multiRenderer.setBarSpacing(0.5);
    //Setting background color of the graph to transparent
    multiRenderer.setBackgroundColor(Color.TRANSPARENT);
    multiRenderer.setApplyBackgroundColor(true);
    //setting the margin size for the graph in the order top, left, bottom, right
    multiRenderer.setMargins(new int[]{30, 30, 30, 30});

    if(currentView != CurrentView.DETAIL) {
      multiRenderer.setChartTitle("");
      multiRenderer.setXTitle(title);
    } else {
      multiRenderer.setChartTitle(title);
      multiRenderer.setXTitle("");
    }
    multiRenderer.setChartTitleTextSize(40);
    multiRenderer.setYTitle(legendHours == null ? "" : legendHours);
  }

  private void drawBarChart(final int width, final String title, final String legendHours, final @NonNull Map<Integer, ChartEntry> values) {
    if(datasetBar != null)
      datasetBar.clear();
    initMultiRender(title, legendHours);

    datasetBar = new XYMultipleSeriesDataset();

    SortedSet<Integer> keys = new TreeSet<>(values.keySet());
    //setting max values to be display in x axis
    multiRenderer.setXAxisMax(keys.size() + 1);
    multiRenderer.setBarWidth((int) getPercentOf(width / multiRenderer.getXAxisMax(), 35));
    int i = 1;
    double max = 0.0, min = Double.MAX_VALUE;

    int defaultColor = getResources().getColor(R.color.half_black, getActivity().getTheme());
    int cunder = getResources().getColor(R.color.color_under, getActivity().getTheme());
    int cover = getResources().getColor(R.color.color_over, getActivity().getTheme());
    int cwork = getResources().getColor(R.color.color_work, getActivity().getTheme());
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

      multiRenderer.addXTextLabel(i, ce.x_label);
      if (ce.dover >= ce.dwork) {
        if (max < ce.dover) max = ce.dover;
        if (min > ce.dwork) min = ce.dwork;
      } else {
        if (max < ce.dwork) max = ce.dwork;
        if (min > ce.dover) min = ce.dover;
      }
      i++;
    }

    int orientation = getActivity().getResources().getConfiguration().orientation;
    int offsetPercent = orientation == Configuration.ORIENTATION_PORTRAIT ? 3 : 6;
    for (AnnotationEntry ae : annotations) {
      datasetBar.getSeriesAt(ae.idx).addAnnotation(ae.annotation, ae.x, ae.y + getPercentOf(max, offsetPercent));
    }

    //setting min max values to be display in y axis +- 10%
    multiRenderer.setYAxisMax(max + getPercentOf(max, 10));
    if (currentView == CurrentView.DAYS || currentView == CurrentView.WEEKS || currentView == CurrentView.MONTHS)
      multiRenderer.setYAxisMin(0);
    else
      multiRenderer.setYAxisMin(min - getPercentOf(min, 10));

    BarChart bchart = new BarChart(datasetBar, multiRenderer, BarChart.Type.STACKED);
    chartView = new GraphicalView(getContext(), bchart);

    multiRenderer.setSelectableBuffer(100);
    chartView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // handle the click event on the chart
        SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
        if (seriesSelection == null) {
          Log.e("TAG", "No chart element");
        } else {
          int x = ((int) seriesSelection.getXValue()) - 1;
          if (currentView == CurrentView.YEARS) {
            currentView = CurrentView.MONTHS;
            currentYearIndex = x;
          } else if (currentView == CurrentView.MONTHS) {
            currentView = CurrentView.WEEKS;
            currentMonthIndex = x;
          } else if (currentView == CurrentView.WEEKS) {
            currentView = CurrentView.DAYS;
            currentWeekIndex = x;
          } else if (currentView == CurrentView.DAYS) {
            currentView = CurrentView.DETAIL;
            currentDayIndex = x;
          } else return;
          redrawChart();
        }

      }
    });

    chartContainer.addView(chartView);
  }


  private void drawPieChart(final int width, final String title, final @NonNull Map<Integer, ChartEntry> values) {
    initMultiRender(title, null);
    final CategorySeries dataset = new CategorySeries("title");
    SortedSet<Integer> keys = new TreeSet<>(values.keySet());
    int cm = getResources().getColor(R.color.color_morning, getActivity().getTheme());
    int ca = getResources().getColor(R.color.color_afternoon, getActivity().getTheme());
    int cp = getResources().getColor(R.color.color_pause, getActivity().getTheme());
    final String [] titles = new String[3];
    for (Integer key : keys) {
      int i = 0;
      ChartEntry ce = values.get(key);
      titles[i++] = getString(R.string.statistics_morning);
      titles[i++] = getString(R.string.statistics_break);
      titles[i++] = getString(R.string.statistics_afternoon);
      addPieChartEntry(dataset, cm, getString(R.string.statistics_morning), toDouble(ce.morning));
      addPieChartEntry(dataset, cp, getString(R.string.statistics_break), toDouble(ce.pause));
      addPieChartEntry(dataset, ca, getString(R.string.statistics_afternoon), toDouble(ce.afternoon));
    }
    PieChart pchart = new PieChart(dataset, multiRenderer);
    chartView = new GraphicalView(getContext(), pchart);
    multiRenderer.setSelectableBuffer(3);
    chartView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        // handle the click event on the chart
        SeriesSelection seriesSelection = chartView.getCurrentSeriesAndPoint();
        if (seriesSelection == null) {
          Log.e("TAG", "No chart element");
        } else {
          int idx = seriesSelection.getPointIndex();
          SimpleSeriesRenderer ssr = multiRenderer.getSeriesRendererAt(idx);
          ssr.setHighlighted(!ssr.isHighlighted());
          if(!ssr.isHighlighted())
            dataset.set(idx, titles[idx], seriesSelection.getValue());
          else
            dataset.set(idx, fromDouble(seriesSelection.getValue()), seriesSelection.getValue());
          chartView.invalidate();
        }

      }
    });
    chartContainer.addView(chartView);
  }

  private double getPercentOf(final double max, final int percent) {
    return (max * percent / 100);
  }

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
    datasetBar.addSeries(series);
    multiRenderer.addSeriesRenderer(renderer);
  }

  private void addPieChartEntry(CategorySeries dataset, int inColor, String category, double value) {
    SimpleSeriesRenderer  renderer = new SimpleSeriesRenderer();
    renderer.setColor(inColor);
    renderer.setShowLegendItem(true);
    dataset.add(category, value);
    renderer.setHighlighted(false);
    renderer.setChartValuesFormat(NumberFormat.getPercentInstance());// Setting percentage
    multiRenderer.setChartTitleTextSize(30);
    multiRenderer.addSeriesRenderer(renderer);
  }

}
