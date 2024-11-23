package fr.ralala.worktime.ui.adapters;

import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import fr.ralala.worktime.R;

/**
 * ******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Manage the export listview
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class ExportListViewArrayAdapter extends ArrayAdapter<ExportListViewArrayAdapter.ExportEntry> {

  private final Context mContext;
  private final int mId;
  private final List<ExportEntry> mItems;
  private final SparseBooleanArray mSparseBooleanArray;

  private static class ViewHolder {
    TextView text;
    TextView info;
    CheckBox chkItem;
    RelativeLayout rl;
  }

  /**
   * Creates the array adapter.
   *
   * @param context            The Android context.
   * @param textViewResourceId The resource id of the container.
   * @param objects            The objects list.
   */
  public ExportListViewArrayAdapter(final Context context, final int textViewResourceId,
                                    final List<ExportEntry> objects) {
    super(context, textViewResourceId, objects);
    mContext = context;
    mId = textViewResourceId;
    mItems = objects;
    mSparseBooleanArray = new SparseBooleanArray();
  }


  /**
   * Returns the checked items.
   *
   * @return <code>List<ExportEntry></code>
   */
  public List<ExportEntry> getCheckedItems() {
    List<ExportEntry> array = new ArrayList<>();
    for (int i = 0; i < mItems.size(); i++) {
      if (mSparseBooleanArray.get(i)) {
        array.add(mItems.get(i));
      }
    }
    return array;
  }

  /**
   * Returns an items at a specific position.
   *
   * @param i The item index.
   * @return The item.
   */
  @Override
  public ExportEntry getItem(final int i) {
    return mItems.get(i);
  }

  /**
   * Returns the current view.
   *
   * @param position    The view position.
   * @param convertView The view to convert.
   * @param parent      The parent.
   * @return The new view.
   */
  @Override
  public @NonNull
  View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      v = vi.inflate(mId, null);
      holder = new ViewHolder();
      final ViewHolder vh = holder;
      holder.text = v.findViewById(R.id.text);
      holder.info = v.findViewById(R.id.info);
      holder.chkItem = v.findViewById(R.id.chkItem);
      holder.rl = v.findViewById(R.id.rl);
      holder.chkItem.setOnCheckedChangeListener((buttonView, isChecked) -> {
        mSparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
        if (vh.rl != null) {
          vh.rl.setBackgroundColor(vh.chkItem.isChecked() ?
            mContext.getResources().getColor(R.color.blue_1, null) :
            mContext.getResources().getColor(android.R.color.transparent, null));
        }
      });
      v.setTag(holder);
    } else {
      /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    final ExportEntry ee = mItems.get(position);
    if (ee != null) {
      if (holder.text != null)
        holder.text.setText(ee.text);
      if (holder.info != null)
        holder.info.setText(ee.info);
      if (holder.chkItem != null) {
        holder.chkItem.setTag(position);
        holder.chkItem.setChecked(mSparseBooleanArray.get(position));
      }
    }
    return v;
  }

  public static class ExportEntry {
    public int month;
    public int year;
    public String text;
    public String info;
  }

}