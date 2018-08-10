package fr.ralala.worktime.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.ProfileEntry;

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
public class ProfilesEntriesArrayAdapter  extends EntriesArrayAdapter<ProfileEntry> {

  private Context mContext;

  /**
   * Creates the array adapter.
   * @param recyclerView The owner object.
   * @param context The Android context.
   * @param rowResourceId The resource id of the container.
   * @param objects The objects list.
   */
  public ProfilesEntriesArrayAdapter(RecyclerView recyclerView, final Context context, final int rowResourceId,
                                           final List<ProfileEntry> objects) {
    super(recyclerView, rowResourceId, objects);
    mContext = context;
  }

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param pe The associated item.
   */
  @Override
  public void onBindViewHolderEntry(EntriesArrayAdapter.ViewHolder viewHolder, ProfileEntry pe) {
    if (pe != null) {
      if (viewHolder.name != null)
        viewHolder.name.setText(pe.getName());
      if (viewHolder.info != null) {
        MainApplication app = MainApplication.getInstance();
        viewHolder.info.setText(!app.isHideWage() ?
            String.format(Locale.US,
                "%s %s %s -> %s %s %.02f%s/h %s %s %s",
                pe.getStartMorning().timeString(),
                mContext.getString(R.string.at),
                pe.getEndAfternoon().timeString(),
                pe.getWorkTime().timeString(),
                mContext.getString(R.string.text_for),
                pe.getAmountByHour(),
                MainApplication.getInstance().getCurrency(),
                mContext.getString(R.string.and),
                pe.getOverTime().timeString(),
                mContext.getString(R.string.more))
            :
            String.format(Locale.US,
                "%s %s %s -> %s %s %s %s",
                pe.getStartMorning().timeString(),
                mContext.getString(R.string.at),
                pe.getEndAfternoon().timeString(),
                pe.getWorkTime().timeString(),
                mContext.getString(R.string.and),
                pe.getOverTime().timeString(),
                mContext.getString(R.string.more)));
      }
    }
  }
}