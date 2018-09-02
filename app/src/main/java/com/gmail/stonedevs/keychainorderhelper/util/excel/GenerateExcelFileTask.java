/*
 * Copyright 2018, Jared Shane Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.util.excel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.DateUtil;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtils;
import com.gmail.stonedevs.keychainorderhelper.util.OrderUtils;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.gmail.stonedevs.keychainorderhelper.util.ExcelUtils.getCellByAddress;

/**
 * Generates an Excel spreadsheet from a template.
 */
public class GenerateExcelFileTask extends AsyncTask<Void, Void, Uri> {

    private WeakReference<Activity> mContext;

    private ProgressDialog mProgressDialog;

    private CompleteOrder mOrder;

    private GenerateExcelFileCallback mCallback;

    public GenerateExcelFileTask( Activity context, CompleteOrder order,
                                  GenerateExcelFileCallback callback ) {
        mContext = new WeakReference<>( context );
        mProgressDialog = new ProgressDialog( context );

        mOrder = order;
        mCallback = callback;
    }

    private Context getContext() {
        return mContext.get();
    }

    @Override
    protected void onPreExecute() {
        mProgressDialog
                .setMessage( getContext().getString( R.string.dialog_message_send_order_progress_preparing ) );
        mProgressDialog.show();
        super.onPreExecute();
    }

    @Override
    protected Uri doInBackground( Void... voids ) {
        try {
            String filename = OrderUtils.getFilenameForTemplate( getContext(), mOrder.getOrderCategory() );
            Log.d( "GenerateExcelFileTask",
                    "doInBackground: " + filename + ", " + mOrder.getOrderCategory() );
            Workbook workbook = WorkbookFactory.create( getContext().getAssets().open( filename ) );
            return generateExcelFile( workbook );
        } catch ( InvalidFormatException | IOException e ) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute( Uri uri ) {
        mProgressDialog.dismiss();

        if ( uri == null ) {
            mCallback.onFileGenerationFail();
        } else {
            mCallback.onFileGenerationSuccess( uri );
        }

        super.onPostExecute( uri );
    }

    private Uri generateExcelFile( Workbook workbook ) throws IOException {
        String storeName = mOrder.getStoreName();
        Date orderDate = mOrder.getOrderDate();

        Sheet sheet = workbook.getSheetAt( 0 );

        int orderCategory = mOrder.getOrderCategory();

        //  Get Bill To values
        String[] billToValues = OrderUtils.getBillToDetails( getContext(), orderCategory );

        //  Write Bill To
        String[] billToCellLocations = OrderUtils.getBillToLocations( getContext(), orderCategory );
        for ( int i = 0; i < billToCellLocations.length; i++ ) {
            String cellValue = billToValues[i];
            String cellLocation = billToCellLocations[i];
            getCellByAddress( sheet, cellLocation ).setCellValue( cellValue );
        }

        //  Write Store Name and Number.
        String[] storeNameCellLocations = OrderUtils.getStoreNameLocations( getContext(), orderCategory );
        for ( String cellLocation : storeNameCellLocations ) {
            getCellByAddress( sheet, cellLocation ).setCellValue( storeName );
        }

        //  Write Rep Name.
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( getContext() );
        String repName = prefs.getString( getContext().getString( R.string.pref_key_rep_name ),
                getContext().getString( R.string.pref_error_default_value_rep_name ) );
        String repTerritory = mOrder.hasOrderTerritory() ? mOrder.getOrderTerritory()
                : prefs.getString( getContext().getString( R.string.pref_key_rep_territory ),
                getContext().getString( R.string.pref_error_default_value_rep_territory ) );

        String[] repNameCellLocations = OrderUtils.getRepNameLocations( getContext(), orderCategory );
        for ( String cellLocation : repNameCellLocations ) {
            String repFormat = String
                    .format( getContext().getString( R.string.string_format_repName_repTerritory ), repName,
                            repTerritory );
            getCellByAddress( sheet, cellLocation ).setCellValue( repFormat );
        }

        //  Write Current Date.
        String dateFormat = DateUtil.getFormattedDateForLayout( orderDate );
        String[] dateCellLocations = OrderUtils.getOrderDateLocations( getContext(), orderCategory );
        for ( String cellLocation : dateCellLocations ) {
            getCellByAddress( sheet, cellLocation ).setCellValue( dateFormat );
        }

        //  Write Order Item data.
        String[] itemNameCellLocations = OrderUtils.getItemNameLocations( getContext(), orderCategory );
        String[] itemQuantityCellLocations = OrderUtils
                .getItemQuantityLocations( getContext(), orderCategory );

        List<OrderItem> orderItems = mOrder.getOrderItems();
        for ( int i = 0; i < orderItems.size(); i++ ) {
            OrderItem orderItem = orderItems.get( i );

            if ( orderItem != null ) {
                Cell nameCell = ExcelUtils.getCellByAddress( sheet, itemNameCellLocations[i] );
                nameCell.setCellValue( orderItem.getName() );

                Cell quantityCell = ExcelUtils.getCellByAddress( sheet, itemQuantityCellLocations[i] );
                int quantity = orderItem.getQuantity();
                if ( quantity > 0 ) {
                    quantityCell.setCellValue( quantity );
                } else {
                    quantityCell.setCellValue( "" );
                }
            }
        }

        SimpleDateFormat format = new SimpleDateFormat(
                getContext().getString( R.string.string_date_filename ), Locale.getDefault() );
        String filenameDateFormat = format.format( orderDate );
        String filename = String.format( OrderUtils.getFilename( getContext(), orderCategory ),
                repTerritory.toLowerCase(),
                storeName.toLowerCase().replaceAll( "[^a-zA-Z0-9.\\-]", "_" ),
                filenameDateFormat );

        File file = new File(
                getContext().getExternalFilesDir( Environment.DIRECTORY_DOCUMENTS ), filename );

        FileOutputStream out = new FileOutputStream( file );
        workbook.write( out );
        out.close();

        return GenerateExcelFileProvider
                .getUriForFile( getContext(), BuildConfig.APPLICATION_ID + ".provider", file );
    }
}