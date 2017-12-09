package fr.ralala.worktime.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.R;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.ui.utils.UIHelper;

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
public class PublicHolidaysEntriesArrayAdapter extends ArrayAdapter<DayEntry> {

  private Context mContext = null;
  private int mId = 0;
  private List<DayEntry> mItems = null;
  private SimpleEntriesArrayAdapterMenuListener<DayEntry> mListener = null;

  private class ViewHolder {
    TextView name;
    TextView info;
    ImageView menu;
  }

  /**
   * Creates the array adapter.
   * @param context The Android context.
   * @param textViewResourceId The resource id of the container.
   * @param objects The objects list.
   * @param listener The listener used for the popup menu.
   */
  public PublicHolidaysEntriesArrayAdapter(final Context context, final int textViewResourceId,
                                           final List<DayEntry> objects,
                                           final SimpleEntriesArrayAdapterMenuListener<DayEntry> listener) {
    super(context, textViewResourceId, objects);
    mContext = context;
    mId = textViewResourceId;
    mItems = objects;
    mListener = listener;
  }

  /**
   * Returns an items at a specific position.
   * @param i The item index.
   * @return The item.
   */
  @Override
  public DayEntry getItem(final int i) {
    return mItems.get(i);
  }

  /**
   * Returns the current view.
   * @param position The view position.
   * @param convertView The view to convert.
   * @param parent The parent.
   * @return The new view.
   */
  @Override
  public @NonNull View getView(final int position, final View convertView,
                               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      v = vi.inflate(mId, null);
      holder = new ViewHolder();
      holder.name = v.findViewById(R.id.name);
      holder.info = v.findViewById(R.id.info);
      holder.menu = v.findViewById(R.id.menu);
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }

    final DayEntry t = mItems.get(position);
    if (t != null) {
      if (holder.name != null)
        holder.name.setText(t.getName());
      if (holder.info != null) {
        Calendar cal = t.getDay().toCalendar();
        String text = String.format(Locale.US, "%02d %s",
          cal.get(Calendar.DAY_OF_MONTH), AndroidHelper.getMonthString(cal.get(Calendar.MONTH)));
        if(!t.isRecurrence())
          text += String.format(Locale.US, " %04d", cal.get(Calendar.YEAR));
        holder.info.setText(text);

      }
      /* Show the popup menu if the user click on the 3-dots item. */
      try {
        holder.menu.setOnClickListener((vv) -> {
          switch (vv.getId()) {
            case R.id.menu:
              final PopupMenu popup = new PopupMenu(mContext, vv);
              /* Force the icons display */
              UIHelper.forcePopupMenuIcons(popup);
              popup.getMenuInflater().inflate(R.menu.popup_listview_edit_delete,
                popup.getMenu());
              /* Init the default behaviour */
              popup.show();
              popup.setOnMenuItemClickListener((item) -> {
                if (mListener != null && R.id.edit == item.getItemId())
                  mListener.onMenuEdit(t);
                else if (mListener != null && R.id.delete == item.getItemId())
                  mListener.onMenuDelete(t);
                return true;
              });
              break;
            default:
              break;
          }
        });
      } catch (Exception e) {
        Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      }
    }
    return v;
  }

}