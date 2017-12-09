package fr.ralala.worktime.ui.activities;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;

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

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Main activity, manage the navigation view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MainActivity extends RuntimePermissionsActivity implements NavigationView.OnNavigationItemSelectedListener {
  private static final int PERMISSIONS_REQUEST = 30;
  private static final int BACK_TIME_DELAY = 2000;
  private static long lastBackPressed = -1;
  private boolean viewIsAtHome = false;
  private MainApplication app = null;
  private NavigationView navigationView = null;
  private SwipeDetector swipeDetector = null;
  private DrawerLayout drawer = null;
  private AppFragmentsFactory fragments = null;

  /**
   * Called when the activity is created.
   * @param savedInstanceState The saved instance state.
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    swipeDetector = new SwipeDetector(this);
    drawer = findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.addDrawerListener(toggle);
    toggle.syncState();

    navigationView = findViewById(R.id.nav_view);
    if(navigationView != null) {
      navigationView.setNavigationItemSelectedListener(this);
      navigationView.getMenu().getItem(0).setChecked(true);
    }
    app = MainApplication.getApp(this);
    fragments = new AppFragmentsFactory(app, navigationView);
    displayView(fragments.getDefaultHomeId());
    /* load SQL */
    if(!app.openSql(this)) finish();

    String[] perms = new String[]{
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.INTERNET,
      Manifest.permission.VIBRATE,
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
   * Returns the instance of the SwipeDetector.
   * @return SwipeDetector
   */
  public SwipeDetector getSwipeDetector() {
    return swipeDetector;
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
    if(app.getLastWidgetOpen() != 0L) {
      long elapsed = System.currentTimeMillis() - app.getLastWidgetOpen();
      if(elapsed <= 500) {
        app.setLastWidgetOpen(0L);
        finish();
        return;
      }
    }
    AndroidHelper.killServiceIfRunning(this, DropboxAutoExportService.class);
    fragments.onResume(this);
    if(app.isExportAutoSave())
      app.initOnLoadTables();
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
    if(app != null) {
      if(app.isExportAutoSave()) {
        startService(new Intent(this, DropboxAutoExportService.class));
      }
      app.getSql().close();
      app.getQuickAccessNotification().remove(this);
    }
    AndroidHelper.killServiceIfRunning(this, QuickAccessService.class);
  }

  /**
   * Called to handle the click on the back button.
   */
  @Override
  public void onBackPressed() {
    if(!fragments.consumeBackPressed()) {
      if (viewIsAtHome)
        drawer.openDrawer(Gravity.START);
      else { //if the current view is not the News fragment
        displayView(fragments.getDefaultHomeId()); //display the home fragment
        navigationView.getMenu().getItem(fragments.getDefaultHomeIndex()).setChecked(true); /* select home title */
        return;
      }
      if (lastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        cleanup();
        super.onBackPressed();
        //Process.killProcess(android.os.Process.myPid());
        return;
      } else {
        UIHelper.toast(this, R.string.on_double_back_exit_text);
      }
      lastBackPressed = System.currentTimeMillis();
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
    return WorkTimeFragment.class.isInstance(fragments.getCurrentFragment()) ? swipeDetector.onTouchEvent(ev) : b;
  }

  /**
   * Displays a specific view.
   * @param viewId The view id to display.
   */
  public void displayView(int viewId) {
    String title = getString(R.string.app_title);

    viewIsAtHome = fragments.getDefaultHomeId() == viewId;
    switch (viewId) {
      case R.id.nav_quickaccess:
        fragments.setCurrentToFragment(AppFragmentsFactory.IDX_QUICK_ACCESS);
        title = getString(R.string.quickaccess);
        break;
      case R.id.nav_profile:
        fragments.setCurrentToFragment(AppFragmentsFactory.IDX_PROFILE);
        title  = getString(R.string.profile);
        break;
      case R.id.nav_public_holidays:
        fragments.setCurrentToFragment(AppFragmentsFactory.IDX_PUBLIC_HOLIDAY);
        title = getString(R.string.public_holidays);
        break;
      case R.id.nav_export:
        fragments.setCurrentToFragment(AppFragmentsFactory.IDX_EXPORT);
        title = getString(R.string.export);
        break;
      case R.id.nav_statistics:
        fragments.setCurrentToFragment(AppFragmentsFactory.IDX_STATISTICS);
        title = getString(R.string.statistics);
        break;
      case R.id.nav_worktime:
        fragments.setCurrentToFragment(AppFragmentsFactory.IDX_WORK_TIME);
        title = getString(R.string.work_time);
        break;
      case R.id.nav_settings:
        app.setResumeAfterActivity(true);
        fragments.setCurrentToFragment(-1);
        startActivity(new Intent(this, SettingsActivity.class));
        if(drawer != null) drawer.closeDrawer(GravityCompat.START);
        return;
      case R.id.nav_exit:
        cleanup();
        super.onBackPressed();
        Process.killProcess(android.os.Process.myPid());
        return;
      default:
        fragments.setCurrentToFragment(-1);
        break;

    }

    if (fragments.getCurrentFragment() != null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      ft.replace(R.id.content_frame, fragments.getCurrentFragment());
      ft.commit();
    }

    // set the toolbar title
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }

    if(drawer != null) drawer.closeDrawer(GravityCompat.START);

  }
}
