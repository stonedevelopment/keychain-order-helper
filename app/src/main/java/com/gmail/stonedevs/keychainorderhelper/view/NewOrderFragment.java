package com.gmail.stonedevs.keychainorderhelper.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.ClickableKeychainAdapter;
import com.gmail.stonedevs.keychainorderhelper.model.Keychain;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil;
import com.gmail.stonedevs.keychainorderhelper.util.Util;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;

public class NewOrderFragment extends BackHandledFragment {

  public static final String TAG = NewOrderFragment.class.getSimpleName();

  private String mStoreName;
  private String mOrderDate;

  private EditText mEditStoreName;
  private EditText mEditOrderDate;

  private RecyclerView mRecyclerView;
  private ClickableKeychainAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;

  public NewOrderFragment() {
    // Required empty public constructor
  }

  public static NewOrderFragment newInstance() {
    NewOrderFragment fragment = new NewOrderFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //  set variables here
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    //  set title of action bar
    ((MainActivity) getActivity())
        .setActionBarTitle(getString(R.string.action_bar_title_newOrderFragment));

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_new_order, container, false);

    String defaultStoreName =
        BuildConfig.DEBUG ? getString(R.string.storeNameText_debug_default_value) : "";
    String defaultOrderDate = Util.getFormattedDateForLayout();

    mStoreName = defaultStoreName;
    mOrderDate = defaultOrderDate;
    ArrayList<Integer> orderQuantities = new ArrayList<>(0);

    if (savedInstanceState != null) {
      mStoreName = savedInstanceState
          .getString(getString(R.string.bundle_key_NewOrderFragment_store_name),
              defaultStoreName);
      mOrderDate = savedInstanceState
          .getString(getString(R.string.bundle_key_NewOrderFragment_order_date),
              defaultOrderDate);
      orderQuantities = savedInstanceState
          .getIntegerArrayList(
              getString(R.string.bundle_key_NewOrderFragment_quantity_cell_values));
    }

    OnFocusChangeListener onEditTextFocusChangeListener = new OnFocusChangeListener() {
      @Override
      public void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) {
          InputMethodManager mImMan = (InputMethodManager) getContext()
              .getSystemService(Context.INPUT_METHOD_SERVICE);
          if (mImMan != null) {
            mImMan.hideSoftInputFromWindow(mEditStoreName.getWindowToken(), 0);
          }
        }
      }
    };

    mEditStoreName = view.findViewById(R.id.editStoreName);
    mEditStoreName.setText(mStoreName);
    mEditStoreName.setOnFocusChangeListener(onEditTextFocusChangeListener);
    mEditStoreName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        mStoreName = s.toString();
      }
    });

    mEditOrderDate = view.findViewById(R.id.editOrderDate);
    mEditOrderDate.setText(mOrderDate);
    mEditOrderDate.setOnFocusChangeListener(onEditTextFocusChangeListener);
    mEditOrderDate.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        mOrderDate = s.toString();
      }
    });

    mRecyclerView = view.findViewById(R.id.listRecyclerView);

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        mLayoutManager.getOrientation());
    mRecyclerView.addItemDecoration(dividerItemDecoration);

    //  Populate names by string-array
    List<Keychain> items = new ArrayList<>(0);
    String[] names = getResources().getStringArray(R.array.excel_cell_values_names);
    String[] addresses = getResources().getStringArray(R.array.excel_cell_locations_quantities);

    //  if quantity cell values persisted through saveInstanceState, fill quantities
    //  else, fill with default 0
    if (orderQuantities != null && orderQuantities.size() == names.length) {
      for (int i = 0; i < names.length; i++) {
        int quantity = orderQuantities.get(i);
        items.add(new Keychain(names[i], quantity, new CellAddress(addresses[i])));
      }
    } else {
      for (int i = 0; i < names.length; i++) {
        items.add(new Keychain(names[i], 0, new CellAddress(addresses[i])));
      }
    }

    mAdapter = new ClickableKeychainAdapter(getActivity());
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
        final boolean areCellsEmpty = mAdapter.areItemQuantitiesEmpty();

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
          builder.setNegativeButton(R.string.dialog_negative_button_incomplete_order,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  //  do nothing, allow user to fix issue, no need to hold their hand.
                }
              });
          builder.show();
        } else {
          AlertDialog.Builder builder = new Builder(getActivity());
          builder.setTitle(R.string.dialog_title_send_order);
          builder.setMessage(getString(R.string.dialog_message_send_order));
          builder.setPositiveButton(R.string.dialog_positive_button_send_order,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  try {
                    sendOrderByEmail();
                  } catch (IOException | InvalidFormatException | ParseException e) {
                    e.printStackTrace();
                  }
                }
              });
          builder.setNegativeButton(R.string.dialog_negative_button_send_order,
              new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                  //  do nothing, allow user to double check order.
                }
              });
          builder.show();
        }
      }
    });

    mRecyclerView.requestFocus();

    return view;
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState
        .putString(getString(R.string.bundle_key_NewOrderFragment_store_name),
            mEditStoreName.getText().toString());
    outState
        .putString(getString(R.string.bundle_key_NewOrderFragment_order_date),
            mEditOrderDate.getText().toString());
    outState.putIntegerArrayList(
        getString(R.string.bundle_key_NewOrderFragment_quantity_cell_values),
        mAdapter.getItemQuantities());

    super.onSaveInstanceState(outState);
  }

  private void sendOrderByEmail()
      throws IOException, InvalidFormatException, ParseException {
    Workbook workbook = WorkbookFactory.create(getActivity().getAssets().open(
        getString(R.string.excel_template_filename)));

    File file = ExcelUtil
        .generateExcelFile(getActivity(), workbook, mStoreName, mOrderDate, mAdapter.getItems());

    ((MainActivity) getActivity()).sendOrderByEmail(file, mStoreName);
  }

  private void resetOrder() {
    //  reset store name
    mStoreName = "";
    mEditStoreName.setText(mStoreName);

    //  reset date
    mOrderDate = Util.getFormattedDateForLayout();
    mEditOrderDate.setText(mOrderDate);

    //  reset cell values
    for (int i = 0; i < mAdapter.getItemCount(); i++) {
      Keychain item = mAdapter.getItem(i);
      if (item != null) {
        item.setQuantity(0);
      }
    }
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public boolean onBackPressed() {
    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_cancel_order);
    builder.setMessage(R.string.dialog_message_cancel_order);
    builder.setPositiveButton(R.string.dialog_positive_button_cancel_order,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            closeFragment();
            Toast.makeText(getActivity(), R.string.toast_dialog_cancel_order_success,
                Toast.LENGTH_SHORT).show();
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_cancel_order,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to continue their order.
          }
        });
    builder.show();

    return true;
  }
}