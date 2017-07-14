package fr.ralala.worktime.fragments;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.achartengine.GraphicalView;
import org.achartengine.chart.BarChart;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
public class ChartsFragment extends Fragment {
  private static final int BACK_TIME_DELAY = 2000;
  private static long lastBackPressed = -1;
  private MainApplication app = null;
  private LinearLayout chartContainer = null;
  private CurrentView currentView = CurrentView.YEARS;
  private DisplayMetrics metrics = null;
  private int currentYearIndex = 0;
  private int currentMonthIndex = 0;
  private int currentWeekIndex = 0;
  private XYMultipleSeriesRenderer multiRenderer = null;
  private XYMultipleSeriesDataset dataset = null;
  private GraphicalView chartView = null;

  private enum CurrentView{
    YEARS,
    MONTHS,
    WEEKS,
    DAYS
  }

  private class ChartEntry {
    WorkTimeDay work = new WorkTimeDay();
    WorkTimeDay over = new WorkTimeDay();
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
  }

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_charts, container, false);
    app = (MainApplication)getActivity().getApplicationContext();
    chartContainer = (LinearLayout) rootView.findViewById(R.id.graph);
    metrics = new DisplayMetrics();
    getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
    /* uses 80% of the screen */
    chartContainer.getLayoutParams().height = (int)getPercentOf(metrics.heightPixels, 85);
    redrawChart();
    return rootView;
  }


  public void onConfigurationChanged(Configuration newConfig) {
    redrawChart();
  }

  public void redrawChart() {
    String title = null;
    int nYear = 0;
    int nMonth = 0;
    int nWeek = 0;
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
            if(currentView != CurrentView.YEARS && currentView != CurrentView.MONTHS && currentMonthIndex == nMonth) title += " " + getResources().getStringArray(R.array.month_short)[month - 1];
            Map<Integer, List<DayEntry>> weeks = months.get(month);
            SortedSet<Integer> keysWeeks = new TreeSet<>(weeks.keySet());
            nWeek = 0;
            for (Integer week : keysWeeks) {
              if(currentView == CurrentView.YEARS || currentView == CurrentView.MONTHS || currentView == CurrentView.WEEKS || currentWeekIndex == nWeek) {
                if(currentView != CurrentView.YEARS && currentView != CurrentView.MONTHS && currentView != CurrentView.WEEKS) title += " " + getString(R.string.week) + " " + week;
                List<DayEntry> days = weeks.get(week);
                for (DayEntry de : days) {
                  if((de.getTypeMorning() == DayType.PUBLIC_HOLIDAY && de.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) || (de.getTypeMorning() == DayType.HOLIDAY && de.getTypeAfternoon() == DayType.HOLIDAY))
                    continue;
                  Integer e = 0;
                  String x_label = "";
                  if(currentView == CurrentView.YEARS) {
                    e = year;
                    x_label = "" + e;
                  }
                  else if(currentView == CurrentView.MONTHS) {
                    e = month;
                    x_label = getResources().getStringArray(R.array.month_letter)[e-1];
                  }
                  else if(currentView == CurrentView.WEEKS) {
                    e = week;
                    x_label = getString(R.string.week_letter) + " " + e;
                  }
                  else if(currentView == CurrentView.DAYS) {
                    e = de.getDay().toCalendar().get(Calendar.DAY_OF_WEEK) - 1;
                    x_label = getResources().getStringArray(R.array.days_letter)[e-1] + " " + de.getDay().getDay();
                  }
                  addDayEntry(entries, e, de, x_label);
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

    drawChart(metrics.widthPixels, title, getString(R.string.charts_hours), entries);

  }

  public void onResume() {
    super.onResume();
    redrawChart();
  }

  private void drawChart(final int width, final String title, final String legendHours, final @NonNull Map<Integer, ChartEntry> values) {
    int defaultColor = getResources().getColor(R.color.half_black, getActivity().getTheme());
    if(dataset != null)
      dataset.clear();
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

    dataset = new XYMultipleSeriesDataset();


    multiRenderer.setChartTitle("");
    multiRenderer.setChartTitleTextSize(40);
    multiRenderer.setXTitle(title);
    multiRenderer.setYTitle(legendHours == null ? "" : legendHours);
    SortedSet<Integer> keys = new TreeSet<>(values.keySet());
    //setting max values to be display in x axis
    multiRenderer.setXAxisMax(keys.size() + 1);

    multiRenderer.setBarWidth((int) getPercentOf(width / multiRenderer.getXAxisMax(), 35));
    int i = 1;
    double max = 0.0, min = Double.MAX_VALUE;

    int cunder = Color.rgb(234, 136, 37);
    int cover = Color.rgb(203, 79, 89);
    int cwork = Color.rgb(135, 160, 139);
    int seriesIdx = 0;
    List<AnnotationEntry> annotations = new ArrayList<>();
    for (Integer key : keys) {
      ChartEntry ce = values.get(key);
      WorkTimeDay withOver = ce.work; /* time include over */
      WorkTimeDay work = withOver.clone();
      work.delTime(ce.over);
      ce.dover = Double.parseDouble(withOver.timeString().replaceAll(":", "."));
      ce.dwork = Double.parseDouble(work.timeString().replaceAll(":", "."));

      if(ce.dover >= ce.dwork) {
        String annotation;
        if(ce.dover == ce.dwork)
          annotation = ce.work.timeString();
        else {
          annotation = ce.work.timeString() + "\n(+"+ce.over.timeString()+")";
        }
        /* over */
        addChartEntry(defaultColor, cover, key, i, ce.dover);
        seriesIdx++;
        /* work */
        addChartEntry(defaultColor, cwork, key, i, ce.dwork);
        annotations.add(new AnnotationEntry(seriesIdx - 1, annotation, i, ce.dover));
        seriesIdx++;
      } else {
        String annotation = ce.work.timeString() + "\n("+ce.over.timeString()+")";
        /* under */
        addChartEntry(defaultColor, cunder, key, i, ce.dwork);
        seriesIdx++;
        /* work */
        addChartEntry(defaultColor, cwork, key, i, ce.dover);
        annotations.add(new AnnotationEntry(seriesIdx - 1, annotation, i, ce.dwork));
        seriesIdx++;
      }

      multiRenderer.addXTextLabel(i, ce.x_label);
      if(ce.dover >= ce.dwork) {
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
    for(AnnotationEntry ae : annotations) {
      dataset.getSeriesAt(ae.idx).addAnnotation(ae.annotation, ae.x, ae.y + getPercentOf(max, offsetPercent));
    }

    //setting min max values to be display in y axis +- 10%
    multiRenderer.setYAxisMax(max + getPercentOf(max, 10));
    if(currentView == CurrentView.DAYS || currentView == CurrentView.WEEKS || currentView == CurrentView.MONTHS)
      multiRenderer.setYAxisMin(0);
    else
      multiRenderer.setYAxisMin(min - getPercentOf(min, 10));

    BarChart bchart = new BarChart(dataset, multiRenderer, BarChart.Type.STACKED);
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
          } else return;
          redrawChart();
        }

      }
    });
    chartContainer.addView(chartView);
  }

  private double getPercentOf(final double max, final int percent) {
    return (max * percent / 100);
  }

  private void addChartEntry(int textColor, int inColor, int key, int index, double value) {
    XYSeriesRenderer renderer = new XYSeriesRenderer();
    renderer.setChartValuesTextSize(30);
    renderer.setAnnotationsTextSize(30);
    renderer.setColor(inColor);
    renderer.setAnnotationsColor(textColor);
    renderer.setFillPoints(true);
    renderer.setDisplayChartValues(false);
    XYSeries series = new XYSeries(""+key);
    series.add(index, value);
    dataset.addSeries(series);
    multiRenderer.addSeriesRenderer(renderer);
  }

}
