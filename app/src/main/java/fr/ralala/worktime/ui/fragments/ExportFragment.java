package fr.ralala.worktime.ui.fragments;


import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
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

import org.apache.poi.ss.usermodel.Sheet;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import fr.ralala.worktime.models.DayType;
import fr.ralala.worktime.utils.AndroidHelper;
import fr.ralala.worktime.MainApplication;
import fr.ralala.worktime.R;
import fr.ralala.worktime.ui.adapters.ExportListViewArrayAdapter;
import fr.ralala.worktime.models.DayEntry;
import fr.ralala.worktime.models.WorkTimeDay;
import fr.ralala.worktime.utils.ExcelHelper;
import fr.ralala.worktime.ui.utils.UIHelper;

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

  private ExportListViewArrayAdapter mLvAdapter = null;
  private Spinner mSpinner = null;
  private MainApplication mApp = null;

  /**
   * Called when the fragment is created.
   * @param inflater The fragment inflater.
   * @param container The fragment container.
   * @param savedInstanceState The saved instance state.
   * @return The created view.
   */
  @Override
  public View onCreateView(@NonNull final LayoutInflater inflater,
                           final ViewGroup container, final Bundle savedInstanceState) {
    final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_export, container, false);
  if(getActivity() == null) return rootView;
    mApp = (MainApplication)getActivity().getApplicationContext();
    Button export = rootView.findViewById(R.id.btExport);
    export.setOnClickListener(this);

    mSpinner = rootView.findViewById(R.id.spYear);
    final ArrayAdapter<String> spAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
    spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    mSpinner.setAdapter(spAdapter);

    ListView list = rootView.findViewById(R.id.list);
    mLvAdapter = new ExportListViewArrayAdapter(
      getContext(), R.layout.export_listview_item, new ArrayList<>());
    list.setAdapter(mLvAdapter);

    Calendar c = Calendar.getInstance();
    c.setTimeZone(TimeZone.getTimeZone("GMT"));
    c.setTime(new Date());
    int old = 0, idx = -1;
    List<DayEntry> works = mApp.getDaysFactory().list();
    for(DayEntry de : works) {
      int y = de.getDay().getYear();
      if(old != y) {
        old = y;
        spAdapter.add(String.valueOf(y));
        idx++;
        if(y == c.get(Calendar.YEAR)) mSpinner.setSelection(idx);
      }
    }
    mSpinner.setOnItemSelectedListener(this);
    reload(Integer.parseInt(mSpinner.getSelectedItem().toString()));
    return rootView;
  }

  /**
   * Called when the user click on a button (export).
   * @param v The clicked view.
   */
  @Override
  public void onClick(View v) {
    final List<ExportListViewArrayAdapter.ExportEntry> entries = mLvAdapter.getCheckedItems();
    if(entries.isEmpty()) {
      UIHelper.snack(getActivity(), getString(R.string.export_no_items));
      return;
    }
    final String email = mApp.getEMail();
    if(mApp.isExportMailEnabled() && email.isEmpty()) {
      UIHelper.snack(getActivity(), getString(R.string.export_email_not_set));
      return;
    }

    final File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
      getString(R.string.app_name) + "_" + entries.get(0).year + ".xls");
    if(file.exists()) {
      UIHelper.showConfirmDialog(getActivity(), getString(R.string.confirm),
        getString(R.string.the_file_exists_part_1) + " " + file.getName() + " " + getString(R.string.the_file_exists_part_2),
        (view) -> exportXLS(entries, email, file), null);
    } else
      exportXLS(entries, email, file);
  }

  /**
   * Exports the selected entries in a XLS file.
   * @param entries The entries to export.
   * @param email The email address used to send the file.
   * @param file The file name.
   */
  private void exportXLS(List<ExportListViewArrayAdapter.ExportEntry> entries, String email, File file) {
    try {
      ExcelHelper excel = new ExcelHelper(file);
      for(int idx = 0; idx < entries.size(); ++idx) {
        ExportListViewArrayAdapter.ExportEntry ee = entries.get(idx);
        Sheet sheet = excel.createSheet(AndroidHelper.getMonthString(ee.month));
        int row = 0, column = 0;

        List<String> headers = new ArrayList<>();
        headers.add(getString(R.string.date).replaceAll(" :", "")); // A -> 65
        headers.add(getString(R.string.type_morning).replaceAll(" :", "")); // B -> 66
        headers.add(getString(R.string.type_afternoon).replaceAll(" :", "")); // C -> 67
        headers.add(getString(R.string.hour_in)); // D -> 68
        headers.add(getString(R.string.hour_out)); // E -> 69
        headers.add(getString(R.string.break_time)); // F -> 70
        headers.add(getString(R.string.overtime)); // G -> 71
        if(!mApp.isExportHideWage())
          headers.add(getString(R.string.wage).replaceAll(" :", "")); // H -> 72
        excel.createHorizontalHeader(sheet, row, column, headers.toArray(new String[]{}));
        List<DayEntry> works = mApp.getDaysFactory().list();
        column = 0;
        row++;
        for (DayEntry de : works) {
          if (!de.getDay().isInMonth(ee.month + 1) || !de.getDay().isInYear(ee.year)) continue;
          excel.addLabel(sheet, row, column, de.getDay().dateString(), false);
          excel.addLabel(sheet, row, column+1, de.getTypeMorning() != DayType.AT_WORK ? de.getTypeMorning().string(getActivity()) : "", false);
          excel.addLabel(sheet, row, column+2, de.getTypeAfternoon() != DayType.AT_WORK ? de.getTypeAfternoon().string(getActivity()) : "", false);
          double wage = .0f;
          int column_offset= 3;
          if(de.getTypeMorning() != DayType.AT_WORK && de.getTypeAfternoon() != DayType.AT_WORK) {
            excel.addTime(sheet, row, column + (column_offset++), WorkTimeDay.timeString(0, 0), false);
            excel.addTime(sheet, row, column + (column_offset++), WorkTimeDay.timeString(0, 0), false);
            excel.addTime(sheet, row, column + (column_offset++), WorkTimeDay.timeString(0, 0), false);
            excel.addTime(sheet, row, column + (column_offset++), WorkTimeDay.timeString(0, 0), false);
          } else {
            excel.addTime(sheet, row, column + (column_offset++), de.getStartMorning().timeString(), false);
            excel.addTime(sheet, row, column + (column_offset++), de.getEndAfternoon().timeString(), false);
            excel.addTime(sheet, row, column + (column_offset++), de.getPause().timeString(), false);
            excel.addTime(sheet, row, column + (column_offset++), de.getOverTime().timeString(), false);
            if (!mApp.isExportHideWage())
              wage = de.getWorkTimePay(mApp.getAmountByHour());
          }
          if (!mApp.isExportHideWage())
            excel.addNumber(sheet, row++, column + (column_offset), wage);
        }
        column = 4;
        excel.addLabel(sheet, row, column++, getString(R.string.total), true);
        int length = (!mApp.isExportHideWage() ? 2 : 1);
        for (int i = 0; i < length; ++i) {
          char c = (char) (71 + i);
          StringBuilder sb = new StringBuilder();
          sb.append("SUM(").append(c).append(2).append(":").append(c).append(row).append(")");
          excel.addFormula(sheet, row, column++, sb);
        }
      }

      excel.write();
      Uri uri = Uri.fromFile(file);
      if(mApp.isExportMailEnabled()) {
        AndroidHelper.sentMailTo(getActivity(), email, uri,
          getString(R.string.export_email_subject),
          getString(R.string.export_email_body),
          getString(R.string.export_email_senderMsg));
        UIHelper.snack(getActivity(), getString(R.string.email_sent_to_with_mail) + " " + email);
      } else
        UIHelper.snack(getActivity(), getString(R.string.email_sent_to_without_mail));
      //if(file.exists()) file.delete();
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
      UIHelper.snack(getActivity(), getString(R.string.error) + ": " + e.getMessage());
    }
  }

  /**
   * Called when the user click on the dropdown list.
   * @param parentView See official javadoc.
   * @param selectedItemView See official javadoc.
   * @param position See official javadoc.
   * @param id See official javadoc.
   */
  @Override
  public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
    reload(Integer.parseInt(mSpinner.getSelectedItem().toString()));
  }

  /**
   * See official javadoc.
   * @param parentView See official javadoc.
   */
  @Override
  public void onNothingSelected(AdapterView<?> parentView) {
  }

  /**
   * Reloads the available month associated to the desired year.
   * @param year The desired year.
   */
  private void reload(int year) {
    mLvAdapter.clear();
    Locale locale = getResources().getConfiguration().getLocales().get(0);
    Map<String, DayEntry> map = mApp.getDaysFactory().toDaysMap();
    Calendar ctime = Calendar.getInstance();
    ctime.setTimeZone(TimeZone.getTimeZone("GMT"));
    ctime.setFirstDayOfWeek(Calendar.MONDAY);
    ctime.set(Calendar.YEAR, year);
    for(int i = 0; i < 12; ++i) {

      WorkTimeDay total = new WorkTimeDay();
      double pay = 0;
      ctime.set(Calendar.MONTH, i);
      ctime.set(Calendar.DAY_OF_MONTH, 1);
      int maxDay = ctime.getMaximum(Calendar.DATE);
      for(int day = 1; day <= maxDay; ++day) {
        ctime.set(Calendar.DAY_OF_MONTH, day);
        DayEntry de = map.get(String.format(Locale.US, "%02d/%02d/%04d", ctime.get(Calendar.DAY_OF_MONTH), ctime.get(Calendar.MONTH) + 1, ctime.get(Calendar.YEAR)));
        if(de != null && (de.getTypeMorning() == DayType.AT_WORK || de.getTypeAfternoon() == DayType.AT_WORK)) {
          pay += de.getWorkTimePay(mApp.getAmountByHour());
          total.addTime(de.getWorkTime());
        }
      }
      if(!total.isValidTime()) continue;
      String info = !mApp.isExportHideWage() ?
        String.format(locale, "%02d:%02d %s (%.02f %s)",
          total.getHours(), total.getMinutes(),
          getString(R.string.hours_lower_case),
          pay, mApp.getCurrency())
        :
        String.format(locale, "%02d:%02d %s",
          total.getHours(), total.getMinutes(),
          getString(R.string.hours_lower_case)) ;
      ExportListViewArrayAdapter.ExportEntry ee = new ExportListViewArrayAdapter.ExportEntry();
      ee.text = AndroidHelper.getMonthString(i);
      ee.info = info;
      ee.month = i;
      ee.year = year;
      mLvAdapter.add(ee);
    }
  }

}
