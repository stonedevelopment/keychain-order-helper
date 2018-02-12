package com.gmail.stonedevs.keychainorderhelper.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.KeychainAdapter;
import com.gmail.stonedevs.keychainorderhelper.databinding.FragmentViewOrderBinding;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil;
import com.gmail.stonedevs.keychainorderhelper.viewmodel.OrderViewModel;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class ViewOrderFragment extends BackHandledFragment {

  private static final String TAG = ViewOrderFragment.class.getSimpleName();
  private static final String KEY_ORDER_ID = "order_id";

  private FragmentViewOrderBinding mBinding;

  private KeychainAdapter mAdapter;

  public ViewOrderFragment() {
    // Required empty public constructor
  }

  public static ViewOrderFragment newInstance(String orderId) {
    ViewOrderFragment fragment = new ViewOrderFragment();

    Bundle args = new Bundle();
    args.putString(KEY_ORDER_ID, orderId);
    fragment.setArguments(args);

    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //  set variable values here
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    //  set title of action bar
    ((MainActivity) getActivity())
        .setActionBarTitle(getString(R.string.action_bar_title_viewOrderFragment));

    // Inflate the layout for this fragment
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_view_order, container, false);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        LinearLayoutManager.VERTICAL);
    mBinding.listRecyclerView.addItemDecoration(dividerItemDecoration);

    mAdapter = new KeychainAdapter();
    mBinding.listRecyclerView.setAdapter(mAdapter);

    mBinding.resendButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.dialog_title_resend_order);
        builder.setMessage(getString(R.string.dialog_message_resend_order));
        builder.setPositiveButton(R.string.dialog_positive_button_resend_order,
            new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {
                try {
                  Workbook workbook = WorkbookFactory.create(getActivity().getAssets().open(
                      getString(R.string.excel_template_filename)));

                  Order mOrder = mBinding.getBoundOrderEntity();

                  File file = ExcelUtil
                      .generateExcelFile(getActivity(), workbook, mOrder.getStoreName(),
                          mOrder.getOrderDate(), mAdapter.getItems());

                  ((MainActivity) getActivity())
                      .sendOrderByEmail(file, mOrder.getStoreName());
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

    return mBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    final OrderViewModel viewModel = ViewModelProviders.of(getActivity())
        .get(OrderViewModel.class);

    viewModel.getObservableOrderEntity().observe(this, new Observer<Order>() {
      @Override
      public void onChanged(@Nullable Order order) {
        viewModel.setBoundOrderEntity(order);
      }
    });

    final String orderId = getArguments().getString(KEY_ORDER_ID);
    viewModel.createObservableOrderEntity(orderId);
  }

  @Override
  public boolean onBackPressed() {
    return false;
  }
}