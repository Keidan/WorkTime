package fr.ralala.worktime.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the profiles list view
 * </p>
 *
 * @author Keidan
 *         <p>
 *******************************************************************************
 */
public class ProfilesEntriesArrayAdapter  extends RecyclerView.Adapter<ProfilesEntriesArrayAdapter.ViewHolder> {

  private Context mContext = null;
  private int mId = 0;
  private List<DayEntry> mItems = null;

  /**
   * Creates the array adapter.
   * @param context The Android context.
   * @param rowResourceId The resource id of the container.
   * @param objects The objects list.
   */
  public ProfilesEntriesArrayAdapter(final Context context, final int rowResourceId,
                                           final List<DayEntry> objects) {
    mContext = context;
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
    if (t != null) {
      if (viewHolder.name != null)
        viewHolder.name.setText(t.getName());
      if (viewHolder.info != null) {
        MainApplication app = (MainApplication) mContext.getApplicationContext();
        viewHolder.info.setText(!app.isHideWage() ?
            String.format(Locale.US,
                "%s %s %s -> %s %s %.02f%s/h %s %s %s",
                t.getStartMorning().timeString(),
                mContext.getString(R.string.at),
                t.getEndAfternoon().timeString(),
                t.getWorkTime().timeString(),
                mContext.getString(R.string.text_for),
                t.getAmountByHour(),
                ((MainApplication)mContext.getApplicationContext()).getCurrency(),
                mContext.getString(R.string.and),
                t.getOverTime().timeString(),
                mContext.getString(R.string.more))
            :
            String.format(Locale.US,
                "%s %s %s -> %s %s %s %s",
                t.getStartMorning().timeString(),
                mContext.getString(R.string.at),
                t.getEndAfternoon().timeString(),
                t.getWorkTime().timeString(),
                mContext.getString(R.string.and),
                t.getOverTime().timeString(),
                mContext.getString(R.string.more)));
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