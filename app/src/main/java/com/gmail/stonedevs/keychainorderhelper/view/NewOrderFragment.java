package com.gmail.stonedevs.keychainorderhelper.view;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.ClickableKeychainAdapter;
import com.gmail.stonedevs.keychainorderhelper.databinding.FragmentNewOrderBinding;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil;
import com.gmail.stonedevs.keychainorderhelper.view.EditStoreNameDialogFragment.Listener;
import com.gmail.stonedevs.keychainorderhelper.viewmodel.OrderViewModel;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class NewOrderFragment extends BackHandledFragment {

  public static final String TAG = NewOrderFragment.class.getSimpleName();

  private FragmentNewOrderBinding mBinding;
  private OrderViewModel mViewModel;
  private ClickableKeychainAdapter mAdapter;

  private LiveData<Order> mOrder;

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
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    //  set title of action bar
    ((MainActivity) getActivity())
        .setActionBarTitle(getString(R.string.action_bar_title_newOrderFragment));

    // Inflate the layout for this fragment
    mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_new_order_old, container, false);

    mBinding.textStoreName.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Bundle args = new Bundle();
        args.putString(getString(R.string.dialog_key_edit_store_name),
            mViewModel.getStoreName().getValue());
        EditStoreNameDialogFragment dialogFragment = EditStoreNameDialogFragment
            .createInstance(args, new Listener() {
              @Override
              public void onSave(String storeName) {
                mViewModel.setStoreName(storeName);
              }
            });
        dialogFragment.show(getActivity().getSupportFragmentManager(), dialogFragment.getTag());
      }
    });

    mBinding.textOrderDate.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Date date = mViewModel.getOrderDate().getValue();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
            new OnDateSetListener() {
              @Override
              public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                mViewModel.setOrderDate(new Date(calendar.getTimeInMillis()));
              }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
      }
    });

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        LinearLayoutManager.VERTICAL);
    mBinding.listRecyclerView.addItemDecoration(dividerItemDecoration);

    mAdapter = new ClickableKeychainAdapter(getActivity());

    mAdapter.populateItems(getActivity(),
        mViewModel.getOrderQuantities().getValue());

    mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
      @Override
      public void onItemRangeChanged(int positionStart, int itemCount) {
        int quantity = mAdapter.getItem(positionStart).getQuantity();
        mViewModel.getOrderQuantities().getValue()
            .set(positionStart, quantity);
      }
    });

    mBinding.listRecyclerView.setAdapter(mAdapter);

    mBinding.resetOrderButton.setOnClickListener(new OnClickListener() {
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

    mBinding.sendOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        final boolean isStoreNameEmpty = mBinding.getBoundStoreName().isEmpty();
        final boolean areCellsEmpty = mAdapter.areItemQuantitiesEmpty();

        if (isStoreNameEmpty || areCellsEmpty) {
          List<String> messages = new ArrayList<>(0);

          if (isStoreNameEmpty) {
            messages.add(getString(R.string.dialog_message_incomplete_order_store_name_empty));
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

    return mBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    String orderId = "";
    if (savedInstanceState != null) {
      orderId = savedInstanceState
          .getString(getString(R.string.bundle_key_order_id), "");
    }

    mViewModel = ViewModelProviders.of(this).get(OrderViewModel.class);

    if (orderId.isEmpty()) {
      Repository repository = Repository.getInstance(getActivity().getApplication());

      String storeName =
          BuildConfig.DEBUG ? getString(R.string.editStoreName_debug_default_value) : "";
      Date orderDate = Calendar.getInstance().getTime();
      Order order = new Order(storeName, orderDate);

      repository.insertOrder(order);

    mViewModel.createObservableOrderEntity();
    }


    mViewModel.getObservableOrderEntity().observe(this, new Observer<Order>() {
      @Override
      public void onChanged(@Nullable Order order) {
        mBinding.setBoundStoreName(order.getStoreName());
        mBinding.setBoundOrderDate(order.getOrderDate());
      }
    });
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(getString(R.string.bundle_key_order_id), mOrder.getValue().getId());
    super.onSaveInstanceState(outState);
  }

  private void sendOrderByEmail()
      throws IOException, InvalidFormatException, ParseException {
    Workbook workbook = WorkbookFactory.create(getActivity().getAssets().open(
        getString(R.string.excel_template_filename)));

    String storeName = mViewModel.getStoreName().getValue();
    Date orderDate = mViewModel.getOrderDate().getValue();
    List<Integer> orderQuantities = mViewModel.getOrderQuantities().getValue();
    Integer orderTotal = mAdapter.getItemQuantityTotal();

    File file = ExcelUtil
        .generateExcelFile(getActivity(), workbook, storeName,
            orderDate, mAdapter.getItems());

    mViewModel.insert(new Order(storeName, orderDate, orderQuantities, orderTotal));

    ((MainActivity) getActivity()).sendOrderByEmail(file, storeName);
  }

  private void resetOrder() {
    List<Integer> orderQuantities = mAdapter.resetItemQuantities();
    mAdapter.notifyDataSetChanged();

    mViewModel.setStoreName("");
    mViewModel.setOrderDate(Calendar.getInstance().getTime());
    mViewModel.setOrderQuantities(orderQuantities);
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