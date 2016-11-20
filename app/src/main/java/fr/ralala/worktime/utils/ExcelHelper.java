package fr.ralala.worktime.utils;

import android.content.Context;

import java.io.File;
import java.io.IOException;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 *******************************************************************************
 * <p><b>Project WorkTime</b><br/>
 * Helper functions for the Excel API
 * </p>
 * @author Keidan
 *
 *******************************************************************************
 */
public class ExcelHelper {
  private WritableCellFormat timesBold;
  private WritableCellFormat times;
  private WritableWorkbook workbook = null;

  public ExcelHelper(final Context ctx, final File file) throws IOException, WriteException{
    WorkbookSettings wbSettings = new WorkbookSettings();
    wbSettings.setLocale(ctx.getResources().getConfiguration().locale);
    workbook = Workbook.createWorkbook(file, wbSettings);
    initFonts();
  }

  public WritableSheet createSheet(final String sheetTitle, int index) {
    workbook.createSheet(sheetTitle, index);
    return workbook.getSheet(index);
  }

  public void write() throws IOException, WriteException {
    workbook.write();
    workbook.close();
  }

  private void initFonts()  throws WriteException {
    // Lets create a times font
    WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
    // Define the cell format
    times = new WritableCellFormat(times10pt);
    // Lets automatically wrap the cells
    times.setWrap(true);

    // create create a bold font with unterlines
    WritableFont times10ptBold = new WritableFont(
      WritableFont.TIMES, 10, WritableFont.BOLD, false);
    timesBold = new WritableCellFormat(times10ptBold);
    // Lets automatically wrap the cells
    timesBold.setWrap(true);
    CellView cv = new CellView();
    cv.setFormat(times);
    cv.setFormat(timesBold);
    cv.setAutosize(true);
  }

  public int createVerticalHeader(WritableSheet sheet, int row, int column, String[] headers)  throws WriteException {
    for(String header : headers)
      addLabel(sheet, row++, column, header, true);
    return row;
  }

  public int createHorizontalHeader(WritableSheet sheet, int row, int column, String[] headers)  throws WriteException {
    for(String header : headers)
      addLabel(sheet, row, column++, header, true);
    return column;
  }

  public void addFormula(WritableSheet sheet, int row, int column, StringBuilder formula) throws WriteException {
    addFormula(sheet, row, column, formula.toString());
  }

  public void addFormula(WritableSheet sheet, int row, int column, String formula) throws WriteException {
    Formula f = new Formula(column, row, formula);
    sheet.addCell(f);
  }

  public void addLabel(WritableSheet sheet, int row, int column, String s, boolean bold) throws WriteException {
    Label label;
    label = new Label(column, row, s, bold ? timesBold : times);
    sheet.addCell(label);
  }

  public void addNumber(WritableSheet sheet, int row, int column,
                         Integer integer, boolean bold) throws WriteException {
    Number number;
    number = new Number(column, row, integer, bold ? timesBold : times);
    sheet.addCell(number);
  }

}
