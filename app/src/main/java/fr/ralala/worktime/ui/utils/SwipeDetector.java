package fr.ralala.worktime.ui.utils;


import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;

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
  private GestureDetector mDetector;
  private SwipeDetectorListener mLi = null;

  /**
   * Creates the SwipeDetector instance.
   * @param c The Android context.
   */
  public SwipeDetector(final Context c) {
    mDetector = new GestureDetector(c, this);
  }

  /**
   * Sets the swipe detector listener.
   * @param li The new listener.
   */
  public void setSwipeDetectorListener(final SwipeDetectorListener li) {
    mLi = li;
  }

  /**
   * Called in the activity when the onTouchEvent is called.
   * @param ev See official javadoc.
   * @return See official javadoc.
   */
  public boolean onTouchEvent(final MotionEvent ev) {
    return mDetector.onTouchEvent(ev);
  }

  /**
   * See official javadoc.
   * @param motionEvent See official javadoc.
   * @return true
   */
  @Override
  public boolean onDown(MotionEvent motionEvent) {
    return true;
  }

  /**
   * See official javadoc.
   * @param motionEvent See official javadoc.
   */
  @Override
  public void onShowPress(MotionEvent motionEvent) {

  }

  /**
   * See official javadoc.
   * @param motionEvent See official javadoc.
   * @return true
   */
  @Override
  public boolean onSingleTapUp(MotionEvent motionEvent) {
    return true;
  }

  /**
   * See official javadoc.
   * @param motionEvent See official javadoc.
   * @param motionEvent1 See official javadoc.
   * @param v See official javadoc.
   * @param v1 See official javadoc.
   * @return true
   */
  @Override
  public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
    return true;
  }

  /**
   * See official javadoc.
   * @param motionEvent See official javadoc.
   */
  @Override
  public void onLongPress(MotionEvent motionEvent) {

  }

  /**
   * See official javadoc.
   * @param start See official javadoc.
   * @param finish See official javadoc.
   * @param velocityX See official javadoc.
   * @param velocityY See official javadoc.
   * @return boolean
   */
  @Override
  public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
    // detect the fling gesture to increase or decrease the current month
    if (Math.abs(start.getY() - finish.getY()) > SWIPE_MAX_OFF_PATH)
      return false;

    if(start.getX() - finish.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      // left to right swipe
      if(mLi != null) mLi.leftToRightSwipe();
    }
    else if (finish.getX() - start.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
      // right to left swipe
      if(mLi != null) mLi.rightToLeftSwipe();
    }
    return true;
  }

  public interface SwipeDetectorListener {
    /**
     * Called when the user sweeps from left to right.
     */
    void leftToRightSwipe();
    /**
     * Called when the user sweeps from right to left.
     */
    void rightToLeftSwipe();
  }
}
