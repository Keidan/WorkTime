package fr.ralala.worktime.ui.activities;


import android.Manifest;
import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;


import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.activities.settings.SettingsActivity;
import fr.ralala.worktime.ui.changelog.ChangeLog;
import fr.ralala.worktime.ui.changelog.ChangeLogIds;
import fr.ralala.worktime.ui.fragments.AppFragmentsFactory;
import fr.ralala.worktime.ui.fragments.WorkTimeFragment;
import fr.ralala.worktime.services.DropboxAutoExportService;
import fr.ralala.worktime.services.QuickAccessService;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.ui.utils.SwipeDetector;
import fr.ralala.worktime.ui.utils.UIHelper;
import fr.ralala.worktime.utils.DefaultExceptionHandler;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Main activity, manage the navigation view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MainActivity extends RuntimePermissionsActivity implements NavigationView.OnNavigationItemSelectedListener, ComponentCallbacks2 {
  private static final int PERMISSIONS_REQUEST = 30;
  private static final int BACK_TIME_DELAY = 2000;
  private static long mLastBackPressed = -1;
  private boolean mViewIsAtHome = false;
  private MainApplication mApp = null;
  private NavigationView mNavigationView = null;
  private SwipeDetector mSwipeDetector = null;
  private DrawerLayout mDrawer = null;
  private AppFragmentsFactory mFragments = null;
  private AlertDialog mProgress;
  private boolean mLockedByProgress = false;
  private boolean mResumeFromActivity = false;
  private Vibrator mVibrator;

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    DefaultExceptionHandler.install(this);
    setContentView(R.layout.activity_main);

    mVibrator = ((Vibrator) getSystemService(VIBRATOR_SERVICE));
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
    if(mNavigationView != null) {
      mNavigationView.setNavigationItemSelectedListener(this);
      mNavigationView.getMenu().getItem(0).setChecked(true);
    }
    mApp = MainApplication.getInstance();
    mFragments = new AppFragmentsFactory(mApp, mNavigationView);

    String[] perms = new String[]{
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.INTERNET,
      Manifest.permission.VIBRATE,
      Manifest.permission.ACCESS_FINE_LOCATION,
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.VIBRATE
    };
    super.requestAppPermissions(perms, R.string.permissions_read_ext_storage , PERMISSIONS_REQUEST);
    ChangeLog changeLog = new ChangeLog(
      new ChangeLogIds(
        R.raw.changelog,
        R.string.changelog_ok_button,
        R.string.background_color,
        R.string.changelog_title,
        R.string.changelog_full_title,
        R.string.changelog_show_full), this);
    if(changeLog.firstRun())
      changeLog.getLogDialog().show();
  }

  /**
   * Returns the instance of the vibrator service.
   * @return Vibrator
   */
  public Vibrator getVibrator() {
    return mVibrator;
  }

  /**
   * Show the progress dialog.
   */
  public void progressShow(boolean locked) {
    mLockedByProgress = locked;
    if(!mProgress.isShowing()) {
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
    if(mProgress.isShowing()) {
      mProgress.dismiss();
    }
  }

  /**
   * Returns the instance of the SwipeDetector.
   * @return SwipeDetector
   */
  public SwipeDetector getSwipeDetector() {
    return mSwipeDetector;
  }

  /**
   * Called when the permissions are granted.
   * @param requestCode The request code.
   */
  @Override
  public void onPermissionsGranted(final int requestCode) {
    //AndroidHelper.toastLong(this, R.string.permissions_read_ext_storage_done);

  }

  /**
   * Called when the activity is resumed.
   */
  @Override
  public void onResume() {
    super.onResume();
    mApp.incResumedCounter();
    if(mFragments.isRequiresProgress() && !mResumeFromActivity && mApp.getResumedCounter() == 1)
      progressShow(true);
    new Thread(() -> {
     /* load SQL */
      if(!mApp.openSql(this)) finish();
      /*if(mApp.isExportAutoSave())
        mApp.initOnLoadTables();*/
      runOnUiThread(() -> {
        displayView(mFragments.getCurrentFragmentId());
        if(mApp.getLastWidgetOpen() != 0L) {
          long elapsed = System.currentTimeMillis() - mApp.getLastWidgetOpen();
          if(elapsed <= 500) {
            mApp.setLastWidgetOpen(0L);
            progressDismiss();
            finish();
            return;
          }
        }
        AndroidHelper.killServiceIfRunning(this, DropboxAutoExportService.class);
        mFragments.onResume(this);
        if(!mLockedByProgress && mProgress.isShowing())
          mProgress.dismiss();
      });

    }).start();
    setResumeFromActivity(false);
  }

  /**
   * Sets resume from activity state.
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
    if(mApp != null) {
      if(mProgress.isShowing())
        mProgress.dismiss();
      if(mApp.isExportAutoSave()) {
        startService(new Intent(this, DropboxAutoExportService.class));
      }
      mApp.getSql().close();
      mApp.getQuickAccessNotification().remove(this);
    }
    AndroidHelper.killServiceIfRunning(this, QuickAccessService.class);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if(!mFragments.consumeBackPressed()) {
      if (mViewIsAtHome)
        mDrawer.openDrawer(Gravity.START);
      else { //if the current view is not the News fragment
        displayView(mFragments.getDefaultHomeId()); //display the home fragment
        mNavigationView.getMenu().getItem(mFragments.getDefaultHomeIndex()).setChecked(true); /* select home title */
        return;
      }
      if (mLastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        cleanup();
        super.onBackPressed();
        //Process.killProcess(android.os.Process.myPid());
        return;
      } else {
        UIHelper.toast(this, R.string.on_double_back_exit_text);
      }
      mLastBackPressed = System.currentTimeMillis();
    }
  }

  /**
   * Called when an item is selected in the navigation view.
   * @param item The selected item.
   * @return boolean
   */
  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(@NonNull MenuItem item) {
    displayView(item.getItemId());
    return true;
  }

  /**
   * Dispatch the touch event.
   * @param ev The event.
   * @return false if the event is not consumed.
   */
  @Override
  public boolean dispatchTouchEvent(MotionEvent ev){
    boolean b = super.dispatchTouchEvent(ev);
    return WorkTimeFragment.class.isInstance(mFragments.getCurrentFragment()) ? mSwipeDetector.onTouchEvent(ev) : b;
  }

  /**
   * Displays a specific view.
   * @param viewId The view id to display.
   */
  public void displayView(int viewId) {
    String title = getString(R.string.app_title);

    mViewIsAtHome = mFragments.getDefaultHomeId() == viewId;
    switch (viewId) {
      case R.id.nav_quickaccess:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_QUICK_ACCESS);
        title = getString(R.string.quickaccess);
        break;
      case R.id.nav_profile:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_PROFILE);
        title  = getString(R.string.profile);
        break;
      case R.id.nav_public_holidays:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_PUBLIC_HOLIDAY);
        title = getString(R.string.public_holidays);
        break;
      case R.id.nav_export:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_EXPORT);
        title = getString(R.string.export);
        break;
      case R.id.nav_statistics:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_STATISTICS);
        title = getString(R.string.statistics);
        break;
      case R.id.nav_worktime:
        mFragments.setCurrentToFragment(AppFragmentsFactory.IDX_WORK_TIME);
        title = getString(R.string.work_time);
        break;
      case R.id.nav_settings:
        setResumeFromActivity(true);
        mFragments.setCurrentToFragment(-1);
        startActivity(new Intent(this, SettingsActivity.class));
        if(mDrawer != null) mDrawer.closeDrawer(GravityCompat.START);
        return;
      case R.id.nav_exit:
        cleanup();
        super.onBackPressed();
        Process.killProcess(android.os.Process.myPid());
        return;
      default:
        mFragments.setCurrentToFragment(-1);
        break;

    }

    if (mFragments.getCurrentFragment() != null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      ft.replace(R.id.content_frame, mFragments.getCurrentFragment());
      ft.commit();
    }

    // set the toolbar title
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }

    if(mDrawer != null) mDrawer.closeDrawer(GravityCompat.START);

  }
}
