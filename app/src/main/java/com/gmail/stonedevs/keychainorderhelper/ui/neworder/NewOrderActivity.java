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

package com.gmail.stonedevs.keychainorderhelper.ui.neworder;

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
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailFragment;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class NewOrderActivity extends AppCompatActivity {

  private static final String TAG = NewOrderActivity.class.getSimpleName();

  public static final int REQUEST_CODE = MainActivity.REQUEST_CODE + 1;
  public static final int SENT_RESULT_OK = 1;

  private NewOrderViewModel mViewModel;

  @Override
  public boolean onSupportNavigateUp() {
    //  todo: check for if order was saved or not.
    onBackPressed();
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_order);

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
    NewOrderFragment fragment = obtainViewFragment();

    ActivityUtils
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, fragment.getId());
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToNavigationChanges() {

    mViewModel.getResetOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Reset order.
        mViewModel.resetOrder();
      }
    });

    mViewModel.getSendOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Send order.
        // TODO: 2/14/2018 This should check for validity, trigger failed event if invalid.
      }
    });
  }

  private NewOrderFragment obtainViewFragment() {
    NewOrderFragment fragment = (NewOrderFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = NewOrderFragment.createInstance();
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

  public static NewOrderViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(NewOrderViewModel.class);
  }
}