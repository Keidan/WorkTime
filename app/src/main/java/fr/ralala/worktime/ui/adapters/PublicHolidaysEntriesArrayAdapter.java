package fr.ralala.worktime.ui.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.utils.AndroidHelper;
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
public class PublicHolidaysEntriesArrayAdapter extends EntriesArrayAdapter {

  /**
   * Creates the array adapter.
   * @param recyclerView The owner object.
   * @param rowResourceId The resource id of the container.
   * @param objects The objects list.
   */
  public PublicHolidaysEntriesArrayAdapter(RecyclerView recyclerView, final int rowResourceId,
                                           final List<DayEntry> objects) {
    super(recyclerView, rowResourceId, objects);
  }

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param de The associated item.
   */
  @Override
  public void onBindViewHolder(EntriesArrayAdapter.ViewHolder viewHolder, DayEntry de) {
    if(de != null) {
      if (viewHolder.name != null)
        viewHolder.name.setText(de.getName());
      if (viewHolder.info != null) {
        Calendar cal = de.getDay().toCalendar();
        String text = String.format(Locale.US, "%02d %s",
            cal.get(Calendar.DAY_OF_MONTH), AndroidHelper.getMonthString(cal.get(Calendar.MONTH)));
        if (!de.isRecurrence())
          text += String.format(Locale.US, " %04d", cal.get(Calendar.YEAR));
        viewHolder.info.setText(text);
      }
    }
  }
}