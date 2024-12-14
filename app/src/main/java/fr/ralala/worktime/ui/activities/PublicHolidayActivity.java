package fr.ralala.worktime.ui.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import fr.ralala.worktime.ApplicationCtx;
import fr.ralala.worktime.R;
import fr.ralala.worktime.launchers.LauncherPublicHolidayActivity;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.PublicHolidayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.ui.utils.UIHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the activity containing the public holiday entries used for the profiles and the classic insertions
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PublicHolidayActivity extends AppCompatActivity {
  private ApplicationCtx mApp = null;
  private PublicHolidayEntry mPhe = null;
  private EditText mTname = null;
  private DatePicker mTdate = null;
  private CheckBox mCkRecurrence = null;

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
    setContentView(R.layout.activity_public_holiday);
    ActionBar actionBar = getDelegate().getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    String extra = "";
    if (getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      extra = extras.getString(LauncherPublicHolidayActivity.PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME);
      if (extra == null || extra.equals("null")) extra = "";
    }
    if (!extra.isEmpty()) {
      int idx = extra.lastIndexOf('|');
      String name = extra.substring(0, idx);
      String date = extra.substring(idx + 1);
      PublicHolidayEntry de = mApp.getPublicHolidaysFactory().getByNameAndDate(name, date);
      if (de != null) {
        this.mPhe = de;
      }
    }
    if (mPhe == null) {
      mPhe = new PublicHolidayEntry(this, WorkTimeDay.now(), DayType.ERROR, DayType.ERROR);
      mPhe.setName("");
    }
    mCkRecurrence = findViewById(R.id.ckRecurrence);
    mTname = findViewById(R.id.etName);
    mTdate = findViewById(R.id.dpDate);
    if (mPhe != null) {
      mTname.setText(mPhe.getName());
      // set current date into datepicker
      mTdate.init(mPhe.getDay().getYear(), mPhe.getDay().getMonth() - 1, mPhe.getDay().getDay(), null);
      mCkRecurrence.setChecked(mPhe.isRecurrence());
    } else {
      mTname.setText("");
      WorkTimeDay now = WorkTimeDay.now();
      // set current date into datepicker
      mTdate.init(now.getYear(), now.getMonth() - 1, now.getDay(), null);
      mCkRecurrence.setChecked(false);
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

  /**
   * Called when the options item is clicked (home and cancel).
   *
   * @param item The selected menu.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    if (item.getItemId() == android.R.id.home || item.getItemId() == R.id.action_cancel) {
      back();
      return true;
    } else if (item.getItemId() == R.id.action_done) {
      final String name = mTname.getText().toString().trim();
      if (name.isEmpty()) {
        UIHelper.shakeError(mTname, getString(R.string.error_no_name));
        return true;
      }
      WorkTimeDay wtd = new WorkTimeDay();
      wtd.setDay(mTdate.getDayOfMonth());
      wtd.setMonth(mTdate.getMonth() + 1);
      wtd.setYear(mTdate.getYear());
      PublicHolidayEntry phe = new PublicHolidayEntry(this, wtd, DayType.PUBLIC_HOLIDAY, DayType.PUBLIC_HOLIDAY);
      phe.setID(mPhe.getID());
      if (mApp.getPublicHolidaysFactory().testValidity(phe)) {
        phe.setName(name);
        phe.setRecurrence(mCkRecurrence.isChecked());
        mApp.getPublicHolidaysFactory().add(phe);
        setResult(RESULT_OK);
        finish();
        UIHelper.closeAnimation(this);
      } else {
        UIHelper.shakeError(mTname, getString(R.string.error_duplicate_public_holiday));
      }
      return true;
    }
    return false;
  }

}
