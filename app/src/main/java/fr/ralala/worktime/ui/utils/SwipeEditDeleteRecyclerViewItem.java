package fr.ralala.worktime.ui.utils;


import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Detect the swipe (left/right) gesture on recycler view item for edit/delete features.
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class SwipeEditDeleteRecyclerViewItem extends ItemTouchHelper.SimpleCallback implements RecyclerView.OnItemTouchListener {

  private final Paint mPaint = new Paint();
  private final SwipeEditDeleteRecyclerViewItemListener mListener;
  private final Bitmap mIconEdit;
  private final Bitmap mIconDelete;
  private final int mColorEdit;
  private final int mColorDelete;
  private RectF mBackgroundEdit = null;
  private RectF mBackgroundDelete = null;
  private int mAdapterPosition = -1;

  public SwipeEditDeleteRecyclerViewItem(Activity activity, RecyclerView recyclerView, SwipeEditDeleteRecyclerViewItemListener listener) {
    super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
    mListener = listener;
    mColorEdit = ResourcesCompat.getColor(activity.getResources(), R.color.item_edit, activity.getTheme());
    mColorDelete = ResourcesCompat.getColor(activity.getResources(), R.color.item_delete, activity.getTheme());
    mIconEdit = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_edit);
    mIconDelete = BitmapFactory.decodeResource(activity.getResources(), R.mipmap.ic_delete);
    ItemTouchHelper itemTouchHelper = new ItemTouchHelper(this);
    itemTouchHelper.attachToRecyclerView(recyclerView);
    recyclerView.addOnItemTouchListener(this);
  }

  public interface SwipeEditDeleteRecyclerViewItemListener {
    /**
     * Called when a ViewHolder is swiped from left to right by the user.
     *
     * @param adapterPosition The position in the adapter.
     */
    void onClickEdit(int adapterPosition);

    /**
     * Called when a ViewHolder is swiped from right to left by the user.
     *
     * @param adapterPosition The position in the adapter.
     */
    void onClickDelete(int adapterPosition);
  }

  /**
   * Silently observe and/or take over touch events sent to the RecyclerView before they are handled by either the RecyclerView itself or its child views.
   *
   * @param rv RecyclerView
   * @param e  MotionEvent describing the touch event. All coordinates are in the RecyclerView's coordinate system.
   * @return true if this OnItemTouchListener wishes to begin intercepting touch events, false to continue with the current behavior and continue observing future events in the gesture.
   */
  @Override
  public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    if (e.getAction() == MotionEvent.ACTION_UP) {
      if (mBackgroundDelete != null && (e.getX() >= mBackgroundDelete.left && e.getX() <= mBackgroundDelete.right && e.getY() >= mBackgroundDelete.top && e.getY() <= mBackgroundDelete.bottom)) {
        mListener.onClickDelete(mAdapterPosition);
        if (rv.getAdapter() != null)
          rv.getAdapter().notifyItemChanged(mAdapterPosition);
        mAdapterPosition = -1;
        return true;
      } else if (mBackgroundEdit != null && (e.getX() >= mBackgroundEdit.right && e.getX() <= mBackgroundEdit.left && e.getY() >= mBackgroundEdit.top && e.getY() <= mBackgroundEdit.bottom)) {
        mListener.onClickEdit(mAdapterPosition);
        if (rv.getAdapter() != null)
          rv.getAdapter().notifyItemChanged(mAdapterPosition);
        mAdapterPosition = -1;
        return true;
      }
    }
    return false;
  }

  /**
   * Process a touch event as part of a gesture that was claimed by returning true from a previous call to onInterceptTouchEvent(RecyclerView, MotionEvent).
   *
   * @param rv RecyclerView
   * @param e  MotionEvent describing the touch event. All coordinates are in the RecyclerView's coordinate system.
   */
  @Override
  public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
    /* Nothing */
  }

  /**
   * Called when a child of RecyclerView does not want RecyclerView and its ancestors to intercept touch events with onInterceptTouchEvent(MotionEvent).
   *
   * @param disallowIntercept True if the child does not want the parent to intercept touch events.
   */
  @Override
  public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    /* Nothing */
  }

  /**
   * Called when ItemTouchHelper wants to move the dragged item from its old position to the new position.
   *
   * @param recyclerView The RecyclerView to which ItemTouchHelper is attached to.
   * @param viewHolder   The ViewHolder which is being dragged by the user.
   * @param target       The ViewHolder over which the currently active item is being dragged.
   * @return True if the viewHolder has been moved to the adapter position of target.
   */
  @Override
  public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
    if (mAdapterPosition != -1) {
      if (recyclerView.getAdapter() != null)
        recyclerView.getAdapter().notifyItemChanged(mAdapterPosition);
      mAdapterPosition = -1;
    }
    return false;
  }

  /**
   * Called when a ViewHolder is swiped by the user.
   *
   * @param viewHolder The ViewHolder which has been swiped by the user.
   * @param direction  The direction to which the ViewHolder is swiped. It is one of UP, DOWN, LEFT or RIGHT.
   */
  @Override
  public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    mAdapterPosition = viewHolder.getAdapterPosition();
  }

  /**
   * Called by ItemTouchHelper on RecyclerView's onDraw callback.
   *
   * @param c                 The canvas which RecyclerView is drawing its children
   * @param recyclerView      The recycler view.
   * @param viewHolder        The ViewHolder which is being interacted by the User or it was interacted and simply animating to its original position
   * @param dX                The amount of horizontal displacement caused by user's action
   * @param dY                The amount of vertical displacement caused by user's action
   * @param actionState       The type of interaction on the View. Is either ACTION_STATE_DRAG or ACTION_STATE_SWIPE.
   * @param isCurrentlyActive True if this view is currently being controlled by the user or false it is simply animating back to its original state.
   */
  @Override
  public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
      View itemView = viewHolder.itemView;
      float height = (float) itemView.getBottom() - (float) itemView.getTop();
      if (mAdapterPosition != -1 && mAdapterPosition != viewHolder.getAdapterPosition()) {
        if (recyclerView.getAdapter() != null)
          recyclerView.getAdapter().notifyItemChanged(mAdapterPosition);
        mAdapterPosition = -1;
      }
      if (dX != 0) {
        viewHolder.itemView.setTranslationX(dX / 5);
        if (dX > 0) {
          mPaint.setColor(mColorEdit);
          mBackgroundEdit = new RectF(itemView.getLeft() + dX / 5, itemView.getTop(), itemView.getLeft(), itemView.getBottom());
          // Draw Rect with varying right side, equal to displacement dX
          c.drawRect(mBackgroundEdit, mPaint);
          // Set the image icon for Right swipe
          int n = mIconEdit.getWidth() / 2;
          float offset = Math.abs((mBackgroundEdit.right) / 2) - n;
          mPaint.setAlpha((int) ((dX * 255) / itemView.getWidth()));
          c.drawBitmap(mIconEdit,
            mBackgroundEdit.right - offset,
            itemView.getTop() + (height - mIconEdit.getHeight()) / 2,
            mPaint);
        } else {
          mPaint.setColor(mColorDelete);
          mBackgroundDelete = new RectF(itemView.getRight() + dX / 5, itemView.getTop(), itemView.getRight(), itemView.getBottom());
          c.drawRect(mBackgroundDelete, mPaint);
          //Set the image icon for Left swipe
          int n = mIconEdit.getWidth() / 2;
          float offset = Math.abs((mBackgroundDelete.right) / 2) - n;
          mPaint.setAlpha((int) ((Math.abs(dX) * 255) / itemView.getWidth()));
          c.drawBitmap(mIconDelete,
            mBackgroundDelete.right - (offset / 3),
            itemView.getTop() + (height - mIconDelete.getHeight()) / 2,
            mPaint);
        }

      } else {
        mBackgroundEdit = null;
        mBackgroundDelete = null;
      }
    }
  }
}
