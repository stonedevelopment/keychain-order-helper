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
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.KeychainAdapter;
import com.gmail.stonedevs.keychainorderhelper.model.Keychain;
import com.gmail.stonedevs.keychainorderhelper.model.Order;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellAddress;

public class ViewOrderFragment extends BackHandledFragment {

  public static final String TAG = ViewOrderFragment.class.getSimpleName();

  private Order mOrder;

  private RecyclerView mRecyclerView;
  private KeychainAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;

  public ViewOrderFragment() {
    // Required empty public constructor
  }

  public static ViewOrderFragment newInstance(Context c, Order order) {
    String storeName = order.getStoreName();
    String orderDate = order.getOrderDate();
    ArrayList<Integer> orderQuantities = order.getOrderQuantities();
    Integer orderTotal = order.getOrderTotal();

    ViewOrderFragment fragment = new ViewOrderFragment();

    Bundle args = new Bundle();
    args.putString(c.getString(R.string.bundle_key_ViewOrderFragment_store_name),
        storeName);
    args.putString(c.getString(R.string.bundle_key_ViewOrderFragment_order_date),
        orderDate);
    args.putIntegerArrayList(c.getString(R.string.bundle_key_ViewOrderFragment_order_quantities),
        orderQuantities);
    args.putInt(c.getString(R.string.bundle_key_ViewOrderFragment_order_total),
        orderTotal);

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
        .setActionBarTitle(getString(R.string.action_bar_title_viewOrderFragment));

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_view_order, container, false);

    final String storeName, orderDate;
    ArrayList<Integer> orderQuantities;
    Integer orderTotal;

    if (savedInstanceState != null) {
      storeName = savedInstanceState
          .getString(getString(R.string.bundle_key_ViewOrderFragment_store_name), "");
      orderDate = savedInstanceState
          .getString(getString(R.string.bundle_key_ViewOrderFragment_order_date), "");
      orderQuantities = savedInstanceState
          .getIntegerArrayList(
              getString(
                  R.string.bundle_key_ViewOrderFragment_order_quantities));
      orderTotal = savedInstanceState
          .getInt(getString(R.string.bundle_key_ViewOrderFragment_order_total));
    } else {
      Bundle args = getArguments();
      storeName = args
          .getString(getString(R.string.bundle_key_ViewOrderFragment_store_name), "");
      orderDate = args
          .getString(getString(R.string.bundle_key_ViewOrderFragment_order_date), "");
      orderQuantities = args
          .getIntegerArrayList(
              getString(R.string.bundle_key_ViewOrderFragment_order_quantities));
      orderTotal = args
          .getInt(getString(R.string.bundle_key_ViewOrderFragment_order_total));
    }

    mOrder = new Order(storeName, orderDate, orderQuantities, orderTotal);

    TextView textStoreName = view.findViewById(R.id.textStoreName);
    textStoreName.setText(mOrder.getStoreName());

    TextView textOrderDate = view.findViewById(R.id.textOrderDate);
    textOrderDate.setText(mOrder.getOrderDate());

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
    orderQuantities = mOrder.getOrderQuantities();
    if (orderQuantities.size() == names.length) {
      for (int i = 0; i < names.length; i++) {
        int quantity = mOrder.getOrderQuantities().get(i);
        items.add(new Keychain(names[i], quantity, new CellAddress(addresses[i])));
      }
    }

    mAdapter = new KeychainAdapter();
    mAdapter.bindItems(items);

    mRecyclerView.setAdapter(mAdapter);

    //  finally, fill order total textView with formatted string
    TextView textOrderTotal = view.findViewById(R.id.textOrderTotal);
    textOrderTotal.setText(mOrder.getOrderTotalText(getActivity()));

    Button resendButton = view.findViewById(R.id.resendButton);
    resendButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.dialog_title_send_order);
        builder.setMessage(getString(R.string.dialog_message_send_order));
        builder.setPositiveButton(R.string.dialog_positive_button_send_order,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                try {
                  Workbook workbook = WorkbookFactory.create(getActivity().getAssets().open(
                      getString(R.string.excel_template_filename)));

                  File file = ExcelUtil
                      .generateExcelFile(getActivity(), workbook, mOrder.getStoreName(),
                          mOrder.getOrderDate(), mAdapter.getItems());

                  ((MainActivity) getActivity()).sendOrderByEmail(file, mOrder.getStoreName());
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
    });
    return view;
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState
        .putString(getString(R.string.bundle_key_ViewOrderFragment_store_name),
            mOrder.getStoreName());
    outState
        .putString(getString(R.string.bundle_key_NewOrderFragment_order_date),
            mOrder.getOrderDate());
    outState.putIntegerArrayList(
        getString(R.string.bundle_key_ViewOrderFragment_order_quantities),
        mAdapter.getItemQuantities());
    outState.putInt(getString(R.string.bundle_key_ViewOrderFragment_order_total),
        mOrder.getOrderTotal());

    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean onBackPressed() {
    return false;
  }
}