package fr.ralala.worktime.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import fr.ralala.worktime.activities.MainActivity;
import fr.ralala.worktime.dialogs.DayEntryDialog;
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
public class MainFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemClickListener, DayEntryDialog.DayEntryDialogSuccessListener, SwipeDetector.SwipeDetectorListener {

  private ImageButton btPreviousMonth = null;
  private ImageButton btNextMonth = null;
  private LinearLayout llDetail = null;
  private TextView tvMonth = null;
  private TextView tvYear = null;
  private TextView tvWorkDays = null;
  private TextView tvMonthlyHours = null;
  private TextView tvMonthlyPay = null;
  private Calendar currentDate = null;
  private DaysEntriesArrayAdapter lvAdapter = null;
  private ListView days = null;
  private MainApplication app = null;
  private boolean isScrollingUp = false;
  private int lastFirstVisibleItem = 0;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_main, container, false);
    ((MainActivity)getActivity()).getSwipeDetector().setSwipeDetectorListener(this);
    app = MainApplication.getApp(getActivity());
    currentDate = Calendar.getInstance();
    currentDate.setTimeZone(TimeZone.getTimeZone("GMT"));
    currentDate.setTime(new Date());

    llDetail = (LinearLayout) rootView.findViewById(R.id.llDetail);
    btPreviousMonth = (ImageButton) rootView.findViewById(R.id.btPreviousMonth);
    btNextMonth = (ImageButton) rootView.findViewById(R.id.btNextMonth);
    tvMonth = (TextView) rootView.findViewById(R.id.tvMonth);
    tvYear = (TextView) rootView.findViewById(R.id.tvYear);
    tvWorkDays = (TextView) rootView.findViewById(R.id.tvWorkDays);
    tvMonthlyHours = (TextView) rootView.findViewById(R.id.tvMonthlyHours);
    tvMonthlyPay = (TextView) rootView.findViewById(R.id.tvMonthlyPay);
    days = (ListView) rootView.findViewById(R.id.days);

    btPreviousMonth.setOnClickListener(this);
    btNextMonth.setOnClickListener(this);

    lvAdapter = new DaysEntriesArrayAdapter(
      getContext(), R.layout.days_listview_item, new ArrayList<DayEntry>());
    days.setAdapter(lvAdapter);
    days.setOnItemClickListener(this);

    days.setOnScrollListener(new AbsListView.OnScrollListener(){
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      }
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getId() == days.getId()) {
          final int currentFirstVisibleItem = days.getFirstVisiblePosition();
          if (currentFirstVisibleItem > lastFirstVisibleItem) {
            isScrollingUp = false;
          } else if (currentFirstVisibleItem < lastFirstVisibleItem) {
            isScrollingUp = true;
          }
          /* change the visibility if 5% of the list is displayed or hidden */
          int k = (int)(lvAdapter.getCount()*(5.0f/100.0f));
          if(!isScrollingUp && currentFirstVisibleItem > k && llDetail.getVisibility() == View.VISIBLE)
            llDetail.setVisibility(View.GONE);
          else if(isScrollingUp && currentFirstVisibleItem < k && llDetail.getVisibility() == View.GONE)
            llDetail.setVisibility(View.VISIBLE);
          else if(currentFirstVisibleItem == 0 && llDetail.getVisibility() != View.VISIBLE)
            llDetail.setVisibility(View.VISIBLE);
          /* store previous item */
          lastFirstVisibleItem = currentFirstVisibleItem;
        }
      }
    });

    updateDate();
    return rootView;
  }

  public void dialogAddEntry(final DayEntry oldEntry, final DayEntry newEntry) {
    if(oldEntry.getName().isEmpty() || !oldEntry.match(newEntry))
      app.getDaysFactory().remove(oldEntry);
    if(newEntry.getStart().isValidTime())
      app.getDaysFactory().add(newEntry);
    updateDate();
  }

  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    DayEntry de = lvAdapter.getItem(i);
    new DayEntryDialog(getActivity(), app, de, true, this).open();
  }

  public void onClick(final View v) {
    if(v.equals(btPreviousMonth)) {
      currentDate.add(Calendar.MONTH, -1);
    } else if(v.equals(btNextMonth)) {
      currentDate.add(Calendar.MONTH, 1);
    }
    updateDate();
  }

  private void updateDate() {
    lvAdapter.clear();
    int month = currentDate.get(Calendar.MONTH);
    int minDay = 1;
    int maxDay = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH);
    /* init the top components */
    String smonth = AndroidHelper.getMonthString(month);
    final String ss_month = String.format(Locale.US, "%02d", month + 1);
    final String ss_maxDay = String.format(Locale.US, "%02d", maxDay);
    smonth += "\n01/" + ss_month + " - " + ss_maxDay + "/" + ss_month;
    tvMonth.setText(smonth);
    tvYear.setText(String.valueOf(currentDate.get(Calendar.YEAR)));

    int wDays = 0;
    long hours = 0L, minutes = 0L, overTime = 0L;
    int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
    double totalPay = 0.0;
    /* loop for each days in the month */
    for(int day = minDay; day <= maxDay; day++) {
      currentDate.set(Calendar.DAY_OF_MONTH, day);
      DayEntry de = new DayEntry(currentDate, DayType.ERROR);
      de.setAmountByHour(app.getAmountByHour()); /* set default amount */
      /* Force public holiday */
      if(app.getPublicHolidaysFactory().isPublicHolidays(de.getDay()))
        de.setType(DayType.PUBLIC_HOLIDAY);
      int now = currentDate.get(Calendar.DAY_OF_WEEK);
      /* count working day */
      if(now != Calendar.SUNDAY && now != Calendar.SATURDAY && !app.getPublicHolidaysFactory().isPublicHolidays(de.getDay()))
        wDays++;
      /* reload data if the current day is already inserted */
      totalPay += app.getDaysFactory().checkForDayDateAndCopy(de);
      WorkTimeDay wt = de.getWorkTime();
      hours += wt.getHours();
      minutes += wt.getMinutes();
      if(de.getType() == DayType.AT_WORK)
        overTime += de.getOverTimeMs(app);
      lvAdapter.add(de);
    }
    /* reload work day label */
    currentDate.set(Calendar.DAY_OF_MONTH, currentDay);
    String workDays = getString(R.string.work_days) + ": " + String.format(Locale.US, "%02d", wDays) + " " + getString(R.string.days_lower_case);
    tvWorkDays.setText(workDays);

    /* reload the monthly hours label */
    WorkTimeDay monthly_hours = new WorkTimeDay().fromTimeUsingCalendar(hours, minutes);
    WorkTimeDay over_hours = new WorkTimeDay().fromTimeUsingCalendar(overTime);
    /* substract legal working time */
    WorkTimeDay wtdEstimatedMonthlyHours = app.getEstimatedMonthlyHours(wDays);
    String monthlyHours = getString(R.string.monthly_hours) + ": " +
      String.format(Locale.US, "%d:%02d/%d:%02d (%s%d:%02d)",
        monthly_hours.getHours(), monthly_hours.getMinutes(),
        wtdEstimatedMonthlyHours.getHours(), wtdEstimatedMonthlyHours.getMinutes(),
        over_hours.getHours() > 0 ? "+" : "", over_hours.getHours(), over_hours.getMinutes());

    tvMonthlyHours.setText(monthlyHours);
    /* init total pay label */
    tvMonthlyPay.setText(getString(R.string.monthly_pay) + ": " + String.format(Locale.US, "%.02f", totalPay) + app.getCurrency());

    lvAdapter.notifyDataSetChanged();
  }


  public void leftToRightSwipe() {
    btNextMonth.callOnClick();
  }

  public void rightToLeftSwipe() {
    btPreviousMonth.callOnClick();
  }

}
