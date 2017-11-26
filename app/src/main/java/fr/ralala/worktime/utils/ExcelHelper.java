package fr.ralala.worktime.utils;

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
  private CellStyle mTimesDateBold;
  private CellStyle mTimesDate;
  private CellStyle mTimesBold;
  private CellStyle mTimes;
  private Workbook mWorkbook = null;
  private File mFile = null;
  private CreationHelper mCreateHelper = null;

  public ExcelHelper(final File file) {
    mFile = file;
    mWorkbook = new HSSFWorkbook();  // or new XSSFWorkbook();
    mCreateHelper = mWorkbook.getCreationHelper();
    Font times10pt = mWorkbook.createFont();
    times10pt.setFontHeightInPoints((short)10);
    times10pt.setFontName("Times");
    Font times10ptBold = mWorkbook.createFont();
    times10ptBold.setFontHeightInPoints((short)10);
    times10ptBold.setFontName("Times");
    times10ptBold.setBold(true);
    DataFormat df = mWorkbook.createDataFormat();
    mTimes = mWorkbook.createCellStyle();
    mTimes.setFont(times10pt);
    mTimesBold = mWorkbook.createCellStyle();
    mTimesBold.setFont(times10ptBold);
    mTimesDate = mWorkbook.createCellStyle();
    mTimesDate.setFont(times10pt);
    mTimesDate.setDataFormat(df.getFormat("[h]:mm;@"));

    mTimesDateBold = mWorkbook.createCellStyle();
    mTimesDateBold.setFont(times10ptBold);
    mTimesDateBold.setDataFormat(df.getFormat("[h]:mm;@"));
  }

  public Sheet createSheet(final String sheetTitle) {
    mWorkbook.createSheet(sheetTitle);
    return mWorkbook.getSheet(sheetTitle);
  }

  public void write() throws IOException {
    FileOutputStream fileOut = new FileOutputStream(mFile);
    mWorkbook.write(fileOut);
    fileOut.close();
    mWorkbook.close();
  }

  public void createHorizontalHeader(Sheet sheet, int row, int column, String[] headers) {
    for(String header : headers)
      addLabel(sheet, row, column++, header, true);
  }

  public void addFormula(Sheet sheet, int row, int column, StringBuilder formula) {
    addFormula(sheet, row, column, formula.toString());
  }

  private void addFormula(Sheet sheet, int row, int column, String formula) {
    Cell cell = getCell(sheet, row, column);
    cell.setCellStyle(mTimesDateBold);
    cell.setCellFormula(formula);
  }

  public void addLabel(Sheet sheet, int row, int column, String s, boolean bold) {
    Cell cell = getCell(sheet, row, column);
    cell.setCellValue(mCreateHelper.createRichTextString(s));
    cell.setCellStyle(bold ? mTimesBold : mTimes);
  }

  public void addTime(Sheet sheet, int row, int column, String s, boolean bold) throws ParseException {
    Cell cell = getCell(sheet, row, column);
    cell.setCellFormula("TIME(" + s.replaceAll(":", ",") + ",00)"); // 00:00:00
    cell.setCellStyle(bold ? mTimesDateBold : mTimesDate);
  }

  public void addNumber(Sheet sheet, int row, int column,
                         double val) {
    Cell cell = getCell(sheet, row, column);
    cell.setCellValue(val);
    cell.setCellStyle(mTimes);
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
