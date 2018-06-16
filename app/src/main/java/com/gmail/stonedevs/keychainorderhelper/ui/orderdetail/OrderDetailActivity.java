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

package com.gmail.stonedevs.keychainorderhelper.ui.orderdetail;

import static com.gmail.stonedevs.keychainorderhelper.util.BundleUtils.BUNDLE_KEY_ORDER_ID;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder.OrderType;
import com.gmail.stonedevs.keychainorderhelper.ui.SettingsActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;
import com.gmail.stonedevs.keychainorderhelper.util.StringUtils;
import java.util.Date;

/**
 * Activity for viewing an order's details, called by {@link OrderListActivity#startNewOrderActivity()}.
 *
 * Should contain all of the constant variables needed for sending results back to its calling
 * activity, store data for toolbar layout components and display them, and create the {@link
 * OrderDetailViewModel} instance used by its {@link OrderDetailFragment}.
 *
 * @see OrderDetailCommander
 * @see OrderDetailNavigator
 */
public class OrderDetailActivity extends AppCompatActivity implements OrderDetailCommander,
    OrderDetailNavigator {

  private static final String TAG = OrderDetailActivity.class.getSimpleName();

  public static final int REQUEST_CODE = NewOrderActivity.REQUEST_CODE + 1;

  public static final int REQUEST_CODE_ACTION_SEND = NewOrderActivity.REQUEST_CODE_ACTION_SEND;

  //  RESULT_OK
  public static final int RESULT_SENT_ORDER_OK = RESULT_OK;
  public static final int RESULT_SENT_ACKNOWLEDGEMENT_OK = RESULT_SENT_ORDER_OK - 1;
  public static final int RESULT_DELETE_ORDER_OK = RESULT_SENT_ACKNOWLEDGEMENT_OK - 1;

  //  RESULT_CANCELED
  public static final int RESULT_SENT_CANCEL = RESULT_CANCELED + 1;
  public static final int RESULT_SENT_ERROR_NO_APPS = RESULT_SENT_CANCEL + 1;
  public static final int RESULT_DATA_LOAD_ERROR = RESULT_SENT_ERROR_NO_APPS + 1;

  private TextView mStoreNameTextView;
  private TextView mOrderQuantityTextView;
  private TextView mOrderDateTextView;
  private TextView mOrderTerritoryTextView;

  private OrderDetailViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_order_detail);

    setupActionBar();

    setupViewFragment();

    setupViewModel();

    subscribeToViewModelEvents();

    subscribeToViewModelCommands();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_order_detail, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_edit:
        startEditOrderActivity();
        return true;
      case R.id.action_send_order:
        mViewModel.initializeSendOrderPhase();
        showConfirmSendOrderDialog();
        return true;
      case R.id.action_send_acknowledgement:
        mViewModel.initializeSendAcknowledgementPhase();
        showConfirmSendAcknowledgementDialog();
        return true;
      case R.id.action_delete:
        mViewModel.initializeDeleteOrderPhase();
        showConfirmDeleteOrderDialog();
        return true;
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
      case REQUEST_CODE_ACTION_SEND:
        if (mViewModel.isSendingOrder()) {
          finishWithResult(RESULT_SENT_ORDER_OK);
        } else if (mViewModel.isSendingAcknowledgement()) {
          finishWithResult(RESULT_SENT_ACKNOWLEDGEMENT_OK);
        } else {
          throw new RuntimeException("Invalid view model state.");
        }
        break;
      case NewOrderActivity.REQUEST_CODE:
        finishWithResult(resultCode);
        break;
      default:
        super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  private void setupActionBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayHomeAsUpEnabled(true);
    }

    mStoreNameTextView = findViewById(R.id.storeNameTextView);
    mOrderQuantityTextView = findViewById(R.id.orderQuantityTextView);
    mOrderDateTextView = findViewById(R.id.orderDateTextView);
    mOrderTerritoryTextView = findViewById(R.id.orderTerritoryTextView);
  }

  private void setupViewFragment() {
    OrderDetailFragment fragment = obtainViewFragment();

    ActivityUtils
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, R.id.fragment_container);
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToViewModelEvents() {
    mViewModel.getUpdateUIEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        mStoreNameTextView.setText(order.getStoreName());

        Date orderDate = order.getOrderDate();
        mOrderDateTextView
            .setText(StringUtils.formatSentOrderDate(getApplicationContext(), orderDate));

        int orderQuantity = order.getOrderQuantity();
        mOrderQuantityTextView
            .setText(StringUtils.formatOrderQuantity(getApplicationContext(), orderQuantity));

        String orderTerritory = order.getOrderTerritory();
        mOrderTerritoryTextView.setText(orderTerritory);
      }
    });

    mViewModel.getErrorLoadingDataEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Data was not received properly.
        finishWithResult(RESULT_DATA_LOAD_ERROR);
      }
    });

    mViewModel.getDataDeletedEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Data was not received properly.
        finishWithResult(RESULT_DELETE_ORDER_OK);
      }
    });

    mViewModel.getErrorDeletingDataEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Data was not deleted properly.
        mViewModel.getSnackBarMessenger().setValue(R.string.snackbar_message_data_deleted_fail);
      }
    });

    mViewModel.getIntentReadyEvent().observe(this, new Observer<Intent>() {
      @Override
      public void onChanged(@Nullable Intent intent) {
        if (mViewModel.isSendingOrder()) {
          sendOrderByEmail(intent);
        } else if (mViewModel.isSendingAcknowledgement()) {
          sendOrderAcknowledgementByEmail(intent);
        } else {
          throw new RuntimeException("Invalid view model state.");
        }
      }
    });
  }

  private void subscribeToViewModelCommands() {
  }

  private OrderDetailFragment obtainViewFragment() {
    OrderDetailFragment fragment = (OrderDetailFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      //  Get the requested order id.
      String orderId = getIntent().getStringExtra(BUNDLE_KEY_ORDER_ID);

      fragment = OrderDetailFragment.createInstance(orderId);
    }

    return fragment;
  }

  public static OrderDetailViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(OrderDetailViewModel.class);
  }

  void sendOrderByEmail(Intent intent) {
    startSendActionIntent(intent, R.string.intent_title_send_order_by_email);
  }

  void sendOrderAcknowledgementByEmail(Intent intent) {
    startSendActionIntent(intent, R.string.intent_title_send_order_acknowledgement_by_email);
  }

  void finishWithResult(int resultCode) {
    setResult(resultCode);
    finish();
  }

  @Override
  public void startEditOrderActivity() {
    Intent intent = new Intent(this, NewOrderActivity.class);
    intent.putExtra(BUNDLE_KEY_ORDER_ID, mViewModel.getOrderId());
    startActivityForResult(intent, NewOrderActivity.REQUEST_CODE);
  }

  @Override
  public void startSendActionIntent(Intent intent, int intentTitle) {
    Intent chooser = Intent.createChooser(intent, getString(intentTitle));

    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
    } else {
      //  there are no apps on phone to handle this intent, cancel order
      finishWithResult(RESULT_SENT_ERROR_NO_APPS);
    }
  }

  @Override
  public void showConfirmSendOrderDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_resend_order);
    builder.setMessage(R.string.dialog_message_resend_order);
    builder.setPositiveButton(R.string.dialog_positive_button_resend_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.beginSendOrderPhase(OrderDetailActivity.this);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_resend_order, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mViewModel.endSendOrderPhase();
      }
    });
    builder.setCancelable(false);
    builder.show();
  }

  @Override
  public void showConfirmSendAcknowledgementDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_send_order_acknowledgement);
    builder.setMessage(getString(R.string.dialog_message_send_order_acknowledgement));
    builder.setPositiveButton(R.string.dialog_positive_button_send_order_acknowledgement,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.beginSendAcknowledgementPhase(OrderType.ACKNOWLEDGEMENT_WITH_ORDER);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_send_order_acknowledgement,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.endSendAcknowledgementPhase();
          }
        });
    builder.setCancelable(false);
    builder.show();
  }

  @Override
  public void showConfirmDeleteOrderDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_delete_order);
    builder.setMessage(R.string.dialog_message_delete_order);
    builder.setPositiveButton(R.string.dialog_positive_button_delete_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.beginDeleteOrderPhase();
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_delete_order, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mViewModel.endDeleteOrderPhase();
      }
    });
    builder.setCancelable(false);
    builder.show();
  }
}