package fr.ralala.worktime.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.models.PublicHolidayEntry;
import fr.ralala.worktime.utils.AndroidHelper;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the public holidays list view
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class PublicHolidaysEntriesArrayAdapter extends EntriesArrayAdapter<PublicHolidayEntry> {

  /**
   * Creates the array adapter.
   *
   * @param recyclerView  The owner object.
   * @param rowResourceId The resource id of the container.
   * @param objects       The objects list.
   */
  public PublicHolidaysEntriesArrayAdapter(RecyclerView recyclerView, final int rowResourceId,
                                           final List<PublicHolidayEntry> objects) {
    super(recyclerView, rowResourceId, objects);
  }

  /**
   * Called on Binding the view holder.
   *
   * @param viewHolder The view holder.
   * @param phe        The associated item.
   */
  @Override
  public void onBindViewHolderEntry(EntriesArrayAdapter.ViewHolder viewHolder, PublicHolidayEntry phe) {
    if (phe != null) {
      if (viewHolder.name != null)
        viewHolder.name.setText(phe.getName());
      if (viewHolder.info != null) {
        Calendar cal = phe.getDay().toCalendar();
        String text = String.format(Locale.US, "%02d %s",
          cal.get(Calendar.DAY_OF_MONTH), AndroidHelper.getMonthString(cal.get(Calendar.MONTH)));
        if (!phe.isRecurrence())
          text += String.format(Locale.US, " %04d", cal.get(Calendar.YEAR));
        viewHolder.info.setText(text);
      }
    }
  }
}