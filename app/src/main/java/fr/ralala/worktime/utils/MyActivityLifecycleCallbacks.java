package fr.ralala.worktime.utils;


import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.List;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage activity state changes notifications
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class MyActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

  private List<Class<?>> mClasses;
  private boolean mIsActivityVisible = false;

  /**
   * Creates the LifecycleCallbacks object for a list of activities.
   * @param classes The list of activities to monitor.
   */
  public MyActivityLifecycleCallbacks(List<Class<?>> classes) {
    mClasses = classes;
  }


  /**
   * Tests if the activity is visible.
   * @return boolean.
   */
  public boolean isActivityVisible() {
    return mIsActivityVisible;
  }

  /**
   * Called when the activity is resumed.
   * See official javadoc.
   * @param activity See official javadoc.
   */
  @Override
  public void onActivityResumed(Activity activity) {
    for(Class<?> c : mClasses)
      if(c.isInstance(activity)) {
        mIsActivityVisible = true;
        break;
      }
  }

  /**
   * Called when the activity is stopped.
   * See official javadoc.
   * @param activity See official javadoc.
   */
  @Override
  public void onActivityStopped(Activity activity) {
    for(Class<?> c : mClasses)
      if(c.isInstance(activity)) {
        mIsActivityVisible = false;
        break;
      }
  }

  /**
   * See official javadoc.
   * @param activity See official javadoc.
   * @param savedInstanceState See official javadoc.
   */
  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  /**
   * See official javadoc.
   * @param activity See official javadoc.
   */
  @Override
  public void onActivityStarted(Activity activity) {

  }

  /**
   * See official javadoc.
   * @param activity See official javadoc.
   */
  @Override
  public void onActivityPaused(Activity activity) {

  }

  /**
   * See official javadoc.
   * @param activity See official javadoc.
   * @param savedInstanceState See official javadoc.
   */
  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle savedInstanceState) {

  }

  /**
   * See official javadoc.
   * @param activity See official javadoc.
   */
  @Override
  public void onActivityDestroyed(Activity activity) {

  }
}
