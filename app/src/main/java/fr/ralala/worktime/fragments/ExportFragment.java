package fr.ralala.worktime.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.adapters.ExportListViewArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.ExcelHelper;
import jxl.write.WritableSheet;

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
    final List<ExportListViewArrayAdapter.ExportEntry> entries = lvAdapter.getCheckedItems();
    if(entries.isEmpty()) {
      AndroidHelper.snack(getActivity(), R.string.export_no_items);
      return;
    }
    final String email = app.getEMail();
    if(app.isExportMailEnabled() && email.isEmpty()) {
      AndroidHelper.snack(getActivity(), R.string.export_email_not_set);
      return;
    }

    final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
      getString(R.string.app_name) + "_" + entries.get(0).year + ".xls");
    if(file.exists()) {
      AndroidHelper.showConfirmDialog(getActivity(), getString(R.string.confirm),
        getString(R.string.the_file_exists_part_1) + " " + file.getName() + " " + getString(R.string.the_file_exists_part_2),
        new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            exportXLS(entries, email, file);
          }
        }, null);
    } else
      exportXLS(entries, email, file);
  }

  private void exportXLS(List<ExportListViewArrayAdapter.ExportEntry> entries, String email, File file) {
    try {
      ExcelHelper excel = new ExcelHelper(getActivity(), file);
      for(int idx = 0; idx < entries.size(); ++idx) {
        ExportListViewArrayAdapter.ExportEntry ee = entries.get(idx);
        WritableSheet sheet = excel.createSheet(AndroidHelper.getMonthString(ee.month), idx);
        int row = 0, column = 0;
        excel.createHorizontalHeader(sheet, row, column, new String[]{
          getString(R.string.date).replaceAll(" :", ""), // A -> 65
          getString(R.string.type).replaceAll(" :", ""), // B -> 66
          getString(R.string.hour_in),                   // C -> 67
          getString(R.string.hour_out),                  // D -> 68
          getString(R.string.break_time),                // E -> 69
          getString(R.string.overtime),                  // F -> 70
          getString(R.string.wage).replaceAll(" :", ""),                      // G -> 71
        });
        List<DayEntry> works = app.getDaysFactory().list();
        column = 0;
        row++;
        for (DayEntry de : works) {
          if (!de.getDay().isInMonth(ee.month + 1) || !de.getDay().isInYear(ee.year)) continue;
          excel.addLabel(sheet, row, column, de.getDay().dateString(), false);
          excel.addLabel(sheet, row, column+1, de.getType() != DayType.AT_WORK ? de.getType().string(getActivity()) : "", false);
          excel.addLabel(sheet, row, column+2, de.getStart().timeString(), false);
          excel.addLabel(sheet, row, column+3, de.getEnd().timeString(), false);
          excel.addLabel(sheet, row, column+4, de.getPause().timeString(), false);
          excel.addLabel(sheet, row, column+5, de.getOverTime(app).timeString(), false);
          excel.addLabel(sheet, row++, column+6, String.format(Locale.US, "%.02f", de.getWorkTimePay()), false);
        }
        /*column = 1;
        excel.addLabel(sheet, row, column++, getString(R.string.total), true);
        for (int i = 0; i < 5; ++i) {
          char c = (char) (67 + i);
          StringBuilder sb = new StringBuilder();
          sb.append("SUM(").append(c).append(1).append(":").append(c).append(row - 1).append(")");
          excel.addFormula(sheet, row, column++, sb);
        }*/
      }

      excel.write();
      Uri uri = Uri.fromFile(file);
      if(app.isExportMailEnabled()) {
        AndroidHelper.sentMailTo(getActivity(), email, uri,
          getString(R.string.export_email_subject),
          getString(R.string.export_email_body),
          getString(R.string.export_email_senderMsg));
        AndroidHelper.snack(getActivity(), getString(R.string.email_sent_to_with_mail) + " " + email);
      } else
        AndroidHelper.snack(getActivity(), getString(R.string.email_sent_to_without_mail));
      //if(file.exists()) file.delete();
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      AndroidHelper.snack(getActivity(), getString(R.string.error) + ": " + e.getMessage());
    }
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
      ee.month = i;
      ee.year = year;
      lvAdapter.add(ee);
    }
  }

}
