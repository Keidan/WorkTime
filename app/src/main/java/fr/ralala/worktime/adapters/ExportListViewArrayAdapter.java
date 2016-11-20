package fr.ralala.worktime.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
 *         <p>
 *******************************************************************************
 */
public class ExportListViewArrayAdapter extends ArrayAdapter<ExportListViewArrayAdapter.ExportEntry>  {

  private Context c = null;
  private int id = 0;
  private List<ExportEntry> items = null;
  private SparseBooleanArray sparseBooleanArray;

  private class ViewHolder {
    TextView text;
    TextView info;
    CheckBox chkItem;
    RelativeLayout rl;
  }

  public ExportListViewArrayAdapter(final Context context, final int textViewResourceId,
                                    final List<ExportEntry> objects) {
    super(context, textViewResourceId, objects);
    this.c = context;
    this.id = textViewResourceId;
    this.items = objects;
    sparseBooleanArray = new SparseBooleanArray();
  }


  public List<ExportEntry> getCheckedItems() {
    List<ExportEntry> array = new ArrayList<>();
    for(int i=0;i<items.size();i++) {
      if(sparseBooleanArray.get(i)) {
        array.add(items.get(i));
      }
    }
    return array;
  }

  @Override
  public ExportEntry getItem(final int i) {
    return items.get(i);
  }

  @Override
  public @NonNull
  View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View v = convertView;
    ViewHolder holder;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      v = vi.inflate(id, null);
      holder = new ViewHolder();
      final ViewHolder vh = holder;
      holder.text = (TextView) v.findViewById(R.id.text);
      holder.info = (TextView) v.findViewById(R.id.info);
      holder.chkItem = (CheckBox) v.findViewById(R.id.chkItem);
      holder.rl = (RelativeLayout) v.findViewById(R.id.rl);
      holder.chkItem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
         sparseBooleanArray.put((Integer) buttonView.getTag(), isChecked);
          if(vh.rl != null) {
            vh.rl.setBackgroundColor(vh.chkItem.isChecked() ?
              c.getResources().getColor(R.color.blue_1, null) :
              c.getResources().getColor(android.R.color.transparent, null));
          }
        }
      });
      v.setTag(holder);
    } else {
        /* We recycle a View that already exists */
      holder = (ViewHolder) v.getTag();
    }
    final ExportEntry ee = items.get(position);
    if (ee != null) {
      if (holder.text != null)
        holder.text.setText(ee.text);
      if (holder.info != null)
        holder.info.setText(ee.info);
      if(holder.chkItem != null) {
        holder.chkItem.setTag(position);
        holder.chkItem.setChecked(sparseBooleanArray.get(position));
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