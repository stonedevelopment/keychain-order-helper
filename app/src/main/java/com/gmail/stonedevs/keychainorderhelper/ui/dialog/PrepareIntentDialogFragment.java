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

package com.gmail.stonedevs.keychainorderhelper.ui.dialog;

import static android.content.Intent.EXTRA_STREAM;
import static com.gmail.stonedevs.keychainorderhelper.util.ExcelUtils.getCellByAddress;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * AlertDialog that will let the User know the order is being generated, intent being prepared, and
 * will dismiss when complete to allow calling Activity to start email intent for result
 */

public class PrepareIntentDialogFragment extends DialogFragment {

  private static final String TAG = PrepareIntentDialogFragment.class.getSimpleName();

  public static final int REQUEST_CODE_ACTION_SEND = 1;

  private OrderSentListener mListener;

  private AppExecutors mAppExecutors;

  private CompleteOrder mOrder;

  private Uri mUri;

  public interface OrderSentListener {

    void onOrderSent();

    void onOrderNotSent();

    void onOrderNotSend_NoAppsForIntent();
  }

  public interface GenerateExcelFileCallback {

    void onFileGenerationComplete(File file);
  }

  public PrepareIntentDialogFragment() {
    //  Empty constructor required for DialogFragment.
  }

  public static PrepareIntentDialogFragment createInstance() {
    return new PrepareIntentDialogFragment();
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new Builder(getActivity(),
        R.layout.dialog_prepare_email_intent);

    return builder.create();
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    prepareOrderToSend();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_ACTION_SEND) {
      orderSent();
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    cancelOrder();
  }

  public void setData(CompleteOrder order) {
    mOrder = order;
  }

  public void setListener(OrderSentListener listener) {
    mListener = listener;
  }

  void prepareOrderToSend() {
    try {
      Workbook workbook = WorkbookFactory.create(getActivity().getAssets().open(
          getString(R.string.excel_template_filename)));

      startRunnerForGenerateExcelFile(workbook, new GenerateExcelFileCallback() {
        @Override
        public void onFileGenerationComplete(File file) {
          //  prepare SEND_ACTION intent with email details
          Intent intent = createEmailIntent(file);

          Intent chooser = Intent
              .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

          if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
          } else {
            //  there are no apps on phone to handle this intent, cancel order
            cancelOrderWithNoAppsForIntent();
          }
        }
      });
    } catch (InvalidFormatException | IOException e) {
      e.printStackTrace();
    }
  }

  private void startRunnerForGenerateExcelFile(final Workbook workbook,
      final GenerateExcelFileCallback callback) {

    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        try {
          File file = generateExcelFile(workbook);
          callback.onFileGenerationComplete(file);
        } catch (IOException | InvalidFormatException | ParseException e) {
          e.printStackTrace();
        }
      }
    };

    //  run on disk thread
    new AppExecutors().diskIO().execute(runnable);
  }

  private File generateExcelFile(Workbook workbook)
      throws IOException, InvalidFormatException, ParseException {
    String storeName = mOrder.getStoreName();
    Date orderDate = mOrder.getOrderDate();

    Sheet sheet = workbook.getSheetAt(0);

    //  Write Store Name and Number.
    String[] storeNameCellLocations = getResources()
        .getStringArray(R.array.excel_cell_locations_store_number);
    for (String cellLocation : storeNameCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(storeName);
    }

    //  Write Rep Name.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String repName = prefs.getString(getString(R.string.pref_key_rep_name),
        getString(R.string.pref_error_default_value_rep_name));
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory),
        getString(R.string.pref_error_default_value_rep_territory));
    String[] repNameCellLocations = getResources()
        .getStringArray(R.array.excel_cell_locations_rep_name);
    for (String cellLocation : repNameCellLocations) {
      String repFormat = String
          .format(getString(R.string.string_format_repName_repTerritory), repName, repTerritory);
      getCellByAddress(sheet, cellLocation).setCellValue(repFormat);
    }

    //  Write Current Date.
    String[] dateCellLocations = getResources()
        .getStringArray(R.array.excel_cell_locations_order_date);
    for (String cellLocation : dateCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(orderDate);
    }

//    ArrayList<Integer> orderQuantities = new ArrayList<>(0);
//    for (int i = 0; i < blankList.size(); i++) {
//      OrderItem orderItem = blankList.get(i);
//
//      if (orderItem != null) {
//        // TODO: 2/12/2018 How to pull cellId data for cellLocation details?
//        CellAddress cellAddress = orderItem.getQuantityLocation();
//        int rowIndex = cellAddress.getRow();
//        int columnIndex = cellAddress.getColumn();
//
//        Row row = sheet.getRow(rowIndex);
//        Cell cell = row.getCell(columnIndex);
//        if (cell == null) {
//          cell = row.createCell(columnIndex, CellType.BLANK);
//        }
//
//        if (orderItem.getQuantity() > 0) {
//          cell.setCellValue(orderItem.getQuantity());
//        } else {
//          cell.setCellValue("");
//        }
//
//        Integer quantity = orderItem.getQuantity();
//        orderQuantities.add(quantity);
//      }
//    }

    SimpleDateFormat format = new SimpleDateFormat(getString(R.string.string_date_filename),
        Locale.getDefault());
    String filenameDateFormat = format.format(orderDate);

    File file = new File(getActivity().getExternalFilesDir(null),
        String.format(getString(R.string.string_format_filename), repTerritory.toLowerCase(),
            storeName.toLowerCase(), filenameDateFormat));
    FileOutputStream out = new FileOutputStream(file);
    workbook.write(out);
    out.close();

    return file;
  }

  private Intent createEmailIntent(File file) {
    String storeName = mOrder.getStoreName();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory),
        getString(R.string.pref_key_rep_territory));

    Intent intent = new Intent(Intent.ACTION_SEND);

    // set the type to 'email'
    intent.setType("vnd.android.cursor.dir/email");

    //  set email address from preferences
    String sendtoEmail =
        BuildConfig.DEBUG ? getString(R.string.pref_debug_default_value_sendto_email)
            : getString(R.string.pref_default_value_sendto_email);
    String to[] = {sendtoEmail};
    intent.putExtra(Intent.EXTRA_EMAIL, to);

    // the attachment
    Uri path = Uri.fromFile(file);
    intent.putExtra(EXTRA_STREAM, path);
    mUri = path;

    // the mail subject
    String subject = String
        .format(getString(R.string.string_format_email_subject), repTerritory, storeName);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    //  the mail body
    String body = String
        .format(getString(R.string.intent_extra_text_body_send_order_by_email), storeName);
    intent.putExtra(Intent.EXTRA_TEXT, body);

    return intent;
  }

  private void orderSent() {
    //  Tell listener of success.
    mListener.onOrderSent();

    //  Dismiss dialog, return to previous screen.
    dismiss();
  }

  private void cancelOrder() {
    //  Tell listener of failure
    mListener.onOrderNotSent();

    //  Dismiss dialog, return to previous screen.
    dismiss();
  }

  private void cancelOrderWithNoAppsForIntent() {
    //  Tell listener of failure
    mListener.onOrderNotSend_NoAppsForIntent();

    //  Dismiss dialog, return to previous screen.
    dismiss();
  }
}