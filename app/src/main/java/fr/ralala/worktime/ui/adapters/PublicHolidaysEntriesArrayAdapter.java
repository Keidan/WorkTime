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

  private Context c = null;
  private int id = 0;
  private List<DayEntry> items = null;
  private SimpleEntriesArrayAdapterMenuListener<DayEntry> listener = null;

  private class ViewHolder {
    TextView name;
    TextView info;
    ImageView menu;
  }

  public PublicHolidaysEntriesArrayAdapter(final Context context, final int textViewResourceId,
                                           final List<DayEntry> objects,
                                           final SimpleEntriesArrayAdapterMenuListener<DayEntry> listener) {
    super(context, textViewResourceId, objects);
    this.c = context;
    this.id = textViewResourceId;
    this.items = objects;
    this.listener = listener;
  }

  @Override
  public DayEntry getItem(final int i) {
    return items.get(i);
  }

  @Override
  public @NonNull View getView(final int position, final View convertView,
                               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      v = vi.inflate(id, null);
      holder = new ViewHolder();
      holder.name = v.findViewById(R.id.name);
      holder.info = v.findViewById(R.id.info);
      holder.menu = v.findViewById(R.id.menu);
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }

    final DayEntry t = items.get(position);
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
              final PopupMenu popup = new PopupMenu(c, vv);
              /* Force the icons display */
              UIHelper.forcePopupMenuIcons(popup);
              popup.getMenuInflater().inflate(R.menu.popup_listview_edit_delete,
                popup.getMenu());
              /* Init the default behaviour */
              popup.show();
              popup.setOnMenuItemClickListener((item) -> {
                if (listener != null && R.id.edit == item.getItemId())
                  listener.onMenuEdit(t);
                else if (listener != null && R.id.delete == item.getItemId())
                  listener.onMenuDelete(t);
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