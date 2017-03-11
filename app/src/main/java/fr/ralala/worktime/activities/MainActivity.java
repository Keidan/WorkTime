package fr.ralala.worktime.activities;


import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.changelog.ChangeLog;
import fr.ralala.worktime.changelog.ChangeLogIds;
import fr.ralala.worktime.fragments.ExportFragment;
import fr.ralala.worktime.fragments.MainFragment;
import fr.ralala.worktime.fragments.ProfileFragment;
import fr.ralala.worktime.fragments.PublicHolidaysFragment;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.utils.SwipeDetector;

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
  private static final int IDX_HOME = 0;
  private static final int IDX_PROFILE = 1;
  private static final int IDX_PUBLIC_HOLYDAY = 2;
  private static final int IDX_EXPORT = 3;
  private static final int PERMISSIONS_REQUEST = 30;
  private static final int BACK_TIME_DELAY = 2000;
  private static long lastBackPressed = -1;
  private boolean viewIsAtHome = false;
  private MainApplication app = null;
  private NavigationView navigationView = null;
  private Fragment fragment = null;
  private SwipeDetector swipeDetector = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    swipeDetector = new SwipeDetector(this);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    navigationView.getMenu().getItem(0).setChecked(true);

    app = MainApplication.getApp(this);
    displayView(getDefaultHome());
    /* load SQL */
    if(!app.openSql(this)) finish();

    String[] perms = new String[]{
      Manifest.permission.READ_EXTERNAL_STORAGE,
      Manifest.permission.WRITE_EXTERNAL_STORAGE,
      Manifest.permission.INTERNET,
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

  private int getDefaultHome() {
    switch(app.getDefaultHome()) {
      case 1:
        return (R.id.nav_profile);
      case 2:
        return (R.id.nav_public_holidays);
      case 3:
        return (R.id.nav_export);
      case 0:
      default:
        return (R.id.nav_home);
    }
  }

  public SwipeDetector getSwipeDetector() {
    return swipeDetector;
  }

  @Override
  public void onPermissionsGranted(final int requestCode) {
    //AndroidHelper.toast_long(this, R.string.permissions_read_ext_storage_done);

  }

  public void onResume() {
    super.onResume();
    if(fragment != null) {
      final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      ft.detach(fragment);
      ft.attach(fragment);
      ft.commit();
      if(ProfileFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_PROFILE).setChecked(true); /* select profile title */
      else if(PublicHolidaysFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_PUBLIC_HOLYDAY).setChecked(true); /* select public holidays title */
      else if(ExportFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_EXPORT).setChecked(true); /* select export title */
      else if(MainFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_HOME).setChecked(true); /* select home title */

    }
  }

  public void onDestroy() {
    if(app != null) app.getSql().close();
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if (drawer.isDrawerOpen(GravityCompat.START)) {
      drawer.closeDrawer(GravityCompat.START);
    }
    if (!viewIsAtHome) { //if the current view is not the News fragment
      displayView(getDefaultHome()); //display the home fragment
      navigationView.getMenu().getItem(IDX_HOME).setChecked(true); /* select home title */
    } else {
      //moveTaskToBack(true);  //If view is in News fragment, exit application
      if (lastBackPressed + BACK_TIME_DELAY > System.currentTimeMillis()) {
        super.onBackPressed();
        Process.killProcess(android.os.Process.myPid());
      } else {
        AndroidHelper.toast(this, R.string.on_double_back_exit_text);
      }
      lastBackPressed = System.currentTimeMillis();
    }
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    displayView(item.getItemId());
    return true;
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev){
    super.dispatchTouchEvent(ev);
    return swipeDetector.onTouchEvent(ev);
  }

  public void displayView(int viewId) {
    String title = getString(R.string.app_title);

    viewIsAtHome = getDefaultHome() == viewId ? true : false;
    switch (viewId) {
      case R.id.nav_profile:
        fragment = new ProfileFragment();
        title  = getString(R.string.profile);
        break;
      case R.id.nav_public_holidays:
        fragment = new PublicHolidaysFragment();
        title = getString(R.string.public_holidays);
        break;
      case R.id.nav_export:
        fragment = new ExportFragment();
        title = getString(R.string.export);
        break;
      case R.id.nav_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null) drawer.closeDrawer(GravityCompat.START);
        return;
      case R.id.nav_home:
      default:
        fragment = new MainFragment();
        break;

    }

    if (fragment != null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);

      ft.replace(R.id.content_frame, fragment);
      ft.commit();
    }

    // set the toolbar title
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    if(drawer != null) drawer.closeDrawer(GravityCompat.START);

  }
}
