package fr.ralala.worktime.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the activity containing the public holiday entries used for the profiles and the classic insertions
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class PublicHolidayActivity extends AppCompatActivity implements View.OnClickListener {
  public static final String PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME = "fr.ralala.worktime.activities.PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME_name";
  private MainApplication app = null;
  private DayEntry de = null;
  private FloatingActionButton fab = null;
  private EditText tname = null;
  private DatePicker tdate = null;

  public static void startActivity(final Context ctx, final String name) {
    Intent intent = new Intent(ctx, PublicHolidayActivity.class);
    intent.putExtra(PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME, name);
    ctx.startActivity(intent);
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    AndroidHelper.closeAnimation(this);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    app = MainApplication.getApp(this);
    setContentView(R.layout.activity_public_holiday);
    android.support.v7.app.ActionBar actionBar = getDelegate().getSupportActionBar();
    actionBar.setDisplayShowHomeEnabled(true);
    actionBar.setDisplayHomeAsUpEnabled(true);
    AndroidHelper.openAnimation(this);
    String name =  "";
    if(getIntent().getExtras() != null) {
      Bundle extras = getIntent().getExtras();
      name = extras.getString(PUBLIC_HOLIDAY_ACTIVITY_EXTRA_NAME);
      if(name.equals("null")) name = "";
    }
    List<DayEntry> days = app.getPublicHolidaysFactory().list();
    for(DayEntry de : days) {
      if(de.getName().equals(name)) {
        Log.e(getClass().getSimpleName(), "found de:"+de.getDay().dateString());
        this.de = de;
        break;
      }
    }
    if(de == null) {
      de = new DayEntry(WorkTimeDay.now(), DayType.ERROR);
      de.setName(name);
    }
    fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(this);


    tname = (EditText) findViewById(R.id.etName);
    tdate = (DatePicker) findViewById(R.id.dpDate);
    if(de != null) {
      tname.setText(de.getName());
      // set current date into datepicker
      tdate.init(de.getDay().getYear(), de.getDay().getMonth() - 1, de.getDay().getDay(), null);
    } else {
      tname.setText("");
      WorkTimeDay now = WorkTimeDay.now();
      // set current date into datepicker
      tdate.init(now.getYear(), now.getMonth() - 1, now.getDay(), null);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.activity_day, menu);
    MenuItem action_cancel = menu.findItem(R.id.action_cancel);
    action_cancel.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      case R.id.action_cancel:
        finish();
        return true;
    }
    return false;
  }

  public void onClick(final View v) {
    if(v.equals(fab)) {
      final String name = tname.getText().toString().trim();
      if(name.isEmpty()) {
        AndroidHelper.showAlertDialog(this, R.string.error, R.string.error_no_name);
        return;
      }
      WorkTimeDay wtd = new WorkTimeDay();
      wtd.setDay(tdate.getDayOfMonth());
      wtd.setMonth(tdate.getMonth() + 1);
      wtd.setYear(tdate.getYear());
      if(de != null) app.getPublicHolidaysFactory().remove(de); /* remove old entry */
      DayEntry de = new DayEntry(wtd, DayType.PUBLIC_HOLIDAY);
      de.setName(name);
      app.getPublicHolidaysFactory().add(de);
    }
  }

}
