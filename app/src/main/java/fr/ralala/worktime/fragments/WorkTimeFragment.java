package fr.ralala.worktime.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import fr.ralala.worktime.activities.DayActivity;
import fr.ralala.worktime.activities.MainActivity;

import fr.ralala.worktime.dialogs.MonthDetailsDialog;
import fr.ralala.worktime.quickaccess.QuickAccessService;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.adapters.DaysEntriesArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.SwipeDetector;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the main fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class WorkTimeFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, SwipeDetector.SwipeDetectorListener {

  private ImageButton btPreviousMonth = null;
  private ImageButton btNextMonth = null;
  private RelativeLayout rlDetails = null;
  private TextView tvMonth = null;
  private TextView tvYear = null;
  private TextView tvWorkDays = null;
  private TextView tvMonthlyHours = null;
  private DaysEntriesArrayAdapter lvAdapter = null;
  private ListView days = null;
  private MainApplication app = null;
  private MonthDetailsDialog monthDetailsDialog = null;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_main, container, false);
    ((MainActivity)getActivity()).getSwipeDetector().setSwipeDetectorListener(this);
    app = MainApplication.getApp(getActivity());
    //app.getCurrentDate().setTime(new Date());

    monthDetailsDialog = new MonthDetailsDialog(getActivity(), app);

    rlDetails = (RelativeLayout) rootView.findViewById(R.id.rlDetails);
    btPreviousMonth = (ImageButton) rootView.findViewById(R.id.btPreviousMonth);
    btNextMonth = (ImageButton) rootView.findViewById(R.id.btNextMonth);
    tvMonth = (TextView) rootView.findViewById(R.id.tvMonth);
    tvYear = (TextView) rootView.findViewById(R.id.tvYear);
    tvWorkDays = (TextView) rootView.findViewById(R.id.tvWorkDays);
    tvMonthlyHours = (TextView) rootView.findViewById(R.id.tvMonthlyHours);
    days = (ListView) rootView.findViewById(R.id.days);

    rlDetails.setOnClickListener(this);
    btPreviousMonth.setOnClickListener(this);
    btNextMonth.setOnClickListener(this);

    lvAdapter = new DaysEntriesArrayAdapter(
      getContext(), R.layout.days_listview_item, new ArrayList<DayEntry>());
    days.setAdapter(lvAdapter);
    days.setOnItemClickListener(this);
    LinearLayout llYearMonth = (LinearLayout)rootView.findViewById(R.id.llYearMonth);
    llYearMonth.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        AndroidHelper.openDatePicker(getActivity(), app.getCurrentDate(), new DatePickerDialog.OnDateSetListener() {
          @Override
          public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
            app.getCurrentDate().set(Calendar.YEAR, selectedYear);
            app.getCurrentDate().set(Calendar.MONTH, selectedMonth);
            app.getCurrentDate().set(Calendar.DAY_OF_MONTH, selectedDay);
            updateAll();
          }
        });
      }
    });

    days.setOnScrollListener(new AbsListView.OnScrollListener(){
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      }
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getId() == days.getId()) {
          final int currentFirstVisibleItem = days.getFirstVisiblePosition();
          boolean isScrollingUp = (currentFirstVisibleItem < app.getLastFirstVisibleItem());
          /* change the visibility if 5% of the list is displayed or hidden */
          int k = at5Percent();
          if(!isScrollingUp && currentFirstVisibleItem > k && rlDetails.getVisibility() == View.VISIBLE)
            rlDetails.setVisibility(View.GONE);
          else if(isScrollingUp && currentFirstVisibleItem < k && rlDetails.getVisibility() == View.GONE)
            rlDetails.setVisibility(View.VISIBLE);
          else if(currentFirstVisibleItem == 0 && rlDetails.getVisibility() != View.VISIBLE)
            rlDetails.setVisibility(View.VISIBLE);
          /* store previous item */
          app.setLastFirstVisibleItem(currentFirstVisibleItem);
        }
      }
    });
    return rootView;
  }

  public void onResume() {
    super.onResume();
    updateAll();
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    if(AndroidHelper.isServiceRunning(getActivity(), QuickAccessService.class)) {
      AndroidHelper.toast(getActivity(), R.string.service_running);
      return;
    }
    DayEntry de = lvAdapter.getItem(i);
    if(de == null || de.getTypeMorning() == DayType.PUBLIC_HOLIDAY || de.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) {
      AndroidHelper.toast(getActivity(), R.string.error_editing_public_holiday);
      return;
    }
    DayActivity.startActivity(getActivity(), de.getDay().dateString(), true);
  }

  public void onClick(final View v) {
    if(v.equals(btPreviousMonth)) {
      app.getCurrentDate().add(Calendar.MONTH, -1);
      app.setLastFirstVisibleItem(0);
      updateAll();
    } else if(v.equals(btNextMonth)) {
      app.getCurrentDate().add(Calendar.MONTH, 1);
      app.setLastFirstVisibleItem(0);
      updateAll();
    } else if(v.equals(rlDetails)) {
      monthDetailsDialog.reloadDetails(
        app.getCurrentDate().get(Calendar.MONTH),
        app.getCurrentDate().get(Calendar.YEAR));
      monthDetailsDialog.open();
    }
  }

  public void updateAll() {
    updateTop();
    updateDates();
  }

  private void updateTop() {
    /* init the top components */
    int month = app.getCurrentDate().get(Calendar.MONTH);
    String smonth = AndroidHelper.getMonthString(month);
    final String ss_month = String.format(Locale.US, "%02d", month + 1);
    final String ss_maxDay = String.format(Locale.US, "%02d", app.getCurrentDate().getActualMaximum(Calendar.DAY_OF_MONTH));
    smonth += "\n01/" + ss_month + " - " + ss_maxDay + "/" + ss_month;
    tvMonth.setText(smonth);
    tvYear.setText(String.valueOf(app.getCurrentDate().get(Calendar.YEAR)));
    tvYear.setText(String.valueOf(app.getCurrentDate().get(Calendar.YEAR)));
  }

  private int at5Percent() {
    return (int)(lvAdapter.getCount()*(5.0f/100.0f));
  }

  private void updateDates() {
    lvAdapter.clear();
    int minDay = 1;
    int maxDay = app.getCurrentDate().getActualMaximum(Calendar.DAY_OF_MONTH);
    List<DayEntry> wDays = new ArrayList<>();
    int index = 0;
    double realwDays = 0.0;
    int currentDay = app.getCurrentDate().get(Calendar.DAY_OF_MONTH);
    /* get first week */
    app.getCurrentDate().set(Calendar.DAY_OF_MONTH, 1);
    int firstWeek = app.getCurrentDate().get(Calendar.WEEK_OF_YEAR);
    WorkTimeDay wtdTotalWorkTime = new WorkTimeDay();
    WorkTimeDay wtdnow = WorkTimeDay.now();
    /* loop for each days in the month */
    for(int day = minDay; day <= maxDay; ++day) {
      app.getCurrentDate().set(Calendar.DAY_OF_MONTH, day);
      DayEntry de = new DayEntry(getActivity(), app.getCurrentDate(), DayType.ERROR, DayType.ERROR);
      de.setAmountByHour(app.getAmountByHour()); /* set default amount */
      /* Force public holiday */
      if(app.getPublicHolidaysFactory().isPublicHolidays(de.getDay())) {
        de.setTypeMorning(DayType.PUBLIC_HOLIDAY);
        de.setTypeAfternoon(DayType.PUBLIC_HOLIDAY);
      }
      int now = app.getCurrentDate().get(Calendar.DAY_OF_WEEK);
      /* reload data if the current day is already inserted */
      app.getDaysFactory().checkForDayDateAndCopy(de);
      /* count working day */
      if(now != Calendar.SUNDAY && now != Calendar.SATURDAY && !app.getPublicHolidaysFactory().isPublicHolidays(de.getDay())) {
        wDays.add(de);
        if(de.getTypeMorning() == DayType.AT_WORK) realwDays += 0.5;
        if(de.getTypeAfternoon() == DayType.AT_WORK) realwDays += 0.5;
      }
      lvAdapter.add(de);
      if(app.isScrollToCurrentDay() && app.getLastFirstVisibleItem() == 0 && de.getDay().dateString().equals(wtdnow.dateString())) {
        app.setLastFirstVisibleItem(index);
      }
      else if(app.getLastFirstVisibleItem() == 0) index++;
    }
    Map<String, DayEntry> map = app.getDaysFactory().toDaysMap();
    int min = (firstWeek == 52 ? 1 : firstWeek);
    for(int w = min; w <= min + 6; ++w) {
      WorkTimeDay wtdWorkTimeFromWeek =  app.getDaysFactory().getWorkTimeDayFromWeek(map, w, app.getCurrentDate().get(Calendar.MONTH), app.getCurrentDate().get(Calendar.YEAR));
      if(wtdWorkTimeFromWeek.isValidTime())
        wtdTotalWorkTime.addTime(wtdWorkTimeFromWeek);
    }
    /* reload work day label */
    app.getCurrentDate().set(Calendar.DAY_OF_MONTH, currentDay);
    int n = Integer.parseInt((""+realwDays).split("\\.")[1]);
    String workDays = getString(R.string.work_days) + ": ";
    if(n != 0)
      workDays += String.format(Locale.US, "%02d.%02d/%02d", (int)realwDays, n, wDays.size());
    else
      workDays += String.format(Locale.US, "%02d/%02d", (int)realwDays, wDays.size());
    workDays += " " + getString(R.string.days_lower_case);
    tvWorkDays.setText(workDays);

    /* substract legal working time */
    WorkTimeDay wtdEstimatedMonthlyHours = app.getEstimatedHours(wDays);
    String monthlyHours = getString(R.string.monthly_hours) + ": " +
      String.format(Locale.US, "%d:%02d/%d:%02d",
        wtdTotalWorkTime.getHours(), wtdTotalWorkTime.getMinutes(),
        wtdEstimatedMonthlyHours.getHours(), wtdEstimatedMonthlyHours.getMinutes());

    tvMonthlyHours.setText(monthlyHours);
    lvAdapter.notifyDataSetChanged();
    /* restores the scroll position and reloads the adapter else the listview seems not agree with the call of setSelection */
    days.setAdapter(days.getAdapter());
    days.setSelection(app.getLastFirstVisibleItem());
    if(app.isScrollToCurrentDay()) {
        /* change the visibility if 5% of the list is displayed or hidden */
      int k = at5Percent();
      if((app.getLastFirstVisibleItem() == 0 || app.getLastFirstVisibleItem() < k) && rlDetails.getVisibility() != View.VISIBLE)
        rlDetails.setVisibility(View.VISIBLE);
      else if (app.getLastFirstVisibleItem() > k && rlDetails.getVisibility() == View.VISIBLE)
        rlDetails.setVisibility(View.GONE);
    }
  }


  public void leftToRightSwipe() {
    btNextMonth.callOnClick();
  }

  public void rightToLeftSwipe() {
    btPreviousMonth.callOnClick();
  }

}
