package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelUtils {

  private static final String TAG = ExcelUtils.class.getSimpleName();

  public static void GenerateStringArrayFormat(Context c)
      throws IOException, InvalidFormatException {
    Workbook workbook = WorkbookFactory.create(c.getAssets().open(
        c.getString(R.string.excel_template_filename)));

    Sheet sheet = workbook.getSheetAt(0);

    echoSectionCellAddresses(c, sheet, 2, 13, 54);
    echoSectionCellAddresses(c, sheet, 5, 13, 54);
    echoSectionCellAddresses(c, sheet, 8, 13, 54);
    echoSectionCellAddresses(c, sheet, 2, 72, 113);
    echoSectionCellAddresses(c, sheet, 5, 72, 113);
    echoSectionCellAddresses(c, sheet, 8, 72, 113);
  }


  private static void echoSectionNames(Context c, Sheet sheet, int col, int beginRow,
      int endRow) {
    List<String> items = new ArrayList<>();

    for (int i = beginRow; i <= endRow; i++) {
      Row row = sheet.getRow(i);

      if (row != null) {
        Cell cell = row.getCell(col);

        if (cell != null) {
          String text = cell.getStringCellValue();
          String formattedItemText = String
              .format(c.getString(R.string.string_format_stringarray_item), text);
          items.add(formattedItemText);
        }
      }
    }

    StringBuilder line = new StringBuilder();
    for (String item : items) {
      line.append(item);
    }
    Log.d(TAG, "echoSectionNames: " + line);
  }

  private static void echoSectionCellAddresses(Context c, Sheet sheet, int col, int beginRow,
      int endRow) {
    List<String> items = new ArrayList<>();

    for (int i = beginRow; i <= endRow; i++) {
      Row row = sheet.getRow(i);

      if (row != null) {
        Cell cell = row.getCell(col);

        if (cell != null) {
          String text = cell.getAddress().formatAsString();
          String formattedItemText = String
              .format(c.getString(R.string.string_format_stringarray_item), text);
          items.add(formattedItemText);
        }
      }
    }

    StringBuilder line = new StringBuilder();
    for (String item : items) {
      line.append(item);
    }
    Log.d(TAG, "GenerateStringArrayFormat: " + line);
  }

  public CellRangeAddress getCellRangeAddress(Context c, int arrayResource) {
    String[] range = c.getResources().getStringArray(arrayResource);
    CellAddress firstCellAddress = new CellAddress(range[0]);
    CellAddress lastCellAddress = new CellAddress(range[1]);
    int firstRow = firstCellAddress.getRow();
    int lastRow = lastCellAddress.getRow();
    int column = firstCellAddress.getColumn();

    return new CellRangeAddress(firstRow, lastRow, column, column);
  }

  public static Cell getCellByAddress(Sheet sheet, String cellLocation) {
    CellAddress cellAddress = new CellAddress(cellLocation);
    int row = cellAddress.getRow();
    int col = cellAddress.getColumn();
    Cell cell = sheet.getRow(row).createCell(col);
    if (cell == null) {
      cell = sheet.getRow(row).createCell(col);
    }
    return cell;
  }

}
