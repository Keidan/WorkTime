package fr.ralala.worktime.fragments;


import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;

import fr.ralala.worktime.AndroidHelper;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.adapters.PublicHolidaysEntriesArrayAdapter;
import fr.ralala.worktime.adapters.SimpleEntriesArrayAdapterMenuListener;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the public holidays fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class PublicHolidaysFragment extends Fragment implements SimpleEntriesArrayAdapterMenuListener<DayEntry>, View.OnClickListener{

  private PublicHolidaysEntriesArrayAdapter adapter = null;
  private MainApplication app = null;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_public_holidays, container, false);

    ListView lv = (ListView) rootView.findViewById(R.id.public_holidays);
    FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab);

    app = MainApplication.getApp(getContext());
    adapter = new PublicHolidaysEntriesArrayAdapter(
      getContext(), R.layout.public_holidays_listview_item, app.getPublicHolidaysFactory().list(), this);
    lv.setAdapter(adapter);


    fab.setOnClickListener(this);
    return rootView;
  }

  public boolean onMenuEdit(DayEntry de) {
    openDialog(de);
    return true;
  }

  public void onMenuDelete(DayEntry de) {
    app.getPublicHolidaysFactory().remove(de);
    adapter.remove(de);
  }

  public void onClick(View view) {
    openDialog(null);
  }

  public void openDialog(final DayEntry de) {
    /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
      .setAction("Action", null).show();*/
    /* Crete the dialog builder and set the title */
    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
    dialogBuilder.setTitle(R.string.public_holidays);
    /* prepare the inflater and set the new content */
    LayoutInflater inflater = getActivity().getLayoutInflater();
    final View dialogView = inflater.inflate(R.layout.content_public_holidays_dialog_box, null);
    dialogBuilder.setView(dialogView);
    /* Get the components */
    final EditText tname = (EditText) dialogView.findViewById(R.id.etName);
    final DatePicker tdate = (DatePicker) dialogView.findViewById(R.id.dpDate);
    if(de != null) {
      tname.setText(de.getName());
      // set current date into datepicker
      tdate.init(de.getDay().getYear(), de.getDay().getMonth() - 1, de.getDay().getDay(), null);
    } else {
      tname.setText("");
      WorkTimeDay now = WorkTimeDay.now();
      // set current date into datepicker
      tdate.init(now.getYear(), now.getMonth() - 1, now.getDay(), null);
    }
    /* Init the common listener. */
    final DialogInterface.OnClickListener ocl = new DialogInterface.OnClickListener(){
      public void onClick(DialogInterface dialog, int whichButton) {
        /* Click on the Positive button (OK) */
        if(whichButton == DialogInterface.BUTTON_POSITIVE) {
          final String name = tname.getText().toString().trim();
          if(name.isEmpty()) {
            AndroidHelper.showAlertDialog(getActivity(), R.string.error, R.string.error_no_name);
            return;
          }
          WorkTimeDay wtd = new WorkTimeDay();
          wtd.setDay(tdate.getDayOfMonth());
          wtd.setMonth(tdate.getMonth() + 1);
          wtd.setYear(tdate.getYear());
          if(de != null) app.getPublicHolidaysFactory().remove(de); /* remove old entry */
          DayEntry de = new DayEntry(wtd, DayType.PUBLIC_HOLIDAY);
          de.setName(name);
          app.getPublicHolidaysFactory().add(de);
          //adapter.add(de);
          adapter.notifyDataSetChanged();
        }
        dialog.dismiss();
      }
    };
    /* attach the listeners and init the default values */
    dialogBuilder.setPositiveButton(R.string.ok, ocl);
    dialogBuilder.setNegativeButton(R.string.cancel, ocl);
    /* show the dialog */
    AlertDialog alertDialog = dialogBuilder.create();
    alertDialog.show();
  }

}
