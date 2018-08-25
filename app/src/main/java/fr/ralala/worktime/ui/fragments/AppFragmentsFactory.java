package fr.ralala.worktime.ui.fragments;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;

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
  public static final int IDX_STATISTICS = 6;
  private static final int IDX_EXIT = 7;
  private Fragment mQuickAccessFragment = null;
  private Fragment mProfileFragment = null;
  private Fragment mPublicHolidaysFragment = null;
  private Fragment mExportFragment = null;
  private Fragment mWorkTimeFragment = null;
  private Fragment mStatisticsFragment = null;
  private Fragment mCurrentFragment;
  private MainApplication mApp;
  private NavigationView mNavigationView;

  /**
   * Creates the fragments factory.
   * @param app The application context.
   * @param navigationView The application navigation view.
   */
  public AppFragmentsFactory(final MainApplication app, final NavigationView navigationView) {
    mApp = app;
    mNavigationView = navigationView;
    if(mQuickAccessFragment == null)
      mQuickAccessFragment = new QuickAccessFragment();
    if(mProfileFragment == null)
      mProfileFragment = new ProfileFragment();
    if(mPublicHolidaysFragment == null)
      mPublicHolidaysFragment = new PublicHolidaysFragment();
    if(mExportFragment == null)
      mExportFragment = new ExportFragment();
    if(mStatisticsFragment == null)
      mStatisticsFragment = new StatisticsFragment();
    if(mWorkTimeFragment == null)
      mWorkTimeFragment = new WorkTimeFragment();
    mCurrentFragment = mWorkTimeFragment;
  }

  /**
   * Tests whether the current fragment requires the progress dialog.
   * @return boolean
   */
  public boolean isRequiresProgress() {
    return mCurrentFragment != null &&
        (mCurrentFragment.equals(mWorkTimeFragment) ||
          mCurrentFragment.equals(mProfileFragment) ||
            mCurrentFragment.equals(mPublicHolidaysFragment) ||
            mCurrentFragment.equals(mExportFragment));
  }

  /**
   * Returns the default home index.
   * @return int
   */
  public int getDefaultHomeIndex() {
    return mApp.getDefaultHome();
  }


  /**
   * Returns the current fragment ID (android).
   * @return int
   */
  public int getCurrentFragmentId() {
    if(mCurrentFragment == null)
      return getDefaultHomeId();
    else if(mCurrentFragment.equals(mProfileFragment))
      return (R.id.nav_profile);
    else if(mCurrentFragment.equals(mPublicHolidaysFragment))
      return (R.id.nav_public_holidays);
    else if(mCurrentFragment.equals(mExportFragment))
      return (R.id.nav_export);
    else if(mCurrentFragment.equals(mStatisticsFragment))
      return (R.id.nav_statistics);
    else if(mCurrentFragment.equals(mWorkTimeFragment))
      return (R.id.nav_worktime);
    else
      return (R.id.nav_quickaccess);
  }

  /**
   * Returns the default home ID (android).
   * @return int
   */
  public int getDefaultHomeId() {
    switch(mApp.getDefaultHome()) {
      case IDX_PROFILE:
        return (R.id.nav_profile);
      case IDX_PUBLIC_HOLIDAY:
        return (R.id.nav_public_holidays);
      case IDX_EXPORT:
        return (R.id.nav_export);
      case IDX_STATISTICS:
        return (R.id.nav_statistics);
      case IDX_WORK_TIME:
        return (R.id.nav_worktime);
      case IDX_QUICK_ACCESS:
      default:
        return (R.id.nav_quickaccess);
    }
  }

  /**
   * Returns the default home fragment.
   * @return Fragment
   */
  private Fragment getDefaultHomeView() {
    switch(mApp.getDefaultHome()) {
      case IDX_QUICK_ACCESS:
        return mQuickAccessFragment;
      case IDX_PROFILE:
        return mProfileFragment;
      case IDX_PUBLIC_HOLIDAY:
        return mPublicHolidaysFragment;
      case IDX_EXPORT:
        return mExportFragment;
      case IDX_STATISTICS:
        return mStatisticsFragment;
      case IDX_WORK_TIME:
      default:
        return mWorkTimeFragment;
    }
  }

  /**
   * Called when the backPressed event is caught in the main activity.
   * @return true if the backPressed is consumed by a fragment.
   */
  public boolean consumeBackPressed() {
    Fragment fragment = mCurrentFragment;
    return fragment != null && StatisticsFragment.class.isInstance(fragment) && ((StatisticsFragment)fragment).consumeBackPressed();
  }

  /**
   * Called when the activity is resumed.
   * @param aca The main activity.
   */
  public void onResume(final AppCompatActivity aca) {
    mNavigationView.getMenu().getItem(IDX_EXIT).setVisible(!mApp.isHideExitButton());
    Fragment fragment = mCurrentFragment;
    if(fragment != null) {
      final FragmentTransaction ft = aca.getSupportFragmentManager().beginTransaction();
      ft.setCustomAnimations(R.anim.fadein, R.anim.fadeout);
      ft.replace(R.id.content_frame, fragment);
      ft.commit();
      if(QuickAccessFragment.class.isInstance(fragment))
        mNavigationView.getMenu().getItem(IDX_QUICK_ACCESS).setChecked(true); /* select quick access title */
      else if(ProfileFragment.class.isInstance(fragment))
        mNavigationView.getMenu().getItem(IDX_PROFILE).setChecked(true); /* select profile title */
      else if(PublicHolidaysFragment.class.isInstance(fragment))
        mNavigationView.getMenu().getItem(IDX_PUBLIC_HOLIDAY).setChecked(true); /* select public holidays title */
      else if(ExportFragment.class.isInstance(fragment))
        mNavigationView.getMenu().getItem(IDX_EXPORT).setChecked(true); /* select export title */
      else if(WorkTimeFragment.class.isInstance(fragment))
        mNavigationView.getMenu().getItem(IDX_WORK_TIME).setChecked(true); /* select work title */
      else if(StatisticsFragment.class.isInstance(fragment))
        mNavigationView.getMenu().getItem(IDX_STATISTICS).setChecked(true); /* select charts title */
    }
  }

  /**
   * Return the current fragment.
   * @return Fragment.
   */
  public Fragment getCurrentFragment() {
    return mCurrentFragment;
  }

  /**
   * Changes the current fragment based on its index in the navigation view.
   * @param idx The index.
   */
  public void setCurrentToFragment(int idx) {
    switch (idx) {
      case IDX_QUICK_ACCESS:
        mCurrentFragment = mQuickAccessFragment;
        break;
      case IDX_PROFILE:
        mCurrentFragment = mProfileFragment;
        break;
      case IDX_PUBLIC_HOLIDAY:
        mCurrentFragment = mPublicHolidaysFragment;
        break;
      case IDX_EXPORT:
        mCurrentFragment = mExportFragment;
        break;
      case IDX_STATISTICS:
        mCurrentFragment = mStatisticsFragment;
        break;
      case IDX_WORK_TIME:
        mCurrentFragment = mWorkTimeFragment;
        break;
      default:
        mCurrentFragment = getDefaultHomeView();
        break;
    }
  }
}
