/*
 * Copyright (c) 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.ui.prepareorder;

import static com.gmail.stonedevs.keychainorderhelper.util.ExcelUtils.getCellByAddress;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder.OrderType;
import com.gmail.stonedevs.keychainorderhelper.util.DateUtil;
import com.gmail.stonedevs.keychainorderhelper.util.EmailUtils;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * TODO: Add a class header comment!
 */

public class PrepareSendActionIntentAsyncTask extends AsyncTask<Void, Void, Intent> {

  private WeakReference<Activity> mContext;

  private ProgressDialog mProgressDialog;

  private CompleteOrder mOrder;

  private PrepareIntentCallback mCallback;

  public PrepareSendActionIntentAsyncTask(Activity context, CompleteOrder order,
      PrepareIntentCallback callback) {
    mContext = new WeakReference<>(context);
    mProgressDialog = new ProgressDialog(context);

    mOrder = order;
    mCallback = callback;
  }

  private Context getContext() {
    return mContext.get();
  }

  @Override
  protected void onPreExecute() {
    mProgressDialog
        .setMessage(getContext().getString(R.string.dialog_message_send_order_progress_preparing));
    mProgressDialog.show();
    super.onPreExecute();
  }

  @Override
  protected Intent doInBackground(Void... voids) {
    try {
      Uri uri = null;
      if (mOrder.getOrderType() == OrderType.ORDER) {
        Workbook workbook = WorkbookFactory.create(getContext().getAssets().open(
            getContext().getString(R.string.excel_template_filename)));

        File file = generateExcelFile(workbook);
        uri = Uri.fromFile(file);
      }

      return EmailUtils.createSendOrderEmailIntent(getContext(), mOrder, uri);
    } catch (InvalidFormatException | IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  protected void onPostExecute(Intent intent) {
    mProgressDialog.dismiss();

    mCallback.onIntentReadyForAction(intent);
    super.onPostExecute(intent);
  }

  private File generateExcelFile(Workbook workbook) throws IOException {
    String storeName = mOrder.getStoreName();
    Date orderDate = mOrder.getOrderDate();

    Sheet sheet = workbook.getSheetAt(0);

    //  Write Store Name and Number.
    String[] storeNameCellLocations = getContext().getResources()
        .getStringArray(R.array.excel_cell_locations_store_number);
    for (String cellLocation : storeNameCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(storeName);
    }

    //  Write Rep Name.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    String repName = prefs.getString(getContext().getString(R.string.pref_key_rep_name),
        getContext().getString(R.string.pref_error_default_value_rep_name));
    String repTerritory = mOrder.hasOrderTerritory() ? mOrder.getOrderTerritory()
        : prefs.getString(getContext().getString(R.string.pref_key_rep_territory),
            getContext().getString(R.string.pref_error_default_value_rep_territory));

    String[] repNameCellLocations = getContext().getResources()
        .getStringArray(R.array.excel_cell_locations_rep_name);
    for (String cellLocation : repNameCellLocations) {
      String repFormat = String
          .format(getContext().getString(R.string.string_format_repName_repTerritory), repName,
              repTerritory);
      getCellByAddress(sheet, cellLocation).setCellValue(repFormat);
    }

    //  Write Current Date.
    String dateFormat = DateUtil.getFormattedDateForLayout(orderDate);
    String[] dateCellLocations = getContext().getResources()
        .getStringArray(R.array.excel_cell_locations_order_date);
    for (String cellLocation : dateCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(dateFormat);
    }

    //  Write Order Item data.
    String[] keychainNameCellLocations = getContext().getResources()
        .getStringArray(R.array.excel_cell_locations_names);
    String[] keychainQuantityCellLocations = getContext().getResources()
        .getStringArray(R.array.excel_cell_locations_quantities);

    List<OrderItem> orderItems = mOrder.getOrderItems();
    for (int i = 0; i < orderItems.size(); i++) {
      OrderItem orderItem = orderItems.get(i);

      if (orderItem != null) {
        Cell nameCell = ExcelUtils.getCellByAddress(sheet, keychainNameCellLocations[i]);
        nameCell.setCellValue(orderItem.getName());

        Cell quantityCell = ExcelUtils.getCellByAddress(sheet, keychainQuantityCellLocations[i]);
        int quantity = orderItem.getQuantity();
        if (quantity > 0) {
          quantityCell.setCellValue(quantity);
        } else {
          quantityCell.setCellValue("");
        }
      }
    }

    SimpleDateFormat format = new SimpleDateFormat(
        getContext().getString(R.string.string_date_filename), Locale.getDefault());
    String filenameDateFormat = format.format(orderDate);
    String filename = String.format(getContext().getString(R.string.string_format_filename),
        repTerritory.toLowerCase(),
        storeName.toLowerCase().replaceAll("[^a-zA-Z0-9.\\-]", "_"),
        filenameDateFormat);

    File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
        String.format(getContext().getString(R.string.string_format_filename_with_suffix),
            filename));

    FileOutputStream out = new FileOutputStream(file);
    workbook.write(out);
    out.close();

    // Tell the media scanner about the new file so that it is
    // immediately available to the user.
    MediaScannerConnection.scanFile(getContext(),
        new String[]{file.toString()}, null,
        new MediaScannerConnection.OnScanCompletedListener() {
          public void onScanCompleted(String path, Uri uri) {
            Log.i("ExternalStorage", "Scanned " + path + ":");
            Log.i("ExternalStorage", "-> uri=" + uri);
          }
        });

    return file;
  }
}