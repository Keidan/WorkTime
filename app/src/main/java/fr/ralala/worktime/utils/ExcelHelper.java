package fr.ralala.worktime.utils;

import android.content.Context;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;


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
  private CellStyle timesDateBold;
  private CellStyle timesDate;
  private CellStyle timesBold;
  private CellStyle times;
  private Workbook workbook = null;
  private File file = null;
  private CreationHelper createHelper = null;

  public ExcelHelper(final Context ctx, final File file) {
    this.file = file;
    workbook = new HSSFWorkbook();  // or new XSSFWorkbook();
    createHelper = workbook.getCreationHelper();
    Font times10pt = workbook.createFont();
    times10pt.setFontHeightInPoints((short)10);
    times10pt.setFontName("Times");
    Font times10ptBold = workbook.createFont();
    times10ptBold.setFontHeightInPoints((short)10);
    times10ptBold.setFontName("Times");
    times10ptBold.setBold(true);
    DataFormat df = workbook.createDataFormat();
    times = workbook.createCellStyle();
    times.setFont(times10pt);
    timesBold = workbook.createCellStyle();
    timesBold.setFont(times10ptBold);
    timesDate = workbook.createCellStyle();
    timesDate.setFont(times10pt);
    timesDate.setDataFormat(df.getFormat("[h]:mm;@"));

    timesDateBold = workbook.createCellStyle();
    timesDateBold.setFont(times10ptBold);
    timesDateBold.setDataFormat(df.getFormat("[h]:mm;@"));
  }

  public Sheet createSheet(final String sheetTitle) {
    workbook.createSheet(sheetTitle);
    return workbook.getSheet(sheetTitle);
  }

  public void write() throws IOException {
    FileOutputStream fileOut = new FileOutputStream(file);
    workbook.write(fileOut);
    fileOut.close();
    workbook.close();
  }

  public int createHorizontalHeader(Sheet sheet, int row, int column, String[] headers) {
    for(String header : headers)
      addLabel(sheet, row, column++, header, true);
    return column;
  }

  public void addFormula(Sheet sheet, int row, int column, StringBuilder formula, boolean bold, boolean time) {
    addFormula(sheet, row, column, formula.toString(), bold, time);
  }

  private void addFormula(Sheet sheet, int row, int column, String formula, boolean bold, boolean time) {
    Cell cell = getCell(sheet, row, column);
    if(!time)
      cell.setCellStyle(bold ? timesBold : times);
    else
      cell.setCellStyle(bold ? timesDateBold : timesDate);
    cell.setCellFormula(formula);
  }

  public void addLabel(Sheet sheet, int row, int column, String s, boolean bold) {
    Cell cell = getCell(sheet, row, column);
    cell.setCellValue(createHelper.createRichTextString(s));
    cell.setCellStyle(bold ? timesBold : times);
  }

  public void addTime(Sheet sheet, int row, int column, String s, boolean bold) throws ParseException {
    Cell cell = getCell(sheet, row, column);
    cell.setCellFormula("TIME(" + s.replaceAll(":", ",") + ",00)"); // 00:00:00
    cell.setCellStyle(bold ? timesDateBold : timesDate);
  }

  public void addNumber(Sheet sheet, int row, int column,
                         double val, boolean bold) {
    Cell cell = getCell(sheet, row, column);
    cell.setCellValue(val);
    cell.setCellStyle(bold ? timesBold : times);
  }

  private Cell getCell(Sheet sheet, int row, int column) {
    Iterator<Row> itr = sheet.rowIterator();
    Row r = null;
    Cell c = null;
    if (itr.hasNext()) {
      while (itr.hasNext()) {
        Row rr = itr.next();
        if (rr.getRowNum() == row) {
          r = rr;
          Iterator<Cell> itc = r.cellIterator();
          if (itc.hasNext()) {
            while (itc.hasNext()) {
              Cell cc = itc.next();
              if (cc.getColumnIndex() == column) {
                c = cc;
                break;
              }
            }
          }
          break;
        }
      }
    }
    return c == null ? ((r == null ? sheet.createRow(row) : r).createCell(column)) : c;
  }

}
