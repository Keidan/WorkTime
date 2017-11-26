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

  public MyActivityLifecycleCallbacks(List<Class<?>> classes) {
    mClasses = classes;
  }


  public boolean isActivityVisible() {
    return mIsActivityVisible;
  }

  @Override
  public void onActivityResumed(Activity activity) {
    for(Class<?> c : mClasses)
      if(c.isInstance(activity)) {
        mIsActivityVisible = true;
        break;
      }
  }

  @Override
  public void onActivityStopped(Activity activity) {
    for(Class<?> c : mClasses)
      if(c.isInstance(activity)) {
        mIsActivityVisible = false;
        break;
      }
  }

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }

  @Override
  public void onActivityStarted(Activity activity) {

  }

  @Override
  public void onActivityPaused(Activity activity) {

  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle savedInstanceState) {

  }

  @Override
  public void onActivityDestroyed(Activity activity) {

  }
}
