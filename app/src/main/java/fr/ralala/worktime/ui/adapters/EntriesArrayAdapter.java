package fr.ralala.worktime.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Generic RecyclerView.Adapter
 * </p>
 *
 * @author Keidan
 *         <p>
 *******************************************************************************
 */
public abstract class EntriesArrayAdapter extends RecyclerView.Adapter<EntriesArrayAdapter.ViewHolder>{

  private int mId;
  private List<DayEntry> mItems;
  private RecyclerView mRecyclerView;

  /**
   * Creates the array adapter.
   * @param recyclerView The owner object.
   * @param rowResourceId The resource id of the container.
   * @param objects The objects list.
   */
  EntriesArrayAdapter(RecyclerView recyclerView, final int rowResourceId,
                                     final List<DayEntry> objects) {
    mRecyclerView = recyclerView;
    mId = rowResourceId;
    mItems = objects;
  }

  /**
   * Returns an item.
   * @param position Item position.
   * @return DayEntry
   */
  public DayEntry getItem(int position) {
    return mItems.get(position);
  }

  /**
   * Called when the view is created.
   * @param viewGroup The view group.
   * @param i The position
   * @return ViewHolder
   */
  @Override
  public @NonNull ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(mId, viewGroup, false);
    return new ViewHolder(view);
  }

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param de The associated item.
   */
  public abstract void onBindViewHolder(ViewHolder viewHolder, DayEntry de);

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param i The position.
   */
  @Override
  public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
    if(mItems.isEmpty()) return;
    if(i > mItems.size())
      i = 0;
    final DayEntry t = mItems.get(i);
    if (t != null) {
      onBindViewHolder(viewHolder, t);
    }
  }

  /**
   * Returns the items count/
   * @return int
   */
  @Override
  public int getItemCount() {
    return mItems.size();
  }

  /**
   * Adds an item.
   * @param item The item to add.
   */
  public void addItem(DayEntry item) {
    mItems.add(item);
    safeNotifyDataSetChanged();
  }

  /**
   * Removes an item.
   * @param item The item to remove.
   */
  public void removeItem(DayEntry item) {
    mItems.remove(item);
    safeNotifyDataSetChanged();
  }

  /**
   * This method call mRecyclerView.getRecycledViewPool().clear() and notifyDataSetChanged().
   */
  public void safeNotifyDataSetChanged() {
    mRecyclerView.getRecycledViewPool().clear();
    try {
      notifyDataSetChanged();
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
  }

  class ViewHolder extends RecyclerView.ViewHolder{
    TextView name;
    TextView info;

    ViewHolder(View view) {
      super(view);
      name = view.findViewById(R.id.name);
      info = view.findViewById(R.id.info);
    }
  }
}
