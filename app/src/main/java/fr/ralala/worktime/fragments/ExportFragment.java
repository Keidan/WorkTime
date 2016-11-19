package fr.ralala.worktime.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.ralala.worktime.AndroidHelper;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.adapters.ExportListViewArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Management of the export fragment view
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ExportFragment extends Fragment implements AdapterView.OnItemSelectedListener, View.OnClickListener{

  private ExportListViewArrayAdapter lvAdapter = null;
  private Spinner sp = null;
  private MainApplication app = null;

  @Override
  public View onCreateView(final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.content_export, container, false);

    app = (MainApplication)getActivity().getApplicationContext();
    Button export = (Button)rootView.findViewById(R.id.btExport);
    export.setOnClickListener(this);

    sp = (Spinner)rootView.findViewById(R.id.spYear);
    final ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
    spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    sp.setAdapter(spAdapter);

    ListView list = (ListView)rootView.findViewById(R.id.list);
    lvAdapter = new ExportListViewArrayAdapter(
      getContext(), R.layout.export_listview_item, new ArrayList<ExportListViewArrayAdapter.ExportEntry>());
    list.setAdapter(lvAdapter);

    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getTimeZone("GMT"));
    c.setTime(new Date());
    int old = 0, idx = -1;
    List<DayEntry> works = app.getDaysFactory().list();
    for(DayEntry de : works) {
      int y = de.getDay().getYear();
      if(old != y) {
        old = y;
        spAdapter.add(String.valueOf(y));
        idx++;
        if(y == c.get(Calendar.YEAR)) sp.setSelection(idx);
      }
    }
    sp.setOnItemSelectedListener(this);
    reload(Integer.parseInt(sp.getSelectedItem().toString()));
    return rootView;
  }

  @Override
  public void onClick(View v) {
    List<ExportListViewArrayAdapter.ExportEntry> entries = lvAdapter.getCheckedItems();
    if(entries.isEmpty()) {
      AndroidHelper.snack(getActivity(), R.string.export_no_items);
      return;
    }
    final String email = app.getEMail();
    if(email.isEmpty()) {
      AndroidHelper.snack(getActivity(), R.string.export_email_not_set);
      return;
    }
    boolean sent = false;

    if(!sent) return;
    AndroidHelper.sentMailTo(getActivity(), email, null,
      getString(R.string.export_email_subject),
      getString(R.string.export_email_body),
      getString(R.string.export_email_senderMsg));
    AndroidHelper.snack(getActivity(), getString(R.string.email_sent_to) + " " + email);
  }

  @Override
  public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    reload(Integer.parseInt(sp.getSelectedItem().toString()));
  }

  @Override
  public void onNothingSelected(AdapterView<?> parentView) {
  }

  private void reload(int year) {
    lvAdapter.clear();
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(Calendar.YEAR, year);
    Locale locale = getResources().getConfiguration().locale;
    for(int i = 0; i < 12; ++i) {
      cal.set(Calendar.MONTH, i);
      long hours = 0, minutes = 0;
      double pay = 0;
      List<DayEntry> works = app.getDaysFactory().list();
      for(DayEntry de : works) {
        if(!de.getDay().isInMonth(i+1) || !de.getDay().isInYear(year)) continue;
        pay += de.getWorkTimePay();
        WorkTimeDay wt = de.getWorkTime();
        hours += wt.getHours();
        minutes += wt.getMinutes();
      }
      if(hours == 0 && minutes == 0) continue;
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
      calendar.setTime(new Date(TimeUnit.MINUTES.toMillis(minutes)));
      String info = String.format(locale, "%02d:%02d %s (%.02f %s)",
        hours + calendar.get(Calendar.HOUR), calendar.get(Calendar.MINUTE),
        getString(R.string.hours_lower_case),
        pay, app.getCurrency());
      ExportListViewArrayAdapter.ExportEntry ee = new ExportListViewArrayAdapter.ExportEntry();
      ee.text = AndroidHelper.getMonthString(i);
      ee.info = info;
      lvAdapter.add(ee);
    }
  }

}
