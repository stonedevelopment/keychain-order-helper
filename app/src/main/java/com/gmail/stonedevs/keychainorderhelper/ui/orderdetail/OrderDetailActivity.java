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

package com.gmail.stonedevs.keychainorderhelper.ui.orderdetail;

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
import com.gmail.stonedevs.keychainorderhelper.ui.SettingsActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;
import com.gmail.stonedevs.keychainorderhelper.util.StringUtils;

public class OrderDetailActivity extends AppCompatActivity implements OrderDetailNavigator {

  private static final String TAG = OrderDetailActivity.class.getSimpleName();

  public static final int REQUEST_CODE = NewOrderActivity.REQUEST_CODE + 1;

  public static final int REQUEST_CODE_ACTION_SEND = NewOrderActivity.REQUEST_CODE_ACTION_SEND;

  //  RESULT_OK
  public static final int RESULT_SAVE_OK = NewOrderActivity.RESULT_SAVE_OK;
  public static final int RESULT_SENT_OK = RESULT_SAVE_OK - 1;

  //  RESULT_CANCELED
  public static final int RESULT_SAVE_CANCEL = NewOrderActivity.RESULT_CANCELED;
  public static final int RESULT_SENT_CANCEL = RESULT_SAVE_CANCEL + 1;
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

    subscribeToNavigationChanges();
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
        mViewModel.getEditOrderCommand().call();
        return true;
      case R.id.action_send_save:
        mViewModel.getSendOrderCommand().call();
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
    mViewModel.handleActivityResult(requestCode, resultCode);

    switch (requestCode) {
      case NewOrderActivity.REQUEST_CODE:
        switch (resultCode) {
          case NewOrderActivity.RESULT_SAVE_CANCEL:
            //  do nothing, user didn't save changes
            //  allow case to bleed into RESULT_SAVE_OK just in case persisted data was changed.
          case NewOrderActivity.RESULT_SAVE_OK:
            String orderId = data.getStringExtra(getString(R.string.bundle_key_order_id));
            mViewModel.refresh(orderId);
            break;
        }
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

        long orderDate = order.getOrderDate().getTime();
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

    mViewModel.getIntentReadyEvent().observe(this, new Observer<Intent>() {
      @Override
      public void onChanged(@Nullable Intent intent) {
        Intent chooser = Intent
            .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

        if (intent.resolveActivity(getPackageManager()) != null) {
          startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
        } else {
          //  there are no apps on phone to handle this intent, cancel order
          mViewModel.getSnackBarMessenger()
              .setValue(R.string.snackbar_message_send_order_error_no_supported_apps);
        }
      }
    });
  }

  private void subscribeToNavigationChanges() {
    //  This event fires when User clicks Resend Order button.
    mViewModel.getSendOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        // TODO: 3/4/2018 check for readiness, just like new order
        //  this is an issue that could result from persistent order save without full data
        showConfirmSendOrderDialog();
      }
    });

    mViewModel.getEditOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        startEditOrderActivity();
      }
    });
  }

  private OrderDetailFragment obtainViewFragment() {
    OrderDetailFragment fragment = (OrderDetailFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = OrderDetailFragment.createInstance();
      fragment.setArguments(obtainArguments());
    }

    return fragment;
  }

  private Bundle obtainArguments() {
    //  Get the requested order id.
    String orderId = getIntent().getStringExtra(getString(R.string.bundle_key_order_id));

    Bundle args = new Bundle();
    args.putString(getString(R.string.bundle_key_order_id), orderId);

    OrderDetailFragment fragment = new OrderDetailFragment();
    fragment.setArguments(args);

    return args;
  }

  public static OrderDetailViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(OrderDetailViewModel.class);
  }

  void finishWithResult(int resultCode) {
    setResult(resultCode);
    finish();
  }

  @Override
  public void startEditOrderActivity() {
    Intent intent = new Intent(this, NewOrderActivity.class);
    intent.putExtra(getString(R.string.bundle_key_order_id), mViewModel.getOrderId());
    startActivityForResult(intent, NewOrderActivity.REQUEST_CODE);
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
            mViewModel.prepareToResendOrder(OrderDetailActivity.this);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_resend_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to continue their order.
          }
        });
    builder.show();
  }
}