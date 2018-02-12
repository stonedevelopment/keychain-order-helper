package com.gmail.stonedevs.keychainorderhelper.util;

import static com.gmail.stonedevs.keychainorderhelper.view.NewOrderFragment.TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.Keychain;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;

public class ExcelUtil {

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
    Log.d(TAG, "GenerateStringArrayFormat: " + line);
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

  public static File generateExcelFile(Context c, Workbook workbook, String storeName,
      Date orderDate,
      List<Keychain> items)
      throws IOException, InvalidFormatException, ParseException {
    Sheet sheet = workbook.getSheetAt(0);

    //  Write Store Name and Number.
    String[] storeNameCellLocations = c.getResources()
        .getStringArray(R.array.excel_cell_locations_store_number);
    for (String cellLocation : storeNameCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(storeName);
    }

    //  Write Rep Name.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
    String repName = prefs.getString(c.getString(R.string.pref_key_rep_name),
        c.getString(R.string.pref_error_default_value_rep_name));
    String repTerritory = prefs.getString(c.getString(R.string.pref_key_rep_territory),
        c.getString(R.string.pref_error_default_value_rep_territory));
    String[] repNameCellLocations = c.getResources()
        .getStringArray(R.array.excel_cell_locations_rep_name);
    for (String cellLocation : repNameCellLocations) {
      String repFormat = String
          .format(c.getString(R.string.string_format_repName_repTerritory), repName, repTerritory);
      getCellByAddress(sheet, cellLocation).setCellValue(repFormat);
    }

    //  Write Current Date.
    String[] dateCellLocations = c.getResources()
        .getStringArray(R.array.excel_cell_locations_order_date);
    for (String cellLocation : dateCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(orderDate);
    }

    Integer orderTotal = 0;
    ArrayList<Integer> orderQuantities = new ArrayList<>(0);
    for (int i = 0; i < items.size(); i++) {
      Keychain item = items.get(i);

      if (item != null) {
        CellAddress cellAddress = item.getQuantityLocation();
        int rowIndex = cellAddress.getRow();
        int columnIndex = cellAddress.getColumn();

        Row row = sheet.getRow(rowIndex);
        Cell cell = row.getCell(columnIndex);
        if (cell == null) {
          cell = row.createCell(columnIndex, CellType.BLANK);
        }

        if (item.getQuantity() > 0) {
          cell.setCellValue(item.getQuantity());
        } else {
          cell.setCellValue("");
        }

        Integer quantity = item.getQuantity();
        orderQuantities.add(quantity);
        orderTotal += quantity;
      }
    }

    SimpleDateFormat format = new SimpleDateFormat(c.getString(R.string.string_date_filename),
        Locale.getDefault());
    String filenameDateFormat = format.format(orderDate);

    File file = new File(c.getExternalFilesDir(null),
        String.format(c.getString(R.string.string_format_filename), repTerritory.toLowerCase(),
            storeName.toLowerCase(), filenameDateFormat));
    FileOutputStream out = new FileOutputStream(file);
    workbook.write(out);
    out.close();

    return file;
  }
}
