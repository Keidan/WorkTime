package fr.ralala.worktime.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.services.DropboxAutoExportService;
import fr.ralala.worktime.services.QuickAccessService;
import fr.ralala.worktime.utils.AndroidHelper;


import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.ui.widgets.DayWidgetProvider1x1;
import fr.ralala.worktime.ui.widgets.DayWidgetProvider4x1;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the activity containing the day entries used for the profiles and the classic insertions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class DayActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{
  public static final String DAY_ACTIVITY_EXTRA_DATE = "DAY_ACTIVITY_EXTRA_DATE_date";
  public static final String DAY_ACTIVITY_EXTRA_PROFILE = "DAY_ACTIVITY_EXTRA_DATE_profile";
  private Spinner mSpProfile = null;
  private Spinner mSpTypeMorning = null;
  private Spinner mSpTypeAfternoon = null;
  private TextView mTvStartMorning = null;
  private TextView mTvEndMorning = null;
  private TextView mTvStartAfternoon = null;
  private TextView mTvLegalWorktime = null;
  private TextView mTvEndAfternoon = null;
  private TextView mTvAdditionalBreak = null;
  private EditText mEtAmount = null;
  private EditText mEtName = null;
  private DayEntry mDe = null;
  private boolean mDisplayProfile = false;
  private ArrayAdapter<String> mSpProfilesAdapter = null;
  private FloatingActionButton mFab = null;

  private WorkTimeDay mWtdStartMorning = null;
  private WorkTimeDay mWtdEndMorning = null;
  private WorkTimeDay mWtdStartAfternoon = null;
  private WorkTimeDay mWtdEndAfternoon = null;
  private WorkTimeDay mWtdAdditionalBreak = null;
  private WorkTimeDay mWtdLegalWorktime = null;
  private MainApplication mApp = null;
  private DayEntry mSelectedProfile = null;
  private boolean mOpenForAdd = false;

  private boolean mFromClear = false;
  private boolean mFromWidget = false;

  /**
   * Starts an activity.
   * @param ctx The Android context.
   * @param date The date used for the extra part.
   * @param profile True from a profile.
   */
  public static void startActivity(final Context ctx, final String date, final boolean profile) {
    Intent intent = new Intent(ctx, DayActivity.class);
    intent.putExtra(DAY_ACTIVITY_EXTRA_DATE, date);
    intent.putExtra(DAY_ACTIVITY_EXTRA_PROFILE, profile);
    ctx.startActivity(intent);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    super.onBackPressed();
    UIHelper.closeAnimation(this);
    clearFromWidget();
  }

  /**
   * Clear is required from a widget.
   */
  private void clearFromWidget() {
    if(mFromWidget) {
      mApp.setLastWidgetOpen(System.currentTimeMillis());
      if(mApp.isExportAutoSave())
        startService(new Intent(this, DropboxAutoExportService.class));
      else
        finish();
    }
  }

  /**
   * Called when the activity is paused.
   */
  @Override
  public void onPause() {
    super.onPause();
    clearFromWidget();
  }

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UIHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    mApp = MainApplication.getApp(this);
    setContentView(R.layout.activity_day);
    String date =  null;
    boolean show = false;
    if(getIntent().getAction() == null || (!getIntent().getAction().equals(DayWidgetProvider1x1.ACTION_FROM_WIDGET) && !getIntent().getAction().equals(DayWidgetProvider4x1.ACTION_FROM_WIDGET))) {
      if(getIntent().getExtras() != null) {
        Bundle extras = getIntent().getExtras();
        date = extras.getString(DAY_ACTIVITY_EXTRA_DATE);
        mDisplayProfile = extras.getBoolean(DAY_ACTIVITY_EXTRA_PROFILE);
        if(date == null || date.equals("null")) date = "";
      }
      show = true;
    } else if(getIntent().getAction().equals(DayWidgetProvider1x1.ACTION_FROM_WIDGET) || getIntent().getAction().equals(DayWidgetProvider4x1.ACTION_FROM_WIDGET)) {
      /* If the main activity is not started, all contexts are reset, so we need to reload the required contexts */
      if(!mApp.openSql(this)) {
        UIHelper.toast(this, R.string.error_widget_sql);
        Log.e(getClass().getSimpleName(), "Widger error SQL");
        finish();
        return ;
      }
      if(mApp.isExportAutoSave())
        mApp.initOnLoadTables();
      date = mApp.getDaysFactory().getCurrentDay().getDay().dateString();
      mDisplayProfile = true;
      mFromWidget = true;
    }

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(show);
      actionBar.setDisplayHomeAsUpEnabled(show);
    }
    List<DayEntry> days = mDisplayProfile ? mApp.getDaysFactory().list() : mApp.getProfilesFactory().list();
    for(DayEntry de : days) {
      if((mDisplayProfile && de.getDay().dateString().equals(date)) || (!mDisplayProfile && de.getName().equals(date))) {
        if(mFromWidget && !de.getWorkTime().isValidTime())
          this.mDe = null;
        else
          this.mDe = de;
        break;
      }
    }
    if(mDe == null) {
      mOpenForAdd = true;
      mDe = new DayEntry(this, WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
      if(date != null && !date.isEmpty() && date.contains("/"))
        mDe.setDay(date);
    }
    mFab = findViewById(R.id.fab);
    if(mFab != null)
      mFab.setOnClickListener(this);
    refreshStartEndPause(mDe);

    if(mDisplayProfile)
      setTitle(date);
    TextView tvProfile = findViewById(R.id.tvProfile);
    mSpProfile = findViewById(R.id.spProfile);
    mSpTypeMorning = findViewById(R.id.spTypeMorning);
    mSpTypeAfternoon = findViewById(R.id.spTypeAfternoon);
    mTvStartMorning = findViewById(R.id.tvStartMorning);
    mTvEndMorning = findViewById(R.id.tvEndMorning);
    mTvStartAfternoon = findViewById(R.id.tvStartAfternoon);
    mTvEndAfternoon = findViewById(R.id.tvEndAfternoon);
    mTvAdditionalBreak = findViewById(R.id.tvAdditionalBreak);
    mTvLegalWorktime = findViewById(R.id.tvLegalWorktime);
    mEtAmount = findViewById(R.id.etAmount);
    TextView tvName = findViewById(R.id.tvName);
    mEtName = findViewById(R.id.etName);

    boolean hw = mApp.isHideWage();
    mEtAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    View tvLblAmount = findViewById(R.id.tvLblAmount);
    if(tvLblAmount != null)
      tvLblAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    /* add click listener for the time picker */
    mTvStartMorning.setOnClickListener(this);
    mTvEndMorning.setOnClickListener(this);
    mTvStartAfternoon.setOnClickListener(this);
    mTvEndAfternoon.setOnClickListener(this);
    mTvAdditionalBreak.setOnClickListener(this);
    mTvLegalWorktime.setOnClickListener(this);
    /* manage view for the call from the profile view */
    if(mDisplayProfile) {
      int v = View.VISIBLE;
      mSpProfile.setVisibility(v);
      if(tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.GONE;
      if(tvName != null)
        tvName.setVisibility(v);
      mEtName.setVisibility(v);
      if(mApp.getProfilesFactory().list().isEmpty()) {
        mSpProfile.setVisibility(View.GONE);
        if(tvProfile != null)
          tvProfile.setVisibility(View.GONE);
      }
    } else {
      int v = View.GONE;
      mSpProfile.setVisibility(v);
      if(tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.VISIBLE;
      if(tvName != null)
        tvName.setVisibility(v);
      mEtName.setVisibility(v);
      mEtName.setText(mDe.getName());
    }
    /* build type spinner */
    final ArrayAdapter<String> spTypeAdapterMorning = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    spTypeAdapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpTypeMorning.setAdapter(spTypeAdapterMorning);
    spTypeAdapterMorning.add("");
    spTypeAdapterMorning.add(DayType.getText(this, DayType.AT_WORK));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.HOLIDAY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.PUBLIC_HOLIDAY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.SICKNESS));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.UNPAID));
    final ArrayAdapter<String> spTypeAdapterAfternoon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    spTypeAdapterAfternoon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpTypeAfternoon.setAdapter(spTypeAdapterAfternoon);
    spTypeAdapterAfternoon.add("");
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.AT_WORK));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.HOLIDAY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.PUBLIC_HOLIDAY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.SICKNESS));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.UNPAID));
    mSpTypeMorning.setSelection(mDe.getTypeMorning() == DayType.ERROR ? 0 : mDe.getTypeMorning().value() + 1);
    mSpTypeAfternoon.setSelection(mDe.getTypeAfternoon() == DayType.ERROR ? 0 : mDe.getTypeAfternoon().value() + 1);

    if(mDisplayProfile) {
    /* build profiles spinner */
      mSpProfilesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
      mSpProfilesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      mSpProfile.setAdapter(mSpProfilesAdapter);
      mSpProfilesAdapter.add("");
      for (DayEntry profile : mApp.getProfilesFactory().list())
        mSpProfilesAdapter.add(profile.getName());
      mSpProfile.setOnItemSelectedListener(this);
    }

    mTvStartMorning.setText(mDe.getStartMorning().timeString());
    mTvEndMorning.setText(mDe.getEndMorning().timeString());
    mTvStartAfternoon.setText(mDe.getStartAfternoon().timeString());
    mTvEndAfternoon.setText(mDe.getEndAfternoon().timeString());
    mTvAdditionalBreak.setText(mDe.getAdditionalBreak().timeString());
    mTvLegalWorktime.setText(mDe.getLegalWorktime().timeString());

    if(!mApp.isHideWage()) {
      mEtAmount.setTypeface(mTvStartAfternoon.getTypeface());
      mEtAmount.setTextSize(14);
      mEtAmount.setTextColor(mTvStartAfternoon.getTextColors());
      mEtAmount.setText(String.format(Locale.US, "%.02f",
        mDe.getAmountByHour() != 0 ? mDe.getAmountByHour() : mApp.getAmountByHour()).replaceAll(",", "."));
    }
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    mFromClear = false;
    mSelectedProfile = null;
    if(mTvLegalWorktime.getText().equals(getString(R.string.default_time))) {
      String t = mApp.getLegalWorkTimeByDay().timeString();
      mTvLegalWorktime.setText(t);
      mDe.setLegalWorktime(t);
    }
    if(mDisplayProfile && mOpenForAdd) {
      DayEntry de = mApp.getProfilesFactory().getHighestLearningWeight();
      if(de == null)
        mSpProfile.setSelection(0);
      else {
        for (int i = 0; i < mSpProfilesAdapter.getCount(); ++i) {
          String s = mSpProfilesAdapter.getItem(i);
          if (s != null && s.equals(de.getName())) {
            mSpProfile.setSelection(i);
            selectProfile(i);
            break;
          }
        }
      }
    }
  }

  /**
   * Called when the options menu is clicked.
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_day, menu);

    MenuItem action_cancel = menu.findItem(R.id.action_cancel);
    action_cancel.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    action_cancel.setTitle(!mDisplayProfile ? R.string.cancel : R.string.clear);
    return true;
  }

  /**
   * Called when the options item is clicked (home and cancel).
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_cancel:
        if(mDisplayProfile) {
          mFromClear = true;

          mTvStartMorning.setText((mWtdStartMorning = new WorkTimeDay()).timeString());
          mTvEndMorning.setText((mWtdEndMorning = new WorkTimeDay()).timeString());
          mTvStartAfternoon.setText((mWtdStartAfternoon = new WorkTimeDay()).timeString());
          mTvEndAfternoon.setText((mWtdEndAfternoon = new WorkTimeDay()).timeString());
          mTvAdditionalBreak.setText((mWtdAdditionalBreak = new WorkTimeDay()).timeString());
          mTvLegalWorktime.setText((mWtdLegalWorktime = mApp.getLegalWorkTimeByDay()).timeString());
          mEtAmount.setText(getString(R.string.zero));
          mSpTypeMorning.setSelection(DayType.AT_WORK.value());
          mSpTypeAfternoon.setSelection(DayType.AT_WORK.value());
          mEtName.setText("");
          setTitle(mDe.getDay().dateString());
          mSpProfile.setSelection(0);
        } else
          onBackPressed();
        return true;
    }
    return false;
  }

  /**
   * Called when a button is clicked (mFab, mTvStartMorning, mTvEndMorning, mTvStartAfternoon, mTvEndAfternoon, mTvAdditionalBreak and mTvLegalWorktime).
   * @param v The view clicked.
   */
  public void onClick(final View v) {
    if(v.equals(mFab)) {
      DayEntry newEntry = new DayEntry(this, mDe.getDay().toCalendar(),
        DayType.compute(this, mSpTypeMorning.getSelectedItem().toString()),
        DayType.compute(this, mSpTypeAfternoon.getSelectedItem().toString()));
      String s = mEtAmount.getText().toString().trim();
      if (s.equals(getString(R.string.zero))) s = "";
      newEntry.setAmountByHour(s.isEmpty() ? mApp.getAmountByHour() : Double.parseDouble(s));
      newEntry.setName(mEtName.getText().toString());
      newEntry.setEndMorning(mTvEndMorning.getText().toString());
      newEntry.setStartMorning(mTvStartMorning.getText().toString());
      newEntry.setEndAfternoon(mTvEndAfternoon.getText().toString());
      newEntry.setAdditionalBreak(mTvAdditionalBreak.getText().toString());
      newEntry.setStartAfternoon(mTvStartAfternoon.getText().toString());
      newEntry.setLegalWorktime(mTvLegalWorktime.getText().toString());
      if(newEntry.getTypeMorning() != DayType.ERROR) {
        if (newEntry.getStartMorning().getHours() == 0) {
          UIHelper.shakeError(mTvStartMorning, getString(R.string.error_invalid_start));
          return;
        } else if (newEntry.getEndMorning().getHours() == 0) {
          UIHelper.shakeError(mTvEndMorning, getString(R.string.error_invalid_end));
          return;
        } else if (mTvStartMorning.getText().toString().equals(mTvEndMorning.getText().toString())) {
          UIHelper.shakeError(mTvStartMorning, getString(R.string.error_invalid_start_end_morning));
          UIHelper.shakeError(mTvEndMorning, getString(R.string.error_invalid_start_end_morning));
          return;
        }
      } else if(newEntry.getTypeAfternoon() != DayType.ERROR) {
        if (newEntry.getStartAfternoon().getHours() == 0) {
          UIHelper.shakeError(mTvStartAfternoon, getString(R.string.error_invalid_start));
          return;
        } else if (newEntry.getEndAfternoon().getHours() == 0) {
          UIHelper.shakeError(mTvEndAfternoon, getString(R.string.error_invalid_end));
          return;
        } else if (mTvStartAfternoon.getText().toString().equals(mTvEndAfternoon.getText().toString())) {
          UIHelper.shakeError(mTvStartAfternoon, getString(R.string.error_invalid_start_end_morning));
          UIHelper.shakeError(mTvEndAfternoon, getString(R.string.error_invalid_start_end_morning));
          return;
        }
      }

      if (mTvLegalWorktime.getText().toString().equals(getString(R.string.default_time))) {
        UIHelper.shakeError(mTvLegalWorktime, getString(R.string.error_invalid_legal_worktime));
        return;
      }
      if(mDisplayProfile) {
        boolean match = mDe.match(newEntry);
        if (!match) {
          mApp.getDaysFactory().remove(mDe);
          mApp.getProfilesFactory().updateProfilesLearningWeight(mSelectedProfile, mApp.getProfilesWeightDepth(), mFromClear);
          mFromClear = false;
          if (newEntry.getStartMorning().isValidTime() || newEntry.getEndAfternoon().isValidTime())
            mApp.getDaysFactory().add(newEntry);
          if(AndroidHelper.isServiceRunning(this, QuickAccessService.class))
            stopService(new Intent(this, QuickAccessService.class));
          AndroidHelper.updateWidget(this, DayWidgetProvider1x1.class);
          AndroidHelper.updateWidget(this, DayWidgetProvider4x1.class);
        }
      } else {
        if (mEtName.getText().toString().isEmpty()) {
          UIHelper.shakeError(mEtName, getString(R.string.error_no_name));
          return;
        }

        if(mDe.getName().isEmpty() || !mDe.match(newEntry)) {
          mApp.getProfilesFactory().remove(mDe);
          if (newEntry.getStartMorning().isValidTime() || newEntry.getEndAfternoon().isValidTime()) {
            newEntry.setLearningWeight(mDe.getLearningWeight());
            mApp.getProfilesFactory().add(newEntry);
          }
        }
      }
      finish();
    } else if(v.equals(mTvStartMorning))
      UIHelper.openTimePicker(this, mWtdStartMorning, mTvStartMorning);
    else if(v.equals(mTvEndMorning))
      UIHelper.openTimePicker(this, mWtdEndMorning, mTvEndMorning);
    else if(v.equals(mTvStartAfternoon))
      UIHelper.openTimePicker(this, mWtdStartAfternoon, mTvStartAfternoon);
    else if(v.equals(mTvEndAfternoon))
      UIHelper.openTimePicker(this, mWtdEndAfternoon, mTvEndAfternoon);
    else if(v.equals(mTvAdditionalBreak))
      UIHelper.openTimePicker(this, mWtdAdditionalBreak, mTvAdditionalBreak);
    else if(v.equals(mTvLegalWorktime))
      UIHelper.openTimePicker(this, mWtdLegalWorktime, mTvLegalWorktime);
  }

  /**
   * Called when an item is selected.
   * @param adapterView The adapter view.
   * @param view The selected view.
   * @param i The view index in the adapter.
   * @param l Not used.
   */
  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    selectProfile(i);
  }

  /**
   * Called when a profile is selected.
   * @param indexInAdapter The index in the adapter.
   */
  private void selectProfile(int indexInAdapter) {
    if(mSpProfilesAdapter != null) {
      String name = mSpProfilesAdapter.getItem(indexInAdapter);
      if(name != null && !name.isEmpty()) {
        DayEntry de = mApp.getProfilesFactory().getByName(name);
        if (de != null) {
          mTvStartMorning.setText(de.getStartMorning().timeString());
          mTvEndMorning.setText(de.getEndMorning().timeString());
          mTvStartAfternoon.setText(de.getStartAfternoon().timeString());
          mTvEndAfternoon.setText(de.getEndAfternoon().timeString());
          mTvAdditionalBreak.setText(de.getAdditionalBreak().timeString());
          mTvLegalWorktime.setText(de.getLegalWorktime().timeString());
          mEtAmount.setText(String.format(Locale.US, "%.02f", de.getAmountByHour()).replaceAll(",", "."));
          mSpTypeMorning.setSelection(de.getTypeMorning() == DayType.ERROR ? 0 : de.getTypeMorning().value() + 1);
          mSpTypeAfternoon.setSelection(de.getTypeAfternoon() == DayType.ERROR ? 0 : de.getTypeAfternoon().value() + 1);
          mEtName.setText(de.getName());
          refreshStartEndPause(de);
          mSelectedProfile = de;
        }
      } else
        mSelectedProfile = null;
    }
  }

  /**
   * Refresh the internal components.
   * @param de The current day entry.
   */
  private void refreshStartEndPause(DayEntry de) {
    mWtdStartMorning = de.getStartMorning().clone();
    mWtdEndMorning = de.getEndMorning().clone();
    mWtdStartAfternoon = de.getStartAfternoon().clone();
    mWtdEndAfternoon = de.getEndAfternoon().clone();
    mWtdAdditionalBreak = de.getAdditionalBreak().clone();
    if(de.getLegalWorktime().timeString().equals(getString(R.string.default_time)))
      de.setLegalWorktime(mApp.getLegalWorkTimeByDay().timeString());
    mWtdLegalWorktime = de.getLegalWorktime().clone();
  }

  /**
   * See official javadoc.
   * @param adapterView See official javadoc.
   */
  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }
}
