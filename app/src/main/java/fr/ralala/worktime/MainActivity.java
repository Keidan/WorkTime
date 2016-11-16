package fr.ralala.worktime;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Process;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.worktime.fragments.ExportFragment;
import fr.ralala.worktime.fragments.MainFragment;
import fr.ralala.worktime.fragments.ProfileFragment;
import fr.ralala.worktime.fragments.PublicHolidaysFragment;
import fr.ralala.worktime.prefs.SettingsActivity;

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
  private Fragment fragment = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
      this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawer.setDrawerListener(toggle);
    toggle.syncState();

    navigationView = (NavigationView) findViewById(R.id.nav_view);
    navigationView.setNavigationItemSelectedListener(this);
    navigationView.getMenu().getItem(0).setChecked(true);

    displayView(R.id.nav_home);
    /* load SQL */
    app = MainApplication.getApp(this);
    if(!app.openSql(drawer)) finish();

    super.requestAppPermissions(new
        String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE}, R.string.permissions_read_ext_storage , PERMISSIONS_REQUEST);
  }

  @Override
  public void onPermissionsGranted(final int requestCode) {
    //AndroidHelper.toast_long(this, R.string.permissions_read_ext_storage_done);
  }

  public void onResume() {
    super.onResume();
    if(fragment != null) {
      final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
      ft.detach(fragment);
      ft.attach(fragment);
      ft.commit();
      if(ProfileFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(1).setChecked(true); /* select profile title */
      else if(PublicHolidaysFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(2).setChecked(true); /* select public holidays title */
      else if(ExportFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(3).setChecked(true); /* select export title */

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
      displayView(R.id.nav_home); //display the home fragment
      navigationView.getMenu().getItem(0).setChecked(true); /* select home title */
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

  public void displayView(int viewId) {
    String title = getString(R.string.app_title);

    switch (viewId) {
      case R.id.nav_profile:
        fragment = new ProfileFragment();
        title  = getString(R.string.profile);
        viewIsAtHome = false;
        break;
      case R.id.nav_public_holidays:
        fragment = new PublicHolidaysFragment();
        title = getString(R.string.public_holidays);
        viewIsAtHome = false;
        break;
      case R.id.nav_export:
        fragment = new ExportFragment();
        title = getString(R.string.export);
        viewIsAtHome = false;
        break;
      case R.id.nav_settings:
        viewIsAtHome = false;
        startActivity(new Intent(this, SettingsActivity.class));
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if(drawer != null) drawer.closeDrawer(GravityCompat.START);
        return;
      case R.id.nav_home:
      default:
        fragment = new MainFragment();
        viewIsAtHome = true;
        break;

    }

    if (fragment != null) {
      FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
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
