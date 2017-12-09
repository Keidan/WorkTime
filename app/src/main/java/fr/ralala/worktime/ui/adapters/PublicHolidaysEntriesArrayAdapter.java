package fr.ralala.worktime.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the public holidays list view
 * </p>
 *
 * @author Keidan
 *         <p>
 *******************************************************************************
 */
public class PublicHolidaysEntriesArrayAdapter extends RecyclerView.Adapter<PublicHolidaysEntriesArrayAdapter.ViewHolder> {

  private int mId = 0;
  private List<DayEntry> mItems = null;

  /**
   * Creates the array adapter.
   * @param rowResourceId The resource id of the container.
   * @param objects The objects list.
   */
  public PublicHolidaysEntriesArrayAdapter(final int rowResourceId,
                                           final List<DayEntry> objects) {
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
  public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(mId, viewGroup, false);
    return new ViewHolder(view);
  }

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param i The position.
   */
  @Override
  public void onBindViewHolder(ViewHolder viewHolder, int i) {
    final DayEntry t = mItems.get(i);
    if(t != null) {
      if (viewHolder.name != null)
        viewHolder.name.setText(t.getName());
      if (viewHolder.info != null) {
        Calendar cal = t.getDay().toCalendar();
        String text = String.format(Locale.US, "%02d %s",
            cal.get(Calendar.DAY_OF_MONTH), AndroidHelper.getMonthString(cal.get(Calendar.MONTH)));
        if (!t.isRecurrence())
          text += String.format(Locale.US, " %04d", cal.get(Calendar.YEAR));
        viewHolder.info.setText(text);
      }
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
    notifyDataSetChanged();
  }

  /**
   * Removes an item.
   * @param item The item to remove.
   */
  public void removeItem(DayEntry item) {
    mItems.remove(item);
    notifyDataSetChanged();
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