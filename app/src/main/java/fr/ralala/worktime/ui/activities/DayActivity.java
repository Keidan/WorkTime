package fr.ralala.worktime.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;
import fr.ralala.worktime.launchers.LauncherDayActivity;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.ProfileEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.services.AutoExportService;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.ui.widgets.DayWidgetProvider1x1;
import fr.ralala.worktime.ui.widgets.DayWidgetProvider4x1;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the activity containing the day entries used for the profiles and the classic insertions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class DayActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
  private static final String TAG = "DayActivity";
  public static final String ZERO_TIME = "00:00";
  private Spinner mSpProfile = null;
  private Spinner mSpTypeMorning = null;
  private Spinner mSpTypeAfternoon = null;
  private TextView mTvStartMorning = null;
  private TextView mTvEndMorning = null;
  private TextView mTvStartAfternoon = null;
  private TextView mTvLegalWorktime = null;
  private TextView mTvEndAfternoon = null;
  private TextView mTvAdditionalBreak = null;
  private TextView mTvRecoveryTime = null;
  private EditText mEtAmount = null;
  private EditText mEtName = null;
  private ImageView mIvGeoloc = null;
  private DayEntry mDe = null;
  private boolean mDisplayProfile = false;
  private ArrayAdapter<String> mSpProfilesAdapter = null;
  private WorkTimeDay mWtdStartMorning = null;
  private WorkTimeDay mWtdEndMorning = null;
  private WorkTimeDay mWtdStartAfternoon = null;
  private WorkTimeDay mWtdEndAfternoon = null;
  private WorkTimeDay mWtdAdditionalBreak = null;
  private WorkTimeDay mWtdRecoveryTime = null;
  private WorkTimeDay mWtdLegalWorktime = null;
  private ApplicationCtx mApp = null;
  private ProfileEntry mSelectedProfile = null;
  private boolean mOpenForAdd = false;
  private boolean mFromClear = false;
  private boolean mFromWidget = false;
  private Location mLocation = null;

  /**
   * Clear is required from a widget.
   */
  private void clearFromWidget() {
    if (mFromWidget) {
      mApp.setLastWidgetOpen(System.currentTimeMillis());
      if (mApp.isExportAutoSave())
        startService(new Intent(this, AutoExportService.class));
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
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    UIHelper.openAnimation(this);
    super.onCreate(savedInstanceState);
    mApp = (ApplicationCtx) getApplication();
    setContentView(R.layout.activity_day);
    String date = null;
    boolean show = false;
    if (getIntent().getAction() == null || (!getIntent().getAction().equals(DayWidgetProvider1x1.ACTION_FROM_WIDGET) && !getIntent().getAction().equals(DayWidgetProvider4x1.ACTION_FROM_WIDGET))) {
      if (getIntent().getExtras() != null) {
        Bundle extras = getIntent().getExtras();
        date = extras.getString(LauncherDayActivity.DAY_ACTIVITY_EXTRA_DATE);
        mDisplayProfile = extras.getBoolean(LauncherDayActivity.DAY_ACTIVITY_EXTRA_PROFILE);
        if (date == null || date.equals("null")) date = "";
      }
      show = true;
    } else if (getIntent().getAction().equals(DayWidgetProvider1x1.ACTION_FROM_WIDGET) || getIntent().getAction().equals(DayWidgetProvider4x1.ACTION_FROM_WIDGET)) {
      /* If the main activity is not started, all contexts are reset, so we need to reload the required contexts */
      if (!mApp.openSql(this)) {
        UIHelper.toast(this, R.string.error_widget_sql);
        String text = "Widget error SQL";
        ApplicationCtx.addLog(this, TAG, text);
        Log.e(getClass().getSimpleName(), text);
        finish();
        return;
      }
      date = mApp.getDaysFactory().getCurrentDay(mApp).getDay().dateString();
      mDisplayProfile = true;
      mFromWidget = true;
    }

    ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(show);
      actionBar.setDisplayHomeAsUpEnabled(show);
    }
    List<ProfileEntry> profiles = mApp.getProfilesFactory().list();
    if (mDisplayProfile) {
      DayEntry de = mApp.getDaysFactory().getDay(date);
      if (de != null) {
        if (mFromWidget && !de.getWorkTime().isValidTime())
          this.mDe = null;
        else
          this.mDe = de;
      }
    } else {
      for (ProfileEntry de : profiles) {
        if (de.getName().equals(date)) {
          if (mFromWidget && !de.getWorkTime().isValidTime())
            this.mDe = null;
          else
            this.mDe = de;
          break;
        }
      }
    }
    if (mDe == null) {
      mOpenForAdd = true;
      if (mDisplayProfile)
        mDe = new DayEntry(this, mFromWidget ? WorkTimeDay.now() : WorkTimeDay.fromDate(date), DayType.ERROR, DayType.ERROR);
      else
        mDe = new ProfileEntry(this, WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
      if (date != null && !date.contains("/"))
        mDe.setDay(date);
    }
    refreshStartEndPause(mDe);

    if (mDisplayProfile)
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
    mTvRecoveryTime = findViewById(R.id.tvRecoveryTime);
    mTvLegalWorktime = findViewById(R.id.tvLegalWorktime);
    mEtAmount = findViewById(R.id.etAmount);
    TextView tvName = findViewById(R.id.tvName);
    mEtName = findViewById(R.id.etName);
    mIvGeoloc = findViewById(R.id.ivGeoloc);

    boolean hw = mApp.isHideWage();
    mEtAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    View tvLblAmount = findViewById(R.id.tvLblAmount);
    if (tvLblAmount != null)
      tvLblAmount.setVisibility(hw ? View.INVISIBLE : View.VISIBLE);
    /* add click listener for the time picker */
    mTvStartMorning.setOnClickListener(this);
    mTvEndMorning.setOnClickListener(this);
    mTvStartAfternoon.setOnClickListener(this);
    mTvEndAfternoon.setOnClickListener(this);
    mTvAdditionalBreak.setOnClickListener(this);
    mTvRecoveryTime.setOnClickListener(this);
    mTvLegalWorktime.setOnClickListener(this);
    /* manage view for the call from the profile view */
    int v;
    if (mDisplayProfile) {
      v = View.VISIBLE;
      mSpProfile.setVisibility(v);
      if (tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.GONE;
      if (profiles.isEmpty()) {
        mSpProfile.setVisibility(View.GONE);
        if (tvProfile != null)
          tvProfile.setVisibility(View.GONE);
      }
    } else {
      v = View.GONE;
      mSpProfile.setVisibility(v);
      if (tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.VISIBLE;
      if (mDe instanceof ProfileEntry) {
        mEtName.setText(((ProfileEntry) mDe).getName());
      }
      mIvGeoloc.setOnClickListener(this);
    }
    if (tvName != null)
      tvName.setVisibility(v);
    mEtName.setVisibility(v);
    mIvGeoloc.setVisibility(v);
    /* build type spinner */
    final ArrayAdapter<String> spTypeAdapterMorning = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    spTypeAdapterMorning.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpTypeMorning.setAdapter(spTypeAdapterMorning);
    spTypeAdapterMorning.add("");
    spTypeAdapterMorning.add(DayType.getText(this, DayType.AT_WORK));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.RECOVERY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.HOLIDAY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.PUBLIC_HOLIDAY));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.SICKNESS));
    spTypeAdapterMorning.add(DayType.getText(this, DayType.UNPAID));
    final ArrayAdapter<String> spTypeAdapterAfternoon = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    spTypeAdapterAfternoon.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
    mSpTypeAfternoon.setAdapter(spTypeAdapterAfternoon);
    spTypeAdapterAfternoon.add("");
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.AT_WORK));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.RECOVERY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.HOLIDAY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.PUBLIC_HOLIDAY));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.SICKNESS));
    spTypeAdapterAfternoon.add(DayType.getText(this, DayType.UNPAID));

    selectSpinner(mSpTypeMorning, mDe.getTypeMorning());
    selectSpinner(mSpTypeAfternoon, mDe.getTypeAfternoon());

    if (mDisplayProfile) {
      /* build profiles spinner */
      mSpProfilesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
      mSpProfilesAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
      mSpProfile.setAdapter(mSpProfilesAdapter);
      mSpProfilesAdapter.add("");
      for (ProfileEntry profile : profiles)
        mSpProfilesAdapter.add(profile.getName());
      mSpProfile.setOnItemSelectedListener(this);
    }

    mTvStartMorning.setText(mDe.getStartMorning().timeString());
    mTvEndMorning.setText(mDe.getEndMorning().timeString());
    mTvStartAfternoon.setText(mDe.getStartAfternoon().timeString());
    mTvEndAfternoon.setText(mDe.getEndAfternoon().timeString());
    mTvAdditionalBreak.setText(mDe.getAdditionalBreak().timeString());
    mTvRecoveryTime.setText(mDe.getRecoveryTime().timeString());
    mTvLegalWorktime.setText(mDe.getLegalWorkTime().timeString());

    if (!mApp.isHideWage()) {
      mEtAmount.setTypeface(mTvStartAfternoon.getTypeface());
      mEtAmount.setTextSize(14);
      mEtAmount.setTextColor(mTvStartAfternoon.getTextColors());
      mEtAmount.setText(String.format(Locale.US, "%.02f",
        mDe.getAmountByHour() != 0 ? mDe.getAmountByHour() : mApp.getAmountByHour()).replace(",", "."));
    }
    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        back();
      }
    });
  }

  private void back() {
    setResult(RESULT_CANCELED);
    finish();
    UIHelper.closeAnimation(this);
    clearFromWidget();
  }

  private void selectSpinner(Spinner sp, final DayType type) {
    if (type == DayType.ERROR) {
      sp.setSelection(0);
    } else {
      for (int i = 0; i < sp.getAdapter().getCount(); i++) {
        DayType dt = DayType.compute(this, sp.getAdapter().getItem(i).toString());
        if (dt == type) {
          sp.setSelection(i);
          break;
        }
      }
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
    if (mTvLegalWorktime.getText().equals(getString(R.string.default_time))) {
      String t = mApp.getLegalWorkTimeByDay().timeString();
      mTvLegalWorktime.setText(t);
      mDe.setLegalWorkTime(t);
    }
    if (mDisplayProfile && mOpenForAdd) {
      ProfileEntry de = mApp.getProfilesFactory().getHighestLearningWeight();
      if (de == null)
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
   *
   * @param menu The selected menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_day, menu);
    return true;
  }

  private boolean canZeroLegalWorkTime() {
    return mTvStartMorning.getText().toString().equals(ZERO_TIME) &&
      mTvEndMorning.getText().toString().equals(ZERO_TIME) &&
      mTvStartAfternoon.getText().toString().equals(ZERO_TIME) &&
      mTvEndAfternoon.getText().toString().equals(ZERO_TIME);
  }

  private void cancel() {
    if (mDisplayProfile) {
      setResult(RESULT_OK);
      mFromClear = true;
      mWtdStartMorning = new WorkTimeDay();
      mTvStartMorning.setText(mWtdStartMorning.timeString());
      mWtdEndMorning = new WorkTimeDay();
      mTvEndMorning.setText(mWtdEndMorning.timeString());
      mWtdStartAfternoon = new WorkTimeDay();
      mTvStartAfternoon.setText(mWtdStartAfternoon.timeString());
      mWtdEndAfternoon = new WorkTimeDay();
      mTvEndAfternoon.setText(mWtdEndAfternoon.timeString());
      mWtdAdditionalBreak = new WorkTimeDay();
      mTvAdditionalBreak.setText(mWtdAdditionalBreak.timeString());
      mWtdRecoveryTime = new WorkTimeDay();
      mTvRecoveryTime.setText(mWtdRecoveryTime.timeString());
      mWtdLegalWorktime = mApp.getLegalWorkTimeByDay();
      mTvLegalWorktime.setText(mWtdLegalWorktime.timeString());
      mEtAmount.setText(getString(R.string.zero));
      mSpTypeMorning.setSelection(DayType.AT_WORK.value());
      mSpTypeAfternoon.setSelection(DayType.AT_WORK.value());
      mEtName.setText("");
      mLocation = new Location("");
      setTitle(mDe.getDay().dateString());
      mSpProfile.setSelection(0);
    } else
      back();
  }

  private void doneProfile(DayEntry newEntry) {
    boolean match = mDe.match(newEntry, false);
    if (!match) {
      mApp.getProfilesFactory().updateProfilesLearningWeight(mSelectedProfile, mApp.getProfilesWeightDepth(), mFromClear);
      mFromClear = false;
      if (newEntry.getTypeMorning() == DayType.RECOVERY || newEntry.getTypeAfternoon() == DayType.RECOVERY || newEntry.getStartMorning().isValidTime() || newEntry.getEndAfternoon().isValidTime()) {
        mApp.getDaysFactory().add(newEntry);
      } else
        mApp.getDaysFactory().remove(mDe);
      AndroidHelper.updateWidget(this, DayWidgetProvider1x1.class);
      AndroidHelper.updateWidget(this, DayWidgetProvider4x1.class);
    }
  }

  private boolean validateDoneDay(DayEntry newEntry) {
    boolean error = false;
    if (mEtName.getText().toString().isEmpty()) {
      UIHelper.shakeError(mEtName, getString(R.string.error_no_name));
      error = true;
    }
    if (newEntry.getTypeMorning() == DayType.ERROR) {
      UIHelper.shakeError(mSpTypeMorning);
      error = true;
    }
    if (newEntry.getTypeAfternoon() == DayType.ERROR) {
      UIHelper.shakeError(mSpTypeAfternoon);
      error = true;
    }
    if (checkMorning(newEntry) || checkAfternoon(newEntry))
      error = true;
    return error;
  }

  private boolean doneDay(DayEntry newEntry) {
    if (validateDoneDay(newEntry))
      return true;
    if (mDe instanceof ProfileEntry && newEntry instanceof ProfileEntry) {
      ProfileEntry peGlobal = (ProfileEntry) mDe;
      ProfileEntry peNew = (ProfileEntry) newEntry;
      if (mLocation != null) {
        peNew.setLongitude(mLocation.getLongitude());
        peNew.setLatitude(mLocation.getLatitude());
      }
      if (peGlobal.getName().isEmpty() || !peGlobal.match(peNew, true)) {
        if (peNew.getTypeMorning() == DayType.RECOVERY || peNew.getTypeAfternoon() == DayType.RECOVERY || peNew.getStartMorning().isValidTime() || peNew.getEndAfternoon().isValidTime()) {
          peNew.setLearningWeight(peGlobal.getLearningWeight());
          mApp.getProfilesFactory().add(peNew);
        } else
          mApp.getProfilesFactory().remove(peGlobal);
      }
    }
    return false;
  }

  private void done() {
    DayEntry newEntry = mDisplayProfile ? new DayEntry(this, mDe.getDay().toCalendar(),
      DayType.compute(this, mSpTypeMorning.getSelectedItem().toString()),
      DayType.compute(this, mSpTypeAfternoon.getSelectedItem().toString()))
      : new ProfileEntry(this, mDe.getDay().toCalendar(),
      DayType.compute(this, mSpTypeMorning.getSelectedItem().toString()),
      DayType.compute(this, mSpTypeAfternoon.getSelectedItem().toString()));
    String s = mEtAmount.getText().toString().trim();
    if (s.equals(getString(R.string.zero))) s = "";
    newEntry.setAmountByHour(s.isEmpty() ? mApp.getAmountByHour() : Double.parseDouble(s));
    if (newEntry instanceof ProfileEntry)
      ((ProfileEntry) newEntry).setName(mEtName.getText().toString());
    newEntry.setID(mDe.getID());
    newEntry.setEndMorning(mTvEndMorning.getText().toString());
    newEntry.setStartMorning(mTvStartMorning.getText().toString());
    newEntry.setEndAfternoon(mTvEndAfternoon.getText().toString());
    newEntry.setAdditionalBreak(mTvAdditionalBreak.getText().toString());
    newEntry.setRecoveryTime(mTvRecoveryTime.getText().toString());
    newEntry.setStartAfternoon(mTvStartAfternoon.getText().toString());
    newEntry.setLegalWorkTime(mTvLegalWorktime.getText().toString());
    if (newEntry.getTypeMorning() != DayType.ERROR) {
      if (checkMorning(newEntry))
        return;
    } else if (newEntry.getTypeAfternoon() != DayType.ERROR && (checkAfternoon(newEntry))) {
      return;
    }

    if (!canZeroLegalWorkTime() && mTvLegalWorktime.getText().toString().equals(getString(R.string.default_time))) {
      UIHelper.shakeError(mTvLegalWorktime, getString(R.string.error_invalid_legal_worktime));
      return;
    }
    if (mDisplayProfile) {
      doneProfile(newEntry);
    } else {
      if (doneDay(newEntry))
        return;
    }
    setResult(RESULT_OK);
    finish();
    UIHelper.closeAnimation(this);
    clearFromWidget();
  }

  /**
   * Called when the options item is clicked (home and cancel).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      back();
      return true;
    } else if (item.getItemId() == R.id.action_cancel) {
      cancel();
      return true;
    } else if (item.getItemId() == R.id.action_done) {
      done();
      return true;
    }
    return false;
  }

  /**
   * Checks whether the morning entries is valid
   *
   * @param newEntry The entry to check.
   * @return true on error.
   */
  private boolean checkAfternoon(DayEntry newEntry) {
    if (newEntry.getStartAfternoon().getHours() == 0 && newEntry.getTypeAfternoon() != DayType.RECOVERY) {
      UIHelper.shakeError(mTvStartAfternoon, getString(R.string.error_invalid_start));
      return true;
    } else if (newEntry.getEndAfternoon().getHours() == 0 && newEntry.getTypeAfternoon() != DayType.RECOVERY) {
      UIHelper.shakeError(mTvEndAfternoon, getString(R.string.error_invalid_end));
      return true;
    } else if (mTvStartAfternoon.getText().toString().equals(mTvEndAfternoon.getText().toString()) && newEntry.getTypeAfternoon() != DayType.RECOVERY) {
      UIHelper.shakeError(mTvStartAfternoon, getString(R.string.error_invalid_start_end_morning));
      UIHelper.shakeError(mTvEndAfternoon, getString(R.string.error_invalid_start_end_morning));
      return true;
    }
    return false;
  }

  /**
   * Checks whether the morning entries is valid
   *
   * @param newEntry The entry to check.
   * @return true on error.
   */
  private boolean checkMorning(DayEntry newEntry) {
    if (newEntry.getStartMorning().getHours() == 0 && newEntry.getTypeMorning() != DayType.RECOVERY) {
      UIHelper.shakeError(mTvStartMorning, getString(R.string.error_invalid_start));
      return true;
    } else if (newEntry.getEndMorning().getHours() == 0 && newEntry.getTypeMorning() != DayType.RECOVERY) {
      UIHelper.shakeError(mTvEndMorning, getString(R.string.error_invalid_end));
      return true;
    } else if (mTvStartMorning.getText().toString().equals(mTvEndMorning.getText().toString()) && newEntry.getTypeMorning() != DayType.RECOVERY) {
      UIHelper.shakeError(mTvStartMorning, getString(R.string.error_invalid_start_end_morning));
      UIHelper.shakeError(mTvEndMorning, getString(R.string.error_invalid_start_end_morning));
      return true;
    }
    return false;
  }

  /**
   * Called when a button is clicked (mFab, mTvStartMorning, mTvEndMorning, mTvStartAfternoon, mTvEndAfternoon, mTvAdditionalBreak, mTvRecoveryTime and mTvLegalWorktime).
   *
   * @param v The view clicked.
   */
  public void onClick(final View v) {
    if (v.equals(mTvStartMorning))
      UIHelper.openTimePicker(this, mWtdStartMorning, mTvStartMorning);
    else if (v.equals(mTvEndMorning))
      UIHelper.openTimePicker(this, mWtdEndMorning, mTvEndMorning);
    else if (v.equals(mTvStartAfternoon))
      UIHelper.openTimePicker(this, mWtdStartAfternoon, mTvStartAfternoon);
    else if (v.equals(mTvEndAfternoon))
      UIHelper.openTimePicker(this, mWtdEndAfternoon, mTvEndAfternoon);
    else if (v.equals(mTvAdditionalBreak))
      UIHelper.openTimePicker(this, mWtdAdditionalBreak, mTvAdditionalBreak);
    else if (v.equals(mTvRecoveryTime))
      UIHelper.openTimePicker(this, mWtdRecoveryTime, mTvRecoveryTime);
    else if (v.equals(mTvLegalWorktime))
      UIHelper.openTimePicker(this, mWtdLegalWorktime, mTvLegalWorktime);
    else if (v.equals(mIvGeoloc)) {
      locateClick();
    }
  }

  private void locateClick() {
    Location loc = ((ProfileEntry) mDe).getLocation();
    if ((Double.isNaN(loc.getLatitude()) || loc.getLatitude() == 0.0) &&
      (Double.isNaN(loc.getLongitude()) || loc.getLongitude() == 0.0) && mLocation != null &&
      !Double.isNaN(mLocation.getLatitude()) && mLocation.getLatitude() != 0.0 &&
      !Double.isNaN(mLocation.getLongitude()) && mLocation.getLongitude() != 0.0)
      loc = mLocation;
    createGeolocateDialog(getString(R.string.geolocation), loc, (dialog, latitude, longitude) -> {
      final String slatitude = Objects.requireNonNull(latitude.getText()).toString();
      final String slongitude = Objects.requireNonNull(longitude.getText()).toString();
      final Location location = new Location("");
      if (!slatitude.trim().isEmpty())
        try {
          location.setLatitude(Double.parseDouble(slatitude));
        } catch (Exception e) {
          UIHelper.shakeError(latitude, getString(R.string.error_invalid_latitude));
          return;
        }
      if (!slongitude.trim().isEmpty())
        try {
          location.setLongitude(Double.parseDouble(slongitude));
        } catch (Exception e) {
          UIHelper.shakeError(longitude, getString(R.string.error_invalid_longitude));
          return;
        }
      mLocation = location;
      dialog.dismiss();
    });
  }

  /**
   * Called when an item is selected.
   *
   * @param adapterView The adapter view.
   * @param view        The selected view.
   * @param i           The view index in the adapter.
   * @param l           Not used.
   */
  @Override
  public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
    selectProfile(i);
  }

  /**
   * Called when a profile is selected.
   *
   * @param indexInAdapter The index in the adapter.
   */
  private void selectProfile(int indexInAdapter) {
    if (mSpProfilesAdapter != null) {
      String name = mSpProfilesAdapter.getItem(indexInAdapter);
      if (name != null && !name.isEmpty()) {
        ProfileEntry pe = mApp.getProfilesFactory().getByName(name);
        if (pe != null) {
          mTvStartMorning.setText(pe.getStartMorning().timeString());
          mTvEndMorning.setText(pe.getEndMorning().timeString());
          mTvStartAfternoon.setText(pe.getStartAfternoon().timeString());
          mTvEndAfternoon.setText(pe.getEndAfternoon().timeString());
          mTvAdditionalBreak.setText(pe.getAdditionalBreak().timeString());
          mTvRecoveryTime.setText(pe.getRecoveryTime().timeString());
          mTvLegalWorktime.setText(pe.getLegalWorkTime().timeString());
          mEtAmount.setText(String.format(Locale.US, "%.02f", pe.getAmountByHour()).replace(",", "."));
          selectSpinner(mSpTypeMorning, pe.getTypeMorning());
          selectSpinner(mSpTypeAfternoon, pe.getTypeAfternoon());
          mEtName.setText(pe.getName());
          refreshStartEndPause(pe);
          mSelectedProfile = pe;
          mLocation = pe.getLocation();
        }
      } else
        mSelectedProfile = null;
    }
  }

  /**
   * Refresh the internal components.
   *
   * @param de The current day entry.
   */
  private void refreshStartEndPause(DayEntry de) {
    mWtdStartMorning = de.getStartMorning().copy();
    mWtdEndMorning = de.getEndMorning().copy();
    mWtdStartAfternoon = de.getStartAfternoon().copy();
    mWtdEndAfternoon = de.getEndAfternoon().copy();
    mWtdAdditionalBreak = de.getAdditionalBreak().copy();
    mWtdRecoveryTime = de.getRecoveryTime().copy();
    if (de.getLegalWorkTime().timeString().equals(getString(R.string.default_time)))
      de.setLegalWorkTime(mApp.getLegalWorkTimeByDay().timeString());
    mWtdLegalWorktime = de.getLegalWorkTime().copy();
  }

  /**
   * See official javadoc.
   *
   * @param adapterView See official javadoc.
   */
  @Override
  public void onNothingSelected(AdapterView<?> adapterView) {
    /* Nothing */
  }


  private interface DialogPositiveClick {
    void onClick(AlertDialog dialog, TextInputEditText etLatitude, TextInputEditText etLongitude);
  }

  @SuppressLint("InflateParams")
  private void createGeolocateDialog(String title, Location defaultLocation, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false)
      .setTitle(title)
      .setPositiveButton(android.R.string.ok, null)
      .setNegativeButton(android.R.string.cancel, (dialog, whichButton) -> {
      });
    LayoutInflater factory = LayoutInflater.from(this);
    builder.setView(factory.inflate(R.layout.content_dialog_add_geoloc, null));
    final AlertDialog dialog = builder.create();
    dialog.show();
    TextInputEditText etLatitude = dialog.findViewById(R.id.tieLatitude);
    if (etLatitude != null)
      etLatitude.setText((defaultLocation == null || Double.isNaN(defaultLocation.getLatitude()) ||
        defaultLocation.getLatitude() == 0.0) ? "" : String.valueOf(defaultLocation.getLatitude()));
    TextInputEditText etLongitude = dialog.findViewById(R.id.tieLongitude);
    if (etLongitude != null)
      etLongitude.setText(String.valueOf((defaultLocation == null || Double.isNaN(defaultLocation.getLongitude()) ||
        defaultLocation.getLongitude() == 0.0) ? "" : defaultLocation.getLongitude()));
    Button btGeolocateMe = dialog.findViewById(R.id.btGeolocateMe);
    if (btGeolocateMe != null)
      btGeolocateMe.setOnClickListener(v -> locateMeClick(etLatitude, etLongitude));
    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v -> positiveClick.onClick(dialog, etLatitude, etLongitude));
  }

  private void locateMeClick(TextInputEditText etLatitude, TextInputEditText etLongitude) {
    if (!AndroidHelper.getLastLocationNewMethod(this, new AndroidHelper.LocationListener() {
      public void onLocationSuccess(Location location) {
        if (location != null) {
          if (etLatitude != null)
            etLatitude.setText(String.valueOf(location.getLatitude()));
          if (etLongitude != null)
            etLongitude.setText(String.valueOf(location.getLongitude()));
        }
        String text = "Location: " + location;
        ApplicationCtx.addLog(DayActivity.this, TAG, text);
        Log.i(getClass().getSimpleName(), text);
      }

      public void onLocationError(@NonNull Exception e) {
        String text = "Exception: " + e.getMessage();
        ApplicationCtx.addLog(DayActivity.this, TAG, text);
        Log.e(getClass().getSimpleName(), text, e);
        UIHelper.toast(DayActivity.this, e.getMessage());
      }
    }))
      UIHelper.toast(this, R.string.error_gps_permissions);
  }
}
