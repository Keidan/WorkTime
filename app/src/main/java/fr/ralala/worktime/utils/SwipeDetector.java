package fr.ralala.worktime.utils;


import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Detect the swipe (left/right) gesture
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class SwipeDetector implements GestureDetector.OnGestureListener{
  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 250;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;
  private GestureDetector detector = null;
  private SwipeDetectorListener li = null;

  public SwipeDetector(final Context c) {
    detector = new GestureDetector(c, this);
  }

  public void setSwipeDetectorListener(final SwipeDetectorListener li) {
    this.li = li;
  }

  public boolean onTouchEvent(final MotionEvent ev) {
    return detector.onTouchEvent(ev);
  }

  @Override
  public boolean onDown(MotionEvent motionEvent) {
    return true;
  }

  @Override
  public void onShowPress(MotionEvent motionEvent) {

  }

  @Override
  public boolean onSingleTapUp(MotionEvent motionEvent) {
    return true;
  }

  @Override
  public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
    return true;
  }

  @Override
  public void onLongPress(MotionEvent motionEvent) {

  }

  @Override

  public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
    // detect the fling gesture to increase or decrease the current month
    if (Math.abs(start.getY() - finish.getY()) > SWIPE_MAX_OFF_PATH)
      return false;

    if(start.getX() - finish.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      // left to right swipe
      if(li != null) li.leftToRightSwipe();
    }
    else if (finish.getX() - start.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      // right to left swipe
      if(li != null) li.rightToLeftSwipe();
    }
    return true;
  }

  public interface SwipeDetectorListener {
    void leftToRightSwipe();
    void rightToLeftSwipe();
  }
}
