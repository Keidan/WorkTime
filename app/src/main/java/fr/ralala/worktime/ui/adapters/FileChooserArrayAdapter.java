package fr.ralala.worktime.ui.adapters;

import java.util.List;


import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import fr.ralala.worktime.R;
import fr.ralala.worktime.models.FileChooserOption;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Listview adapter
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class FileChooserArrayAdapter extends ArrayAdapter<FileChooserOption> {

  private final Context      c;
  private final int          id;
  private final List<FileChooserOption> items;

  public FileChooserArrayAdapter(final Context context, final int textViewResourceId,
                                    final List<FileChooserOption> objects) {
    super(context, textViewResourceId, objects);
    c = context;
    id = textViewResourceId;
    items = objects;
  }

  @Override
  public FileChooserOption getItem(final int i) {
    return items.get(i);
  }

  @Override
  public @NonNull View getView(final int position, final View convertView,
               @NonNull final ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater vi = (LayoutInflater) c
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      assert vi != null;
      v = vi.inflate(id, null);
    }
    final FileChooserOption o = items.get(position);
    if (o != null) {
      final ImageView i1 = v.findViewById(R.id.icon);
      final TextView t1 = v.findViewById(R.id.name);
      final TextView t2 = v.findViewById(R.id.data);
      if (i1 != null)
        i1.setImageDrawable(o.getIcon());
      if (t1 != null)
        t1.setText(o.getName());
      if (t2 != null)
        t2.setText(o.getData());

    }
    return v;
  }

}