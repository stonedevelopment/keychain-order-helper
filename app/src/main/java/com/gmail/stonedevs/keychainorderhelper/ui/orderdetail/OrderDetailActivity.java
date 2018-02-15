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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.PrepareIntentDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.PrepareIntentDialogFragment.OrderSentListener;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class OrderDetailActivity extends AppCompatActivity implements
    OrderDetailUserInteractionListener {

  private static final String TAG = OrderDetailActivity.class.getSimpleName();

  public static final int REQUEST_CODE = OrderListActivity.REQUEST_CODE + 1;
  public static final int SENT_RESULT_OK = RESULT_FIRST_USER;
  public static final int SENT_RESULT_CANCEL = RESULT_CANCELED;

  private OrderDetailViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_order_detail);

    setupActionBar();

    setupViewFragment();

    setupViewModel();

    subscribeToNavigationChanges();
  }

  private void setupActionBar() {
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
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, fragment.getId());
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToNavigationChanges() {
    //  This event fires when User clicks Resend Order button.
    mViewModel.getSendOrderCommand().observe(this, new Observer<Order>() {
      @Override
      public void onChanged(@Nullable Order order) {
        if (order != null) {
          onResendOrderButtonClick();
        }
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

  private void closeWithResult(int resultCode) {
    setResult(resultCode);
    finish();
  }

  @Override
  public void onResendOrderButtonClick() {
    PrepareIntentDialogFragment dialogFragment = PrepareIntentDialogFragment.createInstance();

    dialogFragment.setListener(new OrderSentListener() {
      @Override
      public void onOrderSent() {
        closeWithResult(SENT_RESULT_OK);
      }

      @Override
      public void onOrderNotSent() {
        mViewModel.getSnackBarMessage().setValue(R.string.snackbar_message_send_order_fail);
      }

      @Override
      public void onOrderNotSend_NoAppsForIntent() {
        mViewModel.getSnackBarMessage()
            .setValue(R.string.snackbar_message_send_order_fail_no_supported_apps);
      }
    });

    dialogFragment.setRepository(mViewModel.getRepository());

    dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
  }
}