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
  private Spinner spProfile = null;
  private Spinner spTypeMorning = null;
  private Spinner spTypeAfternoon = null;
  private TextView tvStartMorning = null;
  private TextView tvEndMorning = null;
  private TextView tvStartAfternoon = null;
  private TextView tvLegalWorktime = null;
  private TextView tvEndAfternoon = null;
  private TextView tvAdditionalBreak = null;
  private EditText etAmount = null;
  private EditText etName = null;
  private DayEntry de = null;
  private boolean displayProfile = false;
  private ArrayAdapter<String> spProfilesAdapter = null;
  private FloatingActionButton fab = null;

  private WorkTimeDay wtdStartMorning = null;
  private WorkTimeDay wtdEndMorning = null;
  private WorkTimeDay wtdStartAfternoon = null;
  private WorkTimeDay wtdEndAfternoon = null;
  private WorkTimeDay wtdAdditionalBreak = null;
  private WorkTimeDay wtdLegalWorktime = null;
  private MainApplication app = null;
  private DayEntry selectedProfile = null;
  private boolean openForAdd = false;

  private boolean fromClear = false;
  private boolean fromWidget = false;

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
    if(fromWidget) {
      app.setLastWidgetOpen(System.currentTimeMillis());
      if(app.isExportAutoSave())
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
    app = MainApplication.getApp(this);
    setContentView(R.layout.activity_day);
    String date =  null;
    boolean show = false;
    if(getIntent().getAction() == null || (!getIntent().getAction().equals(DayWidgetProvider1x1.ACTION_FROM_WIDGET) && !getIntent().getAction().equals(DayWidgetProvider4x1.ACTION_FROM_WIDGET))) {
      if(getIntent().getExtras() != null) {
        Bundle extras = getIntent().getExtras();
        date = extras.getString(DAY_ACTIVITY_EXTRA_DATE);
        displayProfile = extras.getBoolean(DAY_ACTIVITY_EXTRA_PROFILE);
        if(date == null || date.equals("null")) date = "";
      }
      show = true;
    } else if(getIntent().getAction().equals(DayWidgetProvider1x1.ACTION_FROM_WIDGET) || getIntent().getAction().equals(DayWidgetProvider4x1.ACTION_FROM_WIDGET)) {
      /* If the main activity is not started, all contexts are reset, so we need to reload the required contexts */
      if(!app.openSql(this)) {
        UIHelper.toast(this, R.string.error_widget_sql);
        Log.e(getClass().getSimpleName(), "Widger error SQL");
        finish();
        return ;
      }
      if(app.isExportAutoSave())
        app.initOnLoadTables();
      date = app.getDaysFactory().getCurrentDay().getDay().dateString();
      displayProfile = true;
      fromWidget = true;
    }

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(show);
      actionBar.setDisplayHomeAsUpEnabled(show);
    }
    List<DayEntry> days = displayProfile ? app.getDaysFactory().list() : app.getProfilesFactory().list();
    for(DayEntry de : days) {
      if((displayProfile && de.getDay().dateString().equals(date)) || (!displayProfile && de.getName().equals(date))) {
        if(fromWidget && !de.getWorkTime().isValidTime())
          this.de = null;
        else
          this.de = de;
        break;
      }
    }
    if(de == null) {
      openForAdd = true;
      de = new DayEntry(this, WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
      if(date != null && !date.isEmpty() && date.contains("/"))
        de.setDay(date);
    }
    fab = findViewById(R.id.fab);
    if(fab != null)
      fab.setOnClickListener(this);
    refreshStartEndPause(de);

    if(displayProfile)
      setTitle(date);
    TextView tvProfile = findViewById(R.id.tvProfile);
    spProfile = findViewById(R.id.spProfile);
    spTypeMorning = findViewById(R.id.spTypeMorning);
    spTypeAfternoon = findViewById(R.id.spTypeAfternoon);
    tvStartMorning = findViewById(R.id.tvStartMorning);
    tvEndMorning = findViewById(R.id.tvEndMorning);
    tvStartAfternoon = findViewById(R.id.tvStartAfternoon);
    tvEndAfternoon = findViewById(R.id.tvEndAfternoon);
    tvAdditionalBreak = findViewById(R.id.tvAdditionalBreak);
    tvLegalWorktime = findViewById(R.id.tvLegalWorktime);
    etAmount = findViewById(R.id.etAmount);
    TextView tvName = findViewById(R.id.tvName);
    etName = findViewById(R.id.etName);

    boolean hw = app.isHideWage();
    etAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    View tvLblAmount = findViewById(R.id.tvLblAmount);
    if(tvLblAmount != null)
      tvLblAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    /* add click listener for the time picker */
    tvStartMorning.setOnClickListener(this);
    tvEndMorning.setOnClickListener(this);
    tvStartAfternoon.setOnClickListener(this);
    tvEndAfternoon.setOnClickListener(this);
    tvAdditionalBreak.setOnClickListener(this);
    tvLegalWorktime.setOnClickListener(this);
    /* manage view for the call from the profile view */
    if(displayProfile) {
      int v = View.VISIBLE;
      spProfile.setVisibility(v);
      if(tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.GONE;
      if(tvName != null)
        tvName.setVisibility(v);
      etName.setVisibility(v);
      if(app.getProfilesFactory().list().isEmpty()) {
        spProfile.setVisibility(View.GONE);
        if(tvProfile != null)
          tvProfile.setVisibility(View.GONE);
      }
    } else {
      int v = View.GONE;
      spProfile.setVisibility(v);
      if(tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.VISIBLE;
      if(tvName != null)
        tvName.setVisibility(v);
      etName.setVisibility(v);
      etName.setText(de.getName());
    }
    /* build type spinner */
    final ArrayAdapter<String> spTypeAdapterMorning = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    spTypeAdapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spTypeMorning.setAdapter(spTypeAdapterMorning);
    spTypeAdapterMorning.add("");
    spTypeAdapterMorning.add(DayType.getText(this, DayType.AT_WORK));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.HOLIDAY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.PUBLIC_HOLIDAY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.SICKNESS));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.UNPAID));
    final ArrayAdapter<String> spTypeAdapterAfternoon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    spTypeAdapterAfternoon.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spTypeAfternoon.setAdapter(spTypeAdapterAfternoon);
    spTypeAdapterAfternoon.add("");
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.AT_WORK));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.HOLIDAY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.PUBLIC_HOLIDAY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.SICKNESS));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.UNPAID));
    spTypeMorning.setSelection(de.getTypeMorning() == DayType.ERROR ? 0 : de.getTypeMorning().value() + 1);
    spTypeAfternoon.setSelection(de.getTypeAfternoon() == DayType.ERROR ? 0 : de.getTypeAfternoon().value() + 1);

    if(displayProfile) {
    /* build profiles spinner */
      spProfilesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
      spProfilesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spProfile.setAdapter(spProfilesAdapter);
      spProfilesAdapter.add("");
      for (DayEntry profile : app.getProfilesFactory().list())
        spProfilesAdapter.add(profile.getName());
      spProfile.setOnItemSelectedListener(this);
    }

    tvStartMorning.setText(de.getStartMorning().timeString());
    tvEndMorning.setText(de.getEndMorning().timeString());
    tvStartAfternoon.setText(de.getStartAfternoon().timeString());
    tvEndAfternoon.setText(de.getEndAfternoon().timeString());
    tvAdditionalBreak.setText(de.getAdditionalBreak().timeString());
    tvLegalWorktime.setText(de.getLegalWorktime().timeString());

    if(!app.isHideWage()) {
      etAmount.setTypeface(tvStartAfternoon.getTypeface());
      etAmount.setTextSize(14);
      etAmount.setTextColor(tvStartAfternoon.getTextColors());
      etAmount.setText(String.format(Locale.US, "%.02f",
        de.getAmountByHour() != 0 ? de.getAmountByHour() : app.getAmountByHour()).replaceAll(",", "."));
    }
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    fromClear = false;
    selectedProfile = null;
    if(tvLegalWorktime.getText().equals(getString(R.string.default_time))) {
      String t = app.getLegalWorkTimeByDay().timeString();
      tvLegalWorktime.setText(t);
      de.setLegalWorktime(t);
    }
    if(displayProfile && openForAdd) {
      DayEntry de = app.getProfilesFactory().getHighestLearningWeight();
      if(de == null)
        spProfile.setSelection(0);
      else {
        for (int i = 0; i < spProfilesAdapter.getCount(); ++i) {
          String s = spProfilesAdapter.getItem(i);
          if (s != null && s.equals(de.getName())) {
            spProfile.setSelection(i);
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
    action_cancel.setTitle(!displayProfile ? R.string.cancel : R.string.clear);
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
        if(displayProfile) {
          fromClear = true;

          tvStartMorning.setText((wtdStartMorning = new WorkTimeDay()).timeString());
          tvEndMorning.setText((wtdEndMorning = new WorkTimeDay()).timeString());
          tvStartAfternoon.setText((wtdStartAfternoon = new WorkTimeDay()).timeString());
          tvEndAfternoon.setText((wtdEndAfternoon = new WorkTimeDay()).timeString());
          tvAdditionalBreak.setText((wtdAdditionalBreak = new WorkTimeDay()).timeString());
          tvLegalWorktime.setText((wtdLegalWorktime = app.getLegalWorkTimeByDay()).timeString());
          etAmount.setText(getString(R.string.zero));
          spTypeMorning.setSelection(DayType.AT_WORK.value());
          spTypeAfternoon.setSelection(DayType.AT_WORK.value());
          etName.setText("");
          setTitle(de.getDay().dateString());
          spProfile.setSelection(0);
        } else
          onBackPressed();
        return true;
    }
    return false;
  }

  /**
   * Called when a button is clicked (fab, tvStartMorning, tvEndMorning, tvStartAfternoon, tvEndAfternoon, tvAdditionalBreak and tvLegalWorktime).
   * @param v The view clicked.
   */
  public void onClick(final View v) {
    if(v.equals(fab)) {
      DayEntry newEntry = new DayEntry(this, de.getDay().toCalendar(),
        DayType.compute(this, spTypeMorning.getSelectedItem().toString()),
        DayType.compute(this, spTypeAfternoon.getSelectedItem().toString()));
      String s = etAmount.getText().toString().trim();
      if (s.equals(getString(R.string.zero))) s = "";
      newEntry.setAmountByHour(s.isEmpty() ? app.getAmountByHour() : Double.parseDouble(s));
      newEntry.setName(etName.getText().toString());
      newEntry.setEndMorning(tvEndMorning.getText().toString());
      newEntry.setStartMorning(tvStartMorning.getText().toString());
      newEntry.setEndAfternoon(tvEndAfternoon.getText().toString());
      newEntry.setAdditionalBreak(tvAdditionalBreak.getText().toString());
      newEntry.setStartAfternoon(tvStartAfternoon.getText().toString());
      newEntry.setLegalWorktime(tvLegalWorktime.getText().toString());
      if(newEntry.getTypeMorning() != DayType.ERROR) {
        if (newEntry.getStartMorning().getHours() == 0) {
          UIHelper.shakeError(tvStartMorning, getString(R.string.error_invalid_start));
          return;
        } else if (newEntry.getEndMorning().getHours() == 0) {
          UIHelper.shakeError(tvEndMorning, getString(R.string.error_invalid_end));
          return;
        } else if (tvStartMorning.getText().toString().equals(tvEndMorning.getText().toString())) {
          UIHelper.shakeError(tvStartMorning, getString(R.string.error_invalid_start_end_morning));
          UIHelper.shakeError(tvEndMorning, getString(R.string.error_invalid_start_end_morning));
          return;
        }
      } else if(newEntry.getTypeAfternoon() != DayType.ERROR) {
        if (newEntry.getStartAfternoon().getHours() == 0) {
          UIHelper.shakeError(tvStartAfternoon, getString(R.string.error_invalid_start));
          return;
        } else if (newEntry.getEndAfternoon().getHours() == 0) {
          UIHelper.shakeError(tvEndAfternoon, getString(R.string.error_invalid_end));
          return;
        } else if (tvStartAfternoon.getText().toString().equals(tvEndAfternoon.getText().toString())) {
          UIHelper.shakeError(tvStartAfternoon, getString(R.string.error_invalid_start_end_morning));
          UIHelper.shakeError(tvEndAfternoon, getString(R.string.error_invalid_start_end_morning));
          return;
        }
      }

      if (tvLegalWorktime.getText().toString().equals(getString(R.string.default_time))) {
        UIHelper.shakeError(tvLegalWorktime, getString(R.string.error_invalid_legal_worktime));
        return;
      }
      if(displayProfile) {
        boolean match = de.match(newEntry);
        if (!match) {
          app.getDaysFactory().remove(de);
          app.getProfilesFactory().updateProfilesLearningWeight(selectedProfile, app.getProfilesWeightDepth(), fromClear);
          fromClear = false;
          if (newEntry.getStartMorning().isValidTime() || newEntry.getEndAfternoon().isValidTime())
            app.getDaysFactory().add(newEntry);
          if(AndroidHelper.isServiceRunning(this, QuickAccessService.class))
            stopService(new Intent(this, QuickAccessService.class));
          AndroidHelper.updateWidget(this, DayWidgetProvider1x1.class);
          AndroidHelper.updateWidget(this, DayWidgetProvider4x1.class);
        }
      } else {
        if (etName.getText().toString().isEmpty()) {
          UIHelper.shakeError(etName, getString(R.string.error_no_name));
          return;
        }

        if(de.getName().isEmpty() || !de.match(newEntry)) {
          app.getProfilesFactory().remove(de);
          if (newEntry.getStartMorning().isValidTime() || newEntry.getEndAfternoon().isValidTime()) {
            newEntry.setLearningWeight(de.getLearningWeight());
            app.getProfilesFactory().add(newEntry);
          }
        }
      }
      finish();
    } else if(v.equals(tvStartMorning))
      UIHelper.openTimePicker(this, wtdStartMorning, tvStartMorning);
    else if(v.equals(tvEndMorning))
      UIHelper.openTimePicker(this, wtdEndMorning, tvEndMorning);
    else if(v.equals(tvStartAfternoon))
      UIHelper.openTimePicker(this, wtdStartAfternoon, tvStartAfternoon);
    else if(v.equals(tvEndAfternoon))
      UIHelper.openTimePicker(this, wtdEndAfternoon, tvEndAfternoon);
    else if(v.equals(tvAdditionalBreak))
      UIHelper.openTimePicker(this, wtdAdditionalBreak, tvAdditionalBreak);
    else if(v.equals(tvLegalWorktime))
      UIHelper.openTimePicker(this, wtdLegalWorktime, tvLegalWorktime);
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
    if(spProfilesAdapter != null) {
      String name = spProfilesAdapter.getItem(indexInAdapter);
      if(name != null && !name.isEmpty()) {
        DayEntry de = app.getProfilesFactory().getByName(name);
        if (de != null) {
          tvStartMorning.setText(de.getStartMorning().timeString());
          tvEndMorning.setText(de.getEndMorning().timeString());
          tvStartAfternoon.setText(de.getStartAfternoon().timeString());
          tvEndAfternoon.setText(de.getEndAfternoon().timeString());
          tvAdditionalBreak.setText(de.getAdditionalBreak().timeString());
          tvLegalWorktime.setText(de.getLegalWorktime().timeString());
          etAmount.setText(String.format(Locale.US, "%.02f", de.getAmountByHour()).replaceAll(",", "."));
          spTypeMorning.setSelection(de.getTypeMorning() == DayType.ERROR ? 0 : de.getTypeMorning().value() + 1);
          spTypeAfternoon.setSelection(de.getTypeAfternoon() == DayType.ERROR ? 0 : de.getTypeAfternoon().value() + 1);
          etName.setText(de.getName());
          refreshStartEndPause(de);
          selectedProfile = de;
        }
      } else
        selectedProfile = null;
    }
  }

  /**
   * Refresh the internal components.
   * @param de The current day entry.
   */
  private void refreshStartEndPause(DayEntry de) {
    wtdStartMorning = de.getStartMorning().clone();
    wtdEndMorning = de.getEndMorning().clone();
    wtdStartAfternoon = de.getStartAfternoon().clone();
    wtdEndAfternoon = de.getEndAfternoon().clone();
    wtdAdditionalBreak = de.getAdditionalBreak().clone();
    if(de.getLegalWorktime().timeString().equals(getString(R.string.default_time)))
      de.setLegalWorktime(app.getLegalWorkTimeByDay().timeString());
    wtdLegalWorktime = de.getLegalWorktime().clone();
  }

  /**
   * See official javadoc.
   * @param adapterView See official javadoc.
   */
  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {

  }
}
