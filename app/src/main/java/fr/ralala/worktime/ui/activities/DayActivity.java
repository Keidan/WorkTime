package fr.ralala.worktime.ui.activities;

import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.ProfileEntry;
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
  public static final int REQUEST_START_ACTIVITY = 100;
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
  private MainApplication mApp = null;
  private ProfileEntry mSelectedProfile = null;
  private boolean mOpenForAdd = false;
  private boolean mFromClear = false;
  private boolean mFromWidget = false;
  private Location mLocation = null;
  private final ViewGroup mNullParent = null;

  /**
   * Starts an activity.
   * @param fragment The Android fragment.
   * @param date The date used for the extra part.
   * @param profile True from a profile.
   */
  public static void startActivity(final Fragment fragment, final String date, final boolean profile) {
    Intent intent = new Intent(fragment.getContext(), DayActivity.class);
    intent.putExtra(DAY_ACTIVITY_EXTRA_DATE, date);
    intent.putExtra(DAY_ACTIVITY_EXTRA_PROFILE, profile);
    fragment.startActivityForResult(intent, REQUEST_START_ACTIVITY);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
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
    mApp = MainApplication.getInstance();
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
      date = mApp.getDaysFactory().getCurrentDay().getDay().dateString();
      mDisplayProfile = true;
      mFromWidget = true;
    }

    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(show);
      actionBar.setDisplayHomeAsUpEnabled(show);
    }
    List<ProfileEntry> profiles = mApp.getProfilesFactory().list();
    if(mDisplayProfile) {
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
    if(mDe == null) {
      mOpenForAdd = true;
      mDe = mDisplayProfile ? new DayEntry(WorkTimeDay.now(), DayType.ERROR, DayType.ERROR)
            : new ProfileEntry(WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
      if(date != null && !date.isEmpty() && date.contains("/"))
        mDe.setDay(date);
    }
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
    mTvRecoveryTime = findViewById(R.id.tvRecoveryTime);
    mTvLegalWorktime = findViewById(R.id.tvLegalWorktime);
    mEtAmount = findViewById(R.id.etAmount);
    TextView tvName = findViewById(R.id.tvName);
    mEtName = findViewById(R.id.etName);
    mIvGeoloc = findViewById(R.id.ivGeoloc);

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
    mTvRecoveryTime.setOnClickListener(this);
    mTvLegalWorktime.setOnClickListener(this);
    /* manage view for the call from the profile view */
    int v;
    if(mDisplayProfile) {
      v = View.VISIBLE;
      mSpProfile.setVisibility(v);
      if(tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.GONE;
      if(profiles.isEmpty()) {
        mSpProfile.setVisibility(View.GONE);
        if(tvProfile != null)
          tvProfile.setVisibility(View.GONE);
      }
    } else {
      v = View.GONE;
      mSpProfile.setVisibility(v);
      if(tvProfile != null)
        tvProfile.setVisibility(v);
      v = View.VISIBLE;
      if(ProfileEntry.class.isInstance(mDe)) {
        mEtName.setText(((ProfileEntry) mDe).getName());
      }
      mIvGeoloc.setOnClickListener(this);
    }
    if(tvName != null)
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

    if(mDisplayProfile) {
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
    mTvLegalWorktime.setText(mDe.getLegalWorktime().timeString());

    if(!mApp.isHideWage()) {
      mEtAmount.setTypeface(mTvStartAfternoon.getTypeface());
      mEtAmount.setTextSize(14);
      mEtAmount.setTextColor(mTvStartAfternoon.getTextColors());
      mEtAmount.setText(String.format(Locale.US, "%.02f",
        mDe.getAmountByHour() != 0 ? mDe.getAmountByHour() : mApp.getAmountByHour()).replaceAll(",", "."));
    }
  }

  private void selectSpinner(Spinner sp, final DayType type) {
    if(type == DayType.ERROR) {
      sp.setSelection(0);
    } else {
      for(int i = 0; i < sp.getAdapter().getCount(); i++) {
        DayType dt = DayType.compute(this, sp.getAdapter().getItem(i).toString());
        if(dt == type) {
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
    if(mTvLegalWorktime.getText().equals(getString(R.string.default_time))) {
      String t = mApp.getLegalWorkTimeByDay().timeString();
      mTvLegalWorktime.setText(t);
      mDe.setLegalWorktime(t);
    }
    if(mDisplayProfile && mOpenForAdd) {
      ProfileEntry de = mApp.getProfilesFactory().getHighestLearningWeight();
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
          setResult(RESULT_OK);
          mFromClear = true;
          mTvStartMorning.setText((mWtdStartMorning = new WorkTimeDay()).timeString());
          mTvEndMorning.setText((mWtdEndMorning = new WorkTimeDay()).timeString());
          mTvStartAfternoon.setText((mWtdStartAfternoon = new WorkTimeDay()).timeString());
          mTvEndAfternoon.setText((mWtdEndAfternoon = new WorkTimeDay()).timeString());
          mTvAdditionalBreak.setText((mWtdAdditionalBreak = new WorkTimeDay()).timeString());
          mTvRecoveryTime.setText((mWtdRecoveryTime = new WorkTimeDay()).timeString());
          mTvLegalWorktime.setText((mWtdLegalWorktime = mApp.getLegalWorkTimeByDay()).timeString());
          mEtAmount.setText(getString(R.string.zero));
          mSpTypeMorning.setSelection(DayType.AT_WORK.value());
          mSpTypeAfternoon.setSelection(DayType.AT_WORK.value());
          mEtName.setText("");
          mLocation = new Location("");
          setTitle(mDe.getDay().dateString());
          mSpProfile.setSelection(0);
        } else
          onBackPressed();
        return true;
      case R.id.action_done: {
        DayEntry newEntry = mDisplayProfile ? new DayEntry(mDe.getDay().toCalendar(),
            DayType.compute(this, mSpTypeMorning.getSelectedItem().toString()),
            DayType.compute(this, mSpTypeAfternoon.getSelectedItem().toString()))
            : new ProfileEntry(mDe.getDay().toCalendar(),
              DayType.compute(this, mSpTypeMorning.getSelectedItem().toString()),
              DayType.compute(this, mSpTypeAfternoon.getSelectedItem().toString()));
        String s = mEtAmount.getText().toString().trim();
        if (s.equals(getString(R.string.zero))) s = "";
        newEntry.setAmountByHour(s.isEmpty() ? mApp.getAmountByHour() : Double.parseDouble(s));
        if(ProfileEntry.class.isInstance(newEntry))
          ((ProfileEntry)newEntry).setName(mEtName.getText().toString());
        newEntry.setID(mDe.getID());
        newEntry.setEndMorning(mTvEndMorning.getText().toString());
        newEntry.setStartMorning(mTvStartMorning.getText().toString());
        newEntry.setEndAfternoon(mTvEndAfternoon.getText().toString());
        newEntry.setAdditionalBreak(mTvAdditionalBreak.getText().toString());
        newEntry.setRecoveryTime(mTvRecoveryTime.getText().toString());
        newEntry.setStartAfternoon(mTvStartAfternoon.getText().toString());
        newEntry.setLegalWorktime(mTvLegalWorktime.getText().toString());
        if(newEntry.getTypeMorning() != DayType.ERROR) {
          if(checkMorning(newEntry))
            return true;
        } else if(newEntry.getTypeAfternoon() != DayType.ERROR) {
          if(checkAfternoon(newEntry))
            return true;
        }

        if (mTvLegalWorktime.getText().toString().equals(getString(R.string.default_time))) {
          UIHelper.shakeError(mTvLegalWorktime, getString(R.string.error_invalid_legal_worktime));
          return true;
        }
        if(mDisplayProfile) {
          boolean match = mDe.match(newEntry, false);
          if (!match) {
            mApp.getProfilesFactory().updateProfilesLearningWeight(mSelectedProfile, mApp.getProfilesWeightDepth(), mFromClear);
            mFromClear = false;
            if (newEntry.getTypeMorning() == DayType.RECOVERY || newEntry.getTypeAfternoon() == DayType.RECOVERY || newEntry.getStartMorning().isValidTime() || newEntry.getEndAfternoon().isValidTime()) {
              mApp.getDaysFactory().add(newEntry);
            } else
              mApp.getDaysFactory().remove(mDe);
            if(AndroidHelper.isServiceRunning(this, QuickAccessService.class))
              stopService(new Intent(this, QuickAccessService.class));
            AndroidHelper.updateWidget(this, DayWidgetProvider1x1.class);
            AndroidHelper.updateWidget(this, DayWidgetProvider4x1.class);
          }
        } else {
          if (mEtName.getText().toString().isEmpty()) {
            UIHelper.shakeError(mEtName, getString(R.string.error_no_name));
            return true;
          }
          if(newEntry.getTypeMorning() == DayType.ERROR) {
            UIHelper.shakeError(mSpTypeMorning);
            return true;
          }
          if(newEntry.getTypeAfternoon() == DayType.ERROR) {
            UIHelper.shakeError(mSpTypeAfternoon);
            return true;
          }
          if(checkMorning(newEntry) || checkAfternoon(newEntry))
            return true;

          if(ProfileEntry.class.isInstance(mDe)) {
            ProfileEntry peGlobal = (ProfileEntry)mDe;
            ProfileEntry peNew = (ProfileEntry)newEntry;
            if(mLocation != null) {
              peNew.setLongitude(mLocation.getLongitude());
              peNew.setLatitude(mLocation.getLatitude());
            }
            if(peGlobal.getName().isEmpty() || !peGlobal.match(peNew, true)) {
              if (peNew.getTypeMorning() == DayType.RECOVERY || peNew.getTypeAfternoon() == DayType.RECOVERY || peNew.getStartMorning().isValidTime() || peNew.getEndAfternoon().isValidTime()) {
                peNew.setLearningWeight(peGlobal.getLearningWeight());
                mApp.getProfilesFactory().add(peNew);
              } else
                mApp.getProfilesFactory().remove(peGlobal);
            }
          }
        }
        setResult(RESULT_OK);
        finish();
        UIHelper.closeAnimation(this);
        clearFromWidget();
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the morning entries is valid
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
   * @param v The view clicked.
   */
  public void onClick(final View v) {
    if(v.equals(mTvStartMorning))
      UIHelper.openTimePicker(this, mWtdStartMorning, mTvStartMorning);
    else if(v.equals(mTvEndMorning))
      UIHelper.openTimePicker(this, mWtdEndMorning, mTvEndMorning);
    else if(v.equals(mTvStartAfternoon))
      UIHelper.openTimePicker(this, mWtdStartAfternoon, mTvStartAfternoon);
    else if(v.equals(mTvEndAfternoon))
      UIHelper.openTimePicker(this, mWtdEndAfternoon, mTvEndAfternoon);
    else if(v.equals(mTvAdditionalBreak))
      UIHelper.openTimePicker(this, mWtdAdditionalBreak, mTvAdditionalBreak);
    else if(v.equals(mTvRecoveryTime))
      UIHelper.openTimePicker(this, mWtdRecoveryTime, mTvRecoveryTime);
    else if(v.equals(mTvLegalWorktime))
      UIHelper.openTimePicker(this, mWtdLegalWorktime, mTvLegalWorktime);
    else if(v.equals(mIvGeoloc)) {
      Location loc = ((ProfileEntry)mDe).getLocation();
      if((Double.isNaN(loc.getLatitude()) || loc.getLatitude() == 0.0) &&
          (Double.isNaN(loc.getLongitude()) || loc.getLongitude() == 0.0) && mLocation != null &&
          !Double.isNaN(mLocation.getLatitude()) && mLocation.getLatitude() != 0.0 &&
          !Double.isNaN(mLocation.getLongitude()) && mLocation.getLongitude() != 0.0)
        loc = mLocation;
      createGeolocateDialog(getString(R.string.geolocation), R.layout.content_dialog_add_geoloc, loc, (dialog, latitude, longitude) -> {
        final String slatitude = latitude.getText().toString();
        final String slongitude = longitude.getText().toString();
        final Location location = new Location("");
        if(!slatitude.trim().isEmpty())
          try {
            location.setLatitude(Double.parseDouble(slatitude));
          } catch(Exception e) {
            UIHelper.shakeError(latitude, getString(R.string.error_invalid_latitude));
            return;
          }
        if(!slongitude.trim().isEmpty())
          try {
            location.setLongitude(Double.parseDouble(slongitude));
          } catch(Exception e) {
            UIHelper.shakeError(longitude, getString(R.string.error_invalid_longitude));
            return;
          }
        mLocation = location;
        dialog.dismiss();
      });
    }
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
        ProfileEntry pe = mApp.getProfilesFactory().getByName(name);
        if (pe != null) {
          mTvStartMorning.setText(pe.getStartMorning().timeString());
          mTvEndMorning.setText(pe.getEndMorning().timeString());
          mTvStartAfternoon.setText(pe.getStartAfternoon().timeString());
          mTvEndAfternoon.setText(pe.getEndAfternoon().timeString());
          mTvAdditionalBreak.setText(pe.getAdditionalBreak().timeString());
          mTvRecoveryTime.setText(pe.getRecoveryTime().timeString());
          mTvLegalWorktime.setText(pe.getLegalWorktime().timeString());
          mEtAmount.setText(String.format(Locale.US, "%.02f", pe.getAmountByHour()).replaceAll(",", "."));
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
   * @param de The current day entry.
   */
  private void refreshStartEndPause(DayEntry de) {
    mWtdStartMorning = de.getStartMorning().clone();
    mWtdEndMorning = de.getEndMorning().clone();
    mWtdStartAfternoon = de.getStartAfternoon().clone();
    mWtdEndAfternoon = de.getEndAfternoon().clone();
    mWtdAdditionalBreak = de.getAdditionalBreak().clone();
    mWtdRecoveryTime = de.getRecoveryTime().clone();
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


  private interface DialogPositiveClick {
    void onClick(AlertDialog dialog, TextInputEditText etLatitude, TextInputEditText etLongitude);
  }

  private void createGeolocateDialog(String title, int contentId, Location defaultLocation, DialogPositiveClick positiveClick) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setCancelable(false)
        .setTitle(title)
        .setPositiveButton(android.R.string.yes, null)
        .setNegativeButton(android.R.string.no, (dialog, whichButton) -> { });
    LayoutInflater factory = LayoutInflater.from(this);
    builder.setView(factory.inflate(contentId, mNullParent));
    final AlertDialog dialog = builder.create();
    dialog.show();
    TextInputEditText etLatitude = dialog.findViewById(R.id.tieLatitude);
    if(etLatitude != null)
      etLatitude.setText((defaultLocation == null || Double.isNaN(defaultLocation.getLatitude()) ||
          defaultLocation.getLatitude() == 0.0) ? "" : String.valueOf(defaultLocation.getLatitude()));
    TextInputEditText etLongitude = dialog.findViewById(R.id.tieLongitude);
    if(etLongitude != null)
      etLongitude.setText(String.valueOf((defaultLocation == null || Double.isNaN(defaultLocation.getLongitude()) ||
          defaultLocation.getLongitude() == 0.0) ? "" : defaultLocation.getLongitude()));
    Button btGeolocateMe = dialog.findViewById(R.id.btGeolocateMe);
    if(btGeolocateMe != null)
      btGeolocateMe.setOnClickListener((v) -> {
        if(!AndroidHelper.getLastLocationNewMethod(this, new AndroidHelper.LocationListener() {
          public void onLocationSuccess(Location location) {
            if(location != null) {
              if(etLatitude != null)
                etLatitude.setText(String.valueOf(location.getLatitude()));
              if(etLongitude != null)
                etLongitude.setText(String.valueOf(location.getLongitude()));
            }
            Log.i("DayActivity", "Location: " + location);
          }
          public void onLocationError(@NonNull Exception e) {
            Log.e("DayActivity", "Exception: " + e.getMessage(), e);
            UIHelper.toast(DayActivity.this, e.getMessage());
          }
        }))
          UIHelper.toast(this, R.string.error_gps_permissions);
      });
    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener((v) -> positiveClick.onClick(dialog, etLatitude, etLongitude));
  }
}
