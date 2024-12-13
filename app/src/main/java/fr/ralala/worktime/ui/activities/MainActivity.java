package fr.ralala.worktime.ui.activities;


import android.Manifest;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.launchers.LauncherDayActivity;
import fr.ralala.worktime.launchers.LauncherPublicHolidayActivity;
import fr.ralala.worktime.services.AutoExportService;
import fr.ralala.worktime.ui.activities.settings.SettingsActivity;
import fr.ralala.worktime.ui.changelog.ChangeLog;
import fr.ralala.worktime.ui.fragments.AppFragmentsFactory;
import fr.ralala.worktime.ui.fragments.WorkTimeFragment;
import fr.ralala.worktime.ui.utils.SwipeDetector;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.utils.DefaultExceptionHandler;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Main activity, manage the navigation view
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, ComponentCallbacks2 {
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private boolean mViewIsAtHome = false;
  private MainApplication mApp = null;
  private NavigationView mNavigationView = null;
  private SwipeDetector mSwipeDetector = null;
  private DrawerLayout mDrawer = null;
  private AppFragmentsFactory mFragmentsFactory = null;
  private AlertDialog mProgress;
  private boolean mLockedByProgress = false;
  private boolean mResumeFromActivity = false;
  private LauncherDayActivity mLauncherDayActivity;
  private LauncherPublicHolidayActivity mLauncherPublicHolidayActivity;

  /**
   * Called when the activity is created.
   *
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DefaultExceptionHandler.install(this);
    setContentView(R.layout.activity_main);

    Intent intent = getIntent();
    if (intent.getExtras() != null && intent.hasExtra(AndroidHelper.EXTRA_RESTART)) {
      String msg = intent.getExtras().getString(AndroidHelper.EXTRA_RESTART);
      UIHelper.toast(this, msg);
    }
    mProgress = UIHelper.showCircularProgressDialog(this);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    mSwipeDetector = new SwipeDetector(this);
    mDrawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    mDrawer.addDrawerListener(toggle);
    toggle.syncState();

    mNavigationView = findViewById(R.id.nav_view);
    if (mNavigationView != null) {
      mNavigationView.setNavigationItemSelectedListener(this);
      mNavigationView.getMenu().getItem(0).setChecked(true);
    }
    mApp = (MainApplication) getApplication();
    mFragmentsFactory = new AppFragmentsFactory(mApp, mNavigationView);

    /* permissions */
    String[] permissions = new String[]{
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.INTERNET,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.VIBRATE,
    };
    boolean perms = true;
    for (String perm : permissions)
      perms &= ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    if (!perms)
      ActivityCompat.requestPermissions(this, permissions, 1);
    ChangeLog changeLog = mApp.getChangeLog();
    if (changeLog.firstRun())
      changeLog.getLogDialog(this).show();

    mLauncherDayActivity = new LauncherDayActivity(this);
    mLauncherPublicHolidayActivity = new LauncherPublicHolidayActivity(this);
    getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        back();
      }
    });
  }

  private void back() {
    if (mViewIsAtHome)
      mDrawer.openDrawer(GravityCompat.START);
    else { //if the current view is not the News fragment
      displayView(mFragmentsFactory.getDefaultHomeId()); //display the home fragment
      mNavigationView.getMenu().getItem(mFragmentsFactory.getDefaultHomeIndex()).setChecked(true); /* select home title */
      return;
    }
    if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
      cleanup();
      finish();
      return;
    } else {
      UIHelper.toast(this, R.string.on_double_back_exit_text);
    }
    updateLastBackPressed();
  }

  private static void updateLastBackPressed() {
    mLastBackPressed = System.currentTimeMillis();
  }

  public LauncherPublicHolidayActivity getLauncherPublicHolidayActivity() {
    return mLauncherPublicHolidayActivity;
  }

  public LauncherDayActivity getLauncherDayActivity() {
    return mLauncherDayActivity;
  }

  /**
   * Called to create the option menu.
   *
   * @param menu The main menu.
   * @return boolean
   */
  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.activity_main, menu);
    return true;
  }


  /**
   * Called when the user select an option menu item.
   *
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    final int id = item.getItemId();
    if (id == R.id.action_last_export) {
      if (mApp.getLastExportType().equals(MainApplication.PREFS_VAL_LAST_EXPORT_DROPBOX)) {
        AndroidHelper.exportDropbox(mApp, this);
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  /**
   * Show the progress dialog.
   */
  public void progressShow(boolean locked) {
    mLockedByProgress = locked;
    if (!mProgress.isShowing()) {
      mProgress.show();
      Window window = mProgress.getWindow();
      if (window != null) {
        window.setLayout(350, 350);
        View v = window.getDecorView();
        v.setBackgroundResource(R.drawable.rounded_border);
      }
    }
  }

  /**
   * Dismiss the progress dialog.
   */
  public void progressDismiss() {
    mLockedByProgress = false;
    if (mProgress.isShowing()) {
      mProgress.dismiss();
    }
  }

  /**
   * Returns the instance of the SwipeDetector.
   *
   * @return SwipeDetector
   */
  public SwipeDetector getSwipeDetector() {
    return mSwipeDetector;
  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    MainApplication.incResumedCounter();
    if (mFragmentsFactory.isRequiresProgress() && !mResumeFromActivity && MainApplication.getResumedCounter() == 1)
      progressShow(true);
    new Thread(() -> {
      /* load SQL */
      if (!mApp.openSql(this)) finish();
      runOnUiThread(() -> {
        displayView(mFragmentsFactory.getCurrentFragmentId());
        if (mApp.getLastWidgetOpen() != 0L) {
          long elapsed = System.currentTimeMillis() - mApp.getLastWidgetOpen();
          if (elapsed <= 500) {
            mApp.setLastWidgetOpen(0L);
            progressDismiss();
            finish();
            return;
          }
        }
        AndroidHelper.killServiceIfRunning(this, AutoExportService.class);
        mFragmentsFactory.onResume(this);
        if (!mLockedByProgress && mProgress.isShowing())
          mProgress.dismiss();
      });

    }).start();
    setResumeFromActivity(false);
  }

  /**
   * Sets resume from activity state.
   *
   * @param resumeFromActivity The new state.
   */
  public void setResumeFromActivity(boolean resumeFromActivity) {
    mResumeFromActivity = resumeFromActivity;
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onDestroy() {
    cleanup();
    super.onDestroy();
  }

  /**
   * Cleanup the resources.
   */
  private void cleanup() {
    if (mApp != null) {
      if (mProgress.isShowing())
        mProgress.dismiss();
      if (mApp.isExportAutoSave()) {
        startService(new Intent(this, AutoExportService.class));
      }
      mApp.getSql().close();
    }
  }

  /**
   * Called when an item is selected in the navigation view.
   *
   * @param item The selected item.
   * @return boolean
   */
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    displayView(item.getItemId());
    return true;
  }

  /**
   * Dispatch the touch event.
   *
   * @param ev The event.
   * @return false if the event is not consumed.
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    boolean b = super.dispatchTouchEvent(ev);
    return mFragmentsFactory.getCurrentFragment() instanceof WorkTimeFragment ? mSwipeDetector.onTouchEvent(ev) : b;
  }

  /**
   * Displays a specific view.
   *
   * @param viewId The view id to display.
   */
  public void displayView(int viewId) {
    String title = getString(R.string.app_title);

    mViewIsAtHome = mFragmentsFactory.getDefaultHomeId() == viewId;

    if (viewId == R.id.nav_profile) {
      mFragmentsFactory.setCurrentToFragment(AppFragmentsFactory.IDX_PROFILE);
      title = getString(R.string.profile);
    } else if (viewId == R.id.nav_public_holidays) {
      mFragmentsFactory.setCurrentToFragment(AppFragmentsFactory.IDX_PUBLIC_HOLIDAY);
      title = getString(R.string.public_holidays);
    } else if (viewId == R.id.nav_export) {
      mFragmentsFactory.setCurrentToFragment(AppFragmentsFactory.IDX_EXPORT);
      title = getString(R.string.export);
    } else if (viewId == R.id.nav_statistics) {
      mFragmentsFactory.setCurrentToFragment(AppFragmentsFactory.IDX_STATISTICS);
      title = getString(R.string.statistics);
    } else if (viewId == R.id.nav_worktime) {
      mFragmentsFactory.setCurrentToFragment(AppFragmentsFactory.IDX_WORK_TIME);
      title = getString(R.string.work_time);
    } else if (viewId == R.id.nav_settings) {
      setResumeFromActivity(true);
      mFragmentsFactory.setCurrentToFragment(-1);
      startActivity(new Intent(this, SettingsActivity.class));
      if (mDrawer != null) mDrawer.closeDrawer(GravityCompat.START);
      return;
    } else if (viewId == R.id.nav_exit) {
      cleanup();
      back();
      Process.killProcess(android.os.Process.myPid());
      return;
    } else {
      mFragmentsFactory.setCurrentToFragment(-1);
    }

    if (mFragmentsFactory.getCurrentFragment() != null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      ft.replace(R.id.content_frame, mFragmentsFactory.getCurrentFragment());
      ft.commit();
    }

    // set the toolbar title
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }

    if (mDrawer != null) mDrawer.closeDrawer(GravityCompat.START);

  }
}
