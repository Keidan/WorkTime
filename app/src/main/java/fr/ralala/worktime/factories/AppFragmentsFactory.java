package fr.ralala.worktime.factories;

import android.content.res.Configuration;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.fragments.ChartsFragment;
import fr.ralala.worktime.fragments.ExportFragment;
import fr.ralala.worktime.fragments.ProfileFragment;
import fr.ralala.worktime.fragments.PublicHolidaysFragment;
import fr.ralala.worktime.fragments.QuickAccessFragment;
import fr.ralala.worktime.fragments.WorkTimeFragment;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the application fragments
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class AppFragmentsFactory {
  public static final int IDX_WORK_TIME = 1;
  public static final int IDX_QUICK_ACCESS = 0;
  public static final int IDX_PROFILE = 2;
  public static final int IDX_PUBLIC_HOLIDAY = 3;
  public static final int IDX_EXPORT = 4;
  public static final int IDX_CHARTS = 6;
  public static final int IDX_EXIT = 7;
  private Fragment quickAccessFragment = null;
  private Fragment profileFragment = null;
  private Fragment publicHolidaysFragment = null;
  private Fragment exportFragment = null;
  private Fragment workTimeFragment = null;
  private Fragment chartsFragment = null;
  private Fragment currentFragment = null;
  private MainApplication app = null;
  private NavigationView navigationView = null;

  public AppFragmentsFactory(final MainApplication app, final NavigationView navigationView) {
    this.app = app;
    this.navigationView = navigationView;
    if(quickAccessFragment == null)
      quickAccessFragment = new QuickAccessFragment();
    if(profileFragment == null)
      profileFragment = new ProfileFragment();
    if(publicHolidaysFragment == null)
      publicHolidaysFragment = new PublicHolidaysFragment();
    if(exportFragment == null)
      exportFragment = new ExportFragment();
    if(chartsFragment == null)
      chartsFragment = new ChartsFragment();
    if(workTimeFragment == null)
      workTimeFragment = new WorkTimeFragment();
    currentFragment = workTimeFragment;
  }

  public int getDefaultHomeIndex() {
    return app.getDefaultHome();
  }

  public int getDefaultHomeId() {
    switch(app.getDefaultHome()) {
      case IDX_PROFILE:
        return (R.id.nav_profile);
      case IDX_PUBLIC_HOLIDAY:
        return (R.id.nav_public_holidays);
      case IDX_EXPORT:
        return (R.id.nav_export);
      case IDX_CHARTS:
        return (R.id.nav_charts);
      case IDX_WORK_TIME:
        return (R.id.nav_worktime);
      case IDX_QUICK_ACCESS:
      default:
        return (R.id.nav_quickaccess);
    }
  }

  public Fragment getDefaultHomeView() {
    switch(app.getDefaultHome()) {
      case IDX_QUICK_ACCESS:
        return quickAccessFragment;
      case IDX_PROFILE:
        return profileFragment;
      case IDX_PUBLIC_HOLIDAY:
        return publicHolidaysFragment;
      case IDX_EXPORT:
        return exportFragment;
      case IDX_CHARTS:
        return chartsFragment;
      case IDX_WORK_TIME:
      default:
        return workTimeFragment;
    }
  }

  public boolean consumeBackPressed() {
    Fragment fragment = currentFragment;
    if(fragment != null && ChartsFragment.class.isInstance(fragment)) {
      return ((ChartsFragment)fragment).consumeBackPressed();
    }
    return false;
  }

  public void onResume(final AppCompatActivity aca) {
    navigationView.getMenu().getItem(IDX_EXIT).setVisible(!app.isHideExitButton());
    Fragment fragment = currentFragment;
    if(fragment != null) {
      final FragmentTransaction ft = aca.getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      ft.replace(R.id.content_frame, fragment);
      ft.commit();
      if(QuickAccessFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_QUICK_ACCESS).setChecked(true); /* select quick access title */
      else if(ProfileFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_PROFILE).setChecked(true); /* select profile title */
      else if(PublicHolidaysFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_PUBLIC_HOLIDAY).setChecked(true); /* select public holidays title */
      else if(ExportFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_EXPORT).setChecked(true); /* select export title */
      else if(WorkTimeFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_WORK_TIME).setChecked(true); /* select work title */
      else if(ChartsFragment.class.isInstance(fragment))
        navigationView.getMenu().getItem(IDX_CHARTS).setChecked(true); /* select charts title */
    }
  }

  public Fragment getCurrentFragment() {
    return currentFragment;
  }

  public void setCurrentToFragment(int idx) {
    switch (idx) {
      case IDX_QUICK_ACCESS:
        currentFragment = quickAccessFragment;
        break;
      case IDX_PROFILE:
        currentFragment = profileFragment;
        break;
      case IDX_PUBLIC_HOLIDAY:
        currentFragment = publicHolidaysFragment;
        break;
      case IDX_EXPORT:
        currentFragment = exportFragment;
        break;
      case IDX_CHARTS:
        currentFragment = chartsFragment;
        break;
      case IDX_WORK_TIME:
        currentFragment = workTimeFragment;
        break;
      default:
        currentFragment = getDefaultHomeView();
        break;
    }
  }
}
