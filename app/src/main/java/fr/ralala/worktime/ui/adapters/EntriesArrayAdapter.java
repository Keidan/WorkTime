package fr.ralala.worktime.ui.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Generic RecyclerView.Adapter
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public abstract class EntriesArrayAdapter<T> extends RecyclerView.Adapter<EntriesArrayAdapter.ViewHolder> {
  public interface OnLongPressListener<T> {
    /**
     * Called when a long click is captured.
     *
     * @param t The associated item.
     */
    void onLongPressListener(@NonNull T t);
  }

  private final int mId;
  private final List<T> mItems;
  private final RecyclerView mRecyclerView;
  private OnLongPressListener<T> mOnLongPressListener;

  /**
   * Creates the array adapter.
   *
   * @param recyclerView  The owner object.
   * @param rowResourceId The resource id of the container.
   * @param objects       The objects list.
   */
  EntriesArrayAdapter(RecyclerView recyclerView, final int rowResourceId,
                      final List<T> objects) {
    mRecyclerView = recyclerView;
    mId = rowResourceId;
    mItems = objects;
  }

  /**
   * Returns an item.
   *
   * @param position Item position.
   * @return T
   */
  public T getItem(int position) {
    return mItems.get(position);
  }

  /**
   * Sets OnLongPressListener.
   *
   * @param listener New Listener.
   */
  public void setOnLongPressListener(OnLongPressListener<T> listener) {
    mOnLongPressListener = listener;
  }

  /**
   * Called when the view is created.
   *
   * @param viewGroup The view group.
   * @param i         The position
   * @return ViewHolder
   */
  @Override
  public @NonNull EntriesArrayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(mId, viewGroup, false);
    return new ViewHolder(view);
  }

  /**
   * Called on Binding the view holder.
   *
   * @param viewHolder The view holder.
   * @param t          The associated item.
   */
  public abstract void onBindViewHolderEntry(EntriesArrayAdapter.ViewHolder viewHolder, T t);

  /**
   * Called on Binding the view holder.
   *
   * @param viewHolder The view holder.
   * @param i          The position.
   */
  @Override
  public void onBindViewHolder(@NonNull EntriesArrayAdapter.ViewHolder viewHolder, int i) {
    if (mItems.isEmpty()) return;
    if (i > mItems.size())
      i = 0;
    final T t = mItems.get(i);
    if (t != null) {
      if (viewHolder.rl != null && mOnLongPressListener != null)
        viewHolder.rl.setOnLongClickListener(v -> {
          mOnLongPressListener.onLongPressListener(t);
          return true;
        });
      onBindViewHolderEntry(viewHolder, t);
    }
  }

  /**
   * Returns the items count/
   *
   * @return int
   */
  @Override
  public int getItemCount() {
    return mItems.size();
  }

  /**
   * Adds an item.
   *
   * @param item The item to add.
   */
  public void addItem(T item) {
    mItems.add(item);
    safeNotifyDataSetChanged();
  }

  /**
   * Removes an item.
   *
   * @param item The item to remove.
   */
  public void removeItem(T item) {
    mItems.remove(item);
    safeNotifyDataSetChanged();
  }

  /**
   * This method call mRecyclerView.getRecycledViewPool().clear() and notifyDataSetChanged().
   */
  @SuppressLint("NotifyDataSetChanged")
  public void safeNotifyDataSetChanged() {
    mRecyclerView.getRecycledViewPool().clear();
    try {
      notifyDataSetChanged();
    } catch (Exception e) {
      String text = "Exception: " + e.getMessage();
      MainApplication.addLog(mRecyclerView.getContext(), "safeNotifyDataSetChanged", text);
      Log.e(getClass().getSimpleName(), text, e);
    }
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    TextView info;
    RelativeLayout rl;

    ViewHolder(View view) {
      super(view);
      rl = view.findViewById(R.id.rl);
      name = view.findViewById(R.id.name);
      info = view.findViewById(R.id.info);
    }
  }
}
