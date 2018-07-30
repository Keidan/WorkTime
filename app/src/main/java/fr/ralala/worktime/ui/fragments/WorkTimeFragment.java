package fr.ralala.worktime.ui.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

import fr.ralala.worktime.ui.activities.DayActivity;
import fr.ralala.worktime.ui.activities.MainActivity;

import fr.ralala.worktime.ui.dialogs.MonthDetailsDialog;
import fr.ralala.worktime.services.QuickAccessService;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.adapters.DaysEntriesArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.ui.utils.SwipeDetector;
import fr.ralala.worktime.ui.utils.UIHelper;

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

  private ImageButton mBtPreviousMonth = null;
  private ImageButton mBtNextMonth = null;
  private RelativeLayout mRlDetails = null;
  private TextView mTvMonth = null;
  private TextView mTvYear = null;
  private TextView mTvWorkDays = null;
  private TextView mTvMonthlyHours = null;
  private DaysEntriesArrayAdapter mLvAdapter = null;
  private ListView mDays = null;
  private MainApplication mApp = null;
  private MonthDetailsDialog mMonthDetailsDialog = null;
  private MainActivity mActivity;

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
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_main, container, false);
    mActivity = (MainActivity)getActivity();
    assert mActivity != null;
    mActivity.getSwipeDetector().setSwipeDetectorListener(this);
    mApp = MainApplication.getApp(getActivity());
    //mApp.getCurrentDate().setTime(new Date());

    mMonthDetailsDialog = new MonthDetailsDialog(getActivity(), mApp);

    mRlDetails = rootView.findViewById(R.id.rlDetails);
    mBtPreviousMonth = rootView.findViewById(R.id.btPreviousMonth);
    mBtNextMonth = rootView.findViewById(R.id.btNextMonth);
    mTvMonth = rootView.findViewById(R.id.tvMonth);
    mTvYear = rootView.findViewById(R.id.tvYear);
    mTvWorkDays = rootView.findViewById(R.id.tvWorkDays);
    mTvMonthlyHours = rootView.findViewById(R.id.tvMonthlyHours);
    mDays = rootView.findViewById(R.id.days);

    mRlDetails.setOnClickListener(this);
    mBtPreviousMonth.setOnClickListener(this);
    mBtNextMonth.setOnClickListener(this);

    mLvAdapter = new DaysEntriesArrayAdapter(
      getContext(), R.layout.days_listview_item, new ArrayList<>());
    mDays.setAdapter(mLvAdapter);
    mDays.setOnItemClickListener(this);
    LinearLayout llYearMonth = rootView.findViewById(R.id.llYearMonth);
    llYearMonth.setOnClickListener((v) ->
        UIHelper.openDatePicker(getActivity(), mApp.getCurrentDate(), (view, selectedYear, selectedMonth, selectedDay) -> {
          mApp.getCurrentDate().set(Calendar.YEAR, selectedYear);
          mApp.getCurrentDate().set(Calendar.MONTH, selectedMonth);
          mApp.getCurrentDate().set(Calendar.DAY_OF_MONTH, selectedDay);
        updateAll();
      })
    );

    mDays.setOnScrollListener(new AbsListView.OnScrollListener(){
      public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
      }
      public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (view.getId() == mDays.getId()) {
          final int currentFirstVisibleItem = mDays.getFirstVisiblePosition();
          boolean isScrollingUp = (currentFirstVisibleItem < mApp.getLastFirstVisibleItem());
          /* change the visibility if 5% of the list is displayed or hidden */
          int k = at5Percent();
          if(!isScrollingUp && currentFirstVisibleItem > k && mRlDetails.getVisibility() == View.VISIBLE)
            mRlDetails.setVisibility(View.GONE);
          else if(isScrollingUp && currentFirstVisibleItem < k && mRlDetails.getVisibility() == View.GONE)
            mRlDetails.setVisibility(View.VISIBLE);
          else if(currentFirstVisibleItem == 0 && mRlDetails.getVisibility() != View.VISIBLE)
            mRlDetails.setVisibility(View.VISIBLE);
          /* store previous item */
          mApp.setLastFirstVisibleItem(currentFirstVisibleItem);
        }
      }
    });
    updateAll();
    return rootView;
  }

  /**
   * Receive the result from a previous call to startActivityForResult
   * @param requestCode The integer request code originally supplied to startActivityForResult.
   * @param resultCode The integer result code returned by the child activity through its setResult().
   * @param data An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
   */
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == DayActivity.REQUEST_START_ACTIVITY)
      updateAll();
  }

  /**
   * Called when the user click on a specific date.
   * @param adapterView The adapter view.
   * @param view The view.
   * @param i The view position.
   * @param l See official javadoc (not used here).
   */
  @Override
  public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    if(AndroidHelper.isServiceRunning(getActivity(), QuickAccessService.class)) {
      UIHelper.toast(getActivity(), R.string.service_running);
      return;
    }
    DayEntry de = mLvAdapter.getItem(i);
    if(de == null || de.getTypeMorning() == DayType.PUBLIC_HOLIDAY || de.getTypeAfternoon() == DayType.PUBLIC_HOLIDAY) {
      UIHelper.toast(getActivity(), R.string.error_editing_public_holiday);
      return;
    }
    DayActivity.startActivity(this, de.getDay().dateString(), true);
  }

  /**
   * Called when a button is clicked (previous, next and details).
   * @param v The clicked view.
   */
  public void onClick(final View v) {
    if(v.equals(mBtPreviousMonth)) {
      mApp.getCurrentDate().add(Calendar.MONTH, -1);
      mApp.setLastFirstVisibleItem(0);
      updateAll();
    } else if(v.equals(mBtNextMonth)) {
      mApp.getCurrentDate().add(Calendar.MONTH, 1);
      mApp.setLastFirstVisibleItem(0);
      updateAll();
    } else if(v.equals(mRlDetails)) {
      mMonthDetailsDialog.reloadDetails(
          mApp.getCurrentDate().get(Calendar.MONTH),
          mApp.getCurrentDate().get(Calendar.YEAR));
      mMonthDetailsDialog.open();
    }
  }

  /**
   * Updates the top and the dates part.
   */
  public void updateAll() {
    mActivity.progressShow(true);
    new Thread(() -> {
      updateTop();
      updateDates();
       mActivity.runOnUiThread(() -> mActivity.progressDismiss());
    }).start();
  }

  /**
   * Updates the top of the view.
   */
  private void updateTop() {
    /* init the top components */
    int month = mApp.getCurrentDate().get(Calendar.MONTH);
    String smonth = AndroidHelper.getMonthString(month);
    final String ss_month = String.format(Locale.US, "%02d", month + 1);
    final String ss_maxDay = String.format(Locale.US, "%02d", mApp.getCurrentDate().getActualMaximum(Calendar.DAY_OF_MONTH));
    smonth += "\n01/" + ss_month + " - " + ss_maxDay + "/" + ss_month;
    final String s = smonth;
    mActivity.runOnUiThread(() -> {
      mTvMonth.setText(s);
      mTvYear.setText(String.valueOf(mApp.getCurrentDate().get(Calendar.YEAR)));
      mTvYear.setText(String.valueOf(mApp.getCurrentDate().get(Calendar.YEAR)));
    });
  }

  /**
   * Returns 5% of the total visible items.
   * @return int
   */
  private int at5Percent() {
    return (int)(mLvAdapter.getCount()*(5.0f/100.0f));
  }

  /**
   * Updates the dates.
   */
  private void updateDates() {
    mActivity.runOnUiThread(() -> mLvAdapter.clear());
    int minDay = 1;
    int maxDay = mApp.getCurrentDate().getActualMaximum(Calendar.DAY_OF_MONTH);
    List<DayEntry> wDays = new ArrayList<>();
    int index = 0;
    double realwDays = 0.0;
    int currentDay = mApp.getCurrentDate().get(Calendar.DAY_OF_MONTH);
    /* get first week */
    mApp.getCurrentDate().set(Calendar.DAY_OF_MONTH, 1);
    int firstWeek = mApp.getCurrentDate().get(Calendar.WEEK_OF_YEAR);
    WorkTimeDay wtdTotalWorkTime = new WorkTimeDay();
    WorkTimeDay wtdnow = WorkTimeDay.now();
    /* loop for each days in the month */
    for(int day = minDay; day <= maxDay; ++day) {
      mApp.getCurrentDate().set(Calendar.DAY_OF_MONTH, day);
      DayEntry de = new DayEntry(getActivity(), mApp.getCurrentDate(), DayType.ERROR, DayType.ERROR);
      de.setAmountByHour(mApp.getAmountByHour()); /* set default amount */
      /* Force public holiday */
      if(mApp.getPublicHolidaysFactory().isPublicHolidays(de.getDay())) {
        de.setTypeMorning(DayType.PUBLIC_HOLIDAY);
        de.setTypeAfternoon(DayType.PUBLIC_HOLIDAY);
      }
      int now = mApp.getCurrentDate().get(Calendar.DAY_OF_WEEK);
      /* reload data if the current day is already inserted */
      mApp.getDaysFactory().checkForDayDateAndCopy(de);
      /* count working day */
      if(now != Calendar.SUNDAY && now != Calendar.SATURDAY && !mApp.getPublicHolidaysFactory().isPublicHolidays(de.getDay())) {
        wDays.add(de);
        if(de.getTypeMorning() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.RECOVERY) realwDays += 0.5;
        if(de.getTypeAfternoon() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.RECOVERY) realwDays += 0.5;
      }
      mActivity.runOnUiThread(() -> mLvAdapter.add(de));
      if(mApp.isScrollToCurrentDay() && mApp.getLastFirstVisibleItem() == 0 && de.getDay().dateString().equals(wtdnow.dateString())) {
        mApp.setLastFirstVisibleItem(index);
      }
      else if(mApp.getLastFirstVisibleItem() == 0) index++;
    }
    Map<String, DayEntry> map = mApp.getDaysFactory().toDaysMap();
    int min = (firstWeek == 52 ? 1 : firstWeek);
    for(int w = min; w <= min + 6; ++w) {
      WorkTimeDay wtdWorkTimeFromWeek =  mApp.getDaysFactory().getWorkTimeDayFromWeek(map, w, mApp.getCurrentDate().get(Calendar.MONTH), mApp.getCurrentDate().get(Calendar.YEAR));
      if(wtdWorkTimeFromWeek.isValidTime())
        wtdTotalWorkTime.addTime(wtdWorkTimeFromWeek);
    }
    /* reload work day label */
    mApp.getCurrentDate().set(Calendar.DAY_OF_MONTH, currentDay);
    int n = Integer.parseInt((""+realwDays).split("\\.")[1]);
    String workDays = getString(R.string.work_days) + ": ";
    if(n != 0)
      workDays += String.format(Locale.US, "%02d.%02d/%02d", (int)realwDays, n, wDays.size());
    else
      workDays += String.format(Locale.US, "%02d/%02d", (int)realwDays, wDays.size());
    workDays += " " + getString(R.string.days_lower_case);
    final String s_workDays = workDays;
    mActivity.runOnUiThread(() -> mTvWorkDays.setText(s_workDays));
    /* substract legal working time */
    WorkTimeDay wtdEstimatedMonthlyHours = mApp.getEstimatedHours(wDays);
    final String monthlyHours = getString(R.string.monthly_hours) + ": " +
      String.format(Locale.US, "%d:%02d/%d:%02d",
        wtdTotalWorkTime.getHours(), wtdTotalWorkTime.getMinutes(),
        wtdEstimatedMonthlyHours.getHours(), wtdEstimatedMonthlyHours.getMinutes());


    mActivity.runOnUiThread(() -> {
      mTvMonthlyHours.setText(monthlyHours);
      mLvAdapter.notifyDataSetChanged();
    /* restores the scroll position and reloads the adapter else the listview seems not agree with the call of setSelection */
      mDays.setAdapter(mDays.getAdapter());
      mDays.setSelection(mApp.getLastFirstVisibleItem());
      if (mApp.isScrollToCurrentDay()) {
        /* change the visibility if 5% of the list is displayed or hidden */
        int k = at5Percent();
        if ((mApp.getLastFirstVisibleItem() == 0 || mApp.getLastFirstVisibleItem() < k) && mRlDetails.getVisibility() != View.VISIBLE)
          mRlDetails.setVisibility(View.VISIBLE);
        else if (mApp.getLastFirstVisibleItem() > k && mRlDetails.getVisibility() == View.VISIBLE)
          mRlDetails.setVisibility(View.GONE);
      }
    });
  }


  /**
   * Called when the user sweeps from left to right.
   */
  public void leftToRightSwipe() {
    mBtNextMonth.callOnClick();
  }

  /**
   * Called when the user sweeps from right to left.
   */
  public void rightToLeftSwipe() {
    mBtPreviousMonth.callOnClick();
  }

}
