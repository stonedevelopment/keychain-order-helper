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
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class OrderDetailActivity extends AppCompatActivity implements OrderDetailNavigator {

  private static final String TAG = OrderDetailActivity.class.getSimpleName();

  public static final int REQUEST_CODE = OrderListActivity.REQUEST_CODE + 1;

  private static final int REQUEST_CODE_ACTION_SEND = REQUEST_CODE + 1;

  public static final int RESULT_SENT_OK = RESULT_OK;
  public static final int RESULT_ERROR = RESULT_CANCELED + 1;

  private CollapsingToolbarLayout mCollapsingToolbar;

  private OrderDetailViewModel mViewModel;

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

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
      case R.id.action_send:
        mViewModel.getSendOrderCommand().call();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_ACTION_SEND) {
      mViewModel.getSnackBarMessenger().setValue(R.string.snackbar_message_send_order_success);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  private void setupActionBar() {
    mCollapsingToolbar = findViewById(R.id.collapsingToolbar);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }
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
    mViewModel.getUpdateUIStoreNameTextEvent().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
        mCollapsingToolbar.setTitle(s);
      }
    });

    mViewModel.getErrorLoadingDataEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Data was not received properly.
        finishWithResult(RESULT_ERROR);
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
              .setValue(R.string.snackbar_message_send_order_fail_no_supported_apps);
        }
      }
    });
  }

  private void subscribeToNavigationChanges() {
    //  This event fires when User clicks Resend Order button.
    mViewModel.getSendOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        showConfirmSendOrderDialog();
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