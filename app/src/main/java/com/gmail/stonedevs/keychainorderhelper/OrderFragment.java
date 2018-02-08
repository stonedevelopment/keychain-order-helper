package com.gmail.stonedevs.keychainorderhelper;

import static com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil.getCellByAddress;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.json.JSONOrderEntry;
import com.gmail.stonedevs.keychainorderhelper.json.JSONOrderEntryList;
import com.gmail.stonedevs.keychainorderhelper.util.JSONUtil;
import com.gmail.stonedevs.keychainorderhelper.util.Util;
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

public class OrderFragment extends BackHandledFragment {

  public static final String TAG = OrderFragment.class.getSimpleName();

  private EditText mEditStoreName;
  private EditText mEditOrderDate;

  private RecyclerView mRecyclerView;
  private OrderAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;

  private boolean mSentEmail;

  public OrderFragment() {
    // Required empty public constructor
  }

  public static OrderFragment newInstance() {
    OrderFragment fragment = new OrderFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //  reset sent email toggle
    mSentEmail = false;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_order, container, false);

    String defaultStoreName = BuildConfig.DEBUG ? "Store Name 1" : "";
    String defaultOrderDate = BuildConfig.DEBUG ? Util.getFormattedDateForLayout() : "";

    String storeName = defaultStoreName;
    String orderDate = defaultOrderDate;
    ArrayList<Integer> quantityCellValues = new ArrayList<>(0);

    if (savedInstanceState != null) {
      storeName = savedInstanceState
          .getString(getString(R.string.bundle_key_orderFragment_savedInstanceState_store_name), defaultStoreName);
      orderDate = savedInstanceState
          .getString(getString(R.string.bundle_key_orderFragment_savedInstanceState_order_date), defaultOrderDate);
      quantityCellValues = savedInstanceState
          .getIntegerArrayList(getString(R.string.bundle_key_orderFragment_savedInstanceState_quantity_cell_values));
    }

    mEditStoreName = view.findViewById(R.id.editStoreName);
    mEditStoreName.setText(storeName);

    mEditOrderDate = view.findViewById(R.id.editOrderDate);
    if (orderDate.isEmpty()) {
      orderDate = Util.getFormattedDateForLayout();
    }
    mEditOrderDate.setText(orderDate);

    mRecyclerView = view.findViewById(R.id.orderRecyclerView);

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        mLayoutManager.getOrientation());
    mRecyclerView.addItemDecoration(dividerItemDecoration);

    //  Populate names by string-array
    List<OrderItem> items = new ArrayList<>(0);
    String[] names = getResources().getStringArray(R.array.excel_cell_values_names);
    String[] addresses = getResources().getStringArray(R.array.excel_cell_locations_quantities);

    //  if quantity cell values persisted through saveInstanceState, fill quantities
    //  else, fill with default 0
    if (quantityCellValues != null && quantityCellValues.size() == names.length) {
      for (int i = 0; i < names.length; i++) {
        int quantity = quantityCellValues.get(i);
        items.add(new OrderItem(names[i], quantity, new CellAddress(addresses[i])));
      }
    } else {
      for (int i = 0; i < names.length; i++) {
        items.add(new OrderItem(names[i], 0, new CellAddress(addresses[i])));
      }
    }

    mAdapter = new OrderAdapter(getActivity());
    mAdapter.bindItems(items);

    mRecyclerView.setAdapter(mAdapter);

    Button resetOrderButton = view.findViewById(R.id.resetButton);
    resetOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.dialog_title_reset_order);
        builder.setMessage(
            R.string.dialog_message_reset_order);
        builder.setPositiveButton(R.string.dialog_positive_button_reset_order,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                resetOrder();
              }
            });
        builder.setNegativeButton(R.string.dialog_negative_button_reset_order,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
              }
            });
        builder.show();
      }
    });

    Button sendOrderButton = view.findViewById(R.id.sendButton);
    sendOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        final boolean isStoreNameEmpty = mEditStoreName.getText().toString().isEmpty();
        final boolean isDateEmpty = mEditOrderDate.getText().toString().isEmpty();
        final boolean areCellsEmpty = areCellsEmpty();

        if (isStoreNameEmpty || isDateEmpty || areCellsEmpty) {
          List<String> messages = new ArrayList<>(0);

          if (isStoreNameEmpty) {
            messages.add(getString(R.string.dialog_message_incomplete_order_store_name_empty));
          }
          if (isDateEmpty) {
            messages.add(getString(R.string.dialog_message_incomplete_order_date_empty));
          }
          if (areCellsEmpty) {
            messages.add(getString(R.string.dialog_message_incomplete_order_keychains_empty));
          }

          StringBuilder messageFormat = new StringBuilder(0);
          for (String message : messages) {
            messageFormat.append(message);
          }

          AlertDialog.Builder builder = new Builder(getActivity());
          builder.setTitle(R.string.dialog_title_incomplete_order);
          builder.setMessage(
              getString(R.string.dialog_message_incomplete_order)
                  + messageFormat);
          builder.setPositiveButton(R.string.dialog_positive_button_incomplete_order,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  try {
                    sendOrderByEmail(generateExcelFile());
                  } catch (IOException | InvalidFormatException | ParseException e) {
                    e.printStackTrace();
                  }
                }
              });
          builder.setNegativeButton(R.string.dialog_negative_button_incomplete_order,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  if (isStoreNameEmpty) {
                    mEditStoreName.requestFocus();
                  } else if (isDateEmpty) {
                    mEditOrderDate.requestFocus();
                  } else if (areCellsEmpty) {
                    Toast.makeText(getContext(),
                        R.string.toast_dialog_incomplete_order_keychains_empty,
                        Toast.LENGTH_SHORT).show();
                  }
                }
              });
          builder.show();
        } else {
          try {
            sendOrderByEmail(generateExcelFile());
          } catch (IOException | InvalidFormatException | ParseException e) {
            e.printStackTrace();
          }
        }
      }
    });

    return view;
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState
        .putString(getString(R.string.bundle_key_orderFragment_savedInstanceState_store_name), mEditStoreName.getText().toString());
    outState
        .putString(getString(R.string.bundle_key_orderFragment_savedInstanceState_order_date), mEditOrderDate.getText().toString());
    outState.putIntegerArrayList(getString(R.string.bundle_key_orderFragment_savedInstanceState_quantity_cell_values),
        (ArrayList<Integer>) getQuantityCellValues());

    super.onSaveInstanceState(outState);
  }

  private List<Integer> getQuantityCellValues() {
    List<Integer> values = new ArrayList<>();

    for (int i = 0; i < mAdapter.getItemCount(); i++) {
      OrderItem item = mAdapter.getItem(i);
      if (item != null) {
        values.add(item.getQuantity());
      }
    }

    return values;
  }

  private boolean areCellsEmpty() {
    for (int i = 0; i < mAdapter.getItemCount(); i++) {
      OrderItem item = mAdapter.getItem(i);
      if (item != null) {
        if (item.getQuantity() > 0) {
          return false;
        }
      }
    }

    return true;
  }

  private void resetOrder() {
    //  reset store name
    mEditStoreName.setText(null);

    //  reset date
    mEditOrderDate.setText(Util.getFormattedDateForLayout());

    //  reset cell values
    for (int i = 0; i < mAdapter.getItemCount(); i++) {
      OrderItem item = mAdapter.getItem(i);
      if (item != null) {
        item.setQuantity(0);
      }
    }
    mAdapter.notifyDataSetChanged();

    //  reset sent email value
    mSentEmail = false;
  }

  private void sendOrderByEmail(File file) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String storeName = mEditStoreName.getText().toString();
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory), "");

    Uri path = Uri.fromFile(file);
    Intent emailIntent = new Intent(Intent.ACTION_SEND);

    // set the type to 'email'
    emailIntent.setType("vnd.android.cursor.dir/email");

    //  set email address from preferences
    String sendtoEmail = getString(R.string.pref_default_value_sendto_email);
    String to[] = {sendtoEmail};
    emailIntent.putExtra(Intent.EXTRA_EMAIL, to);

    // the attachment
    emailIntent.putExtra(Intent.EXTRA_STREAM, path);

    // the mail subject
    String subject = String
        .format(getString(R.string.string_format_email_subject), repTerritory, storeName);
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

    //  the mail body
    String body = String
        .format(getString(R.string.intent_extra_text_body_send_order_by_email), storeName);
    emailIntent.putExtra(Intent.EXTRA_TEXT, body);

    //  send email!
//    startActivityForResult(
//        Intent.createChooser(emailIntent, getString(R.string.intent_title_send_order_by_email)),
//        R.string.intent_request_code_send_order_by_email);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case R.string.intent_request_code_send_order_by_email:
        Toast
            .makeText(getActivity(), R.string.toast_send_order_by_email_success, Toast.LENGTH_SHORT)
            .show();
        mSentEmail = true;
        break;
    }
  }

  private File generateExcelFile() throws IOException, InvalidFormatException, ParseException {
    Workbook workbook = WorkbookFactory.create(getActivity().getAssets().open(
        getString(R.string.excel_template_filename)));

    Sheet sheet = workbook.getSheetAt(0);

    //  Write Store Name and Number.
    String storeName = mEditStoreName.getText().toString();
    String[] storeNameCellLocations = getResources()
        .getStringArray(R.array.excel_cell_locations_store_number);
    for (String cellLocation : storeNameCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(storeName);
    }

    //  Write Rep Name.
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String repName = prefs.getString(getString(R.string.pref_key_rep_name), "");
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory), "");
    String[] repNameCellLocations = getResources()
        .getStringArray(R.array.excel_cell_locations_rep_name);
    for (String cellLocation : repNameCellLocations) {
      String repFormat = String
          .format(getString(R.string.string_format_repName_repTerritory), repName, repTerritory);
      getCellByAddress(sheet, cellLocation).setCellValue(repFormat);
    }

    //  Write Current Date.
    String orderDate = mEditOrderDate.getText().toString();
    String[] dateCellLocations = getResources()
        .getStringArray(R.array.excel_cell_locations_order_date);
    for (String cellLocation : dateCellLocations) {
      getCellByAddress(sheet, cellLocation).setCellValue(orderDate);
    }

    List<String> orderQuantities = new ArrayList<>(0);
    for (int i = 0; i < mAdapter.getItemCount(); i++) {
      OrderItem item = mAdapter.getItem(i);
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

        orderQuantities.add(String.valueOf(item.getQuantity()));
      }
    }

    SimpleDateFormat format = new SimpleDateFormat(getString(R.string.string_date_layout),
        Locale.getDefault());
    Date newDate = format.parse(orderDate);

    format = new SimpleDateFormat(getString(R.string.string_date_filename), Locale.getDefault());
    String filenameDate = format.format(newDate);

    File file = new File(getActivity().getExternalFilesDir(null),
        String.format(getString(R.string.string_format_filename), repTerritory.toLowerCase(),
            storeName.toLowerCase(), filenameDate));
    FileOutputStream out = new FileOutputStream(file);
    workbook.write(out);
    out.close();

    //  save to orders.json
    saveOrderToJson(storeName, orderQuantities, orderDate);

    return file;
  }

  private void saveOrderToJson(String storeName, List<String> orderQuantities, String orderDate)
      throws IOException {
    JSONOrderEntryList entryList = JSONUtil.getEntries(getActivity());
    JSONOrderEntry entry = new JSONOrderEntry(storeName, orderQuantities, orderDate);

    entryList.addEntry(entry);

    JSONUtil.setEntries(getActivity(), entryList);
  }

  @Override
  public boolean onBackPressed() {
    if (mSentEmail) {
      return false;
    }

    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_cancel_order);
    builder.setMessage(R.string.dialog_message_cancel_order);
    builder.setPositiveButton(R.string.dialog_positive_button_cancel_order,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            getFragmentManager().popBackStack();
            Toast.makeText(getActivity(), R.string.toast_dialog_cancel_order_success,
                Toast.LENGTH_SHORT).show();
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_cancel_order,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });
    builder.show();

    return true;
  }
}