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
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

/**
 * Displays the New Order screen.
 */
public class NewOrderActivity extends AppCompatActivity implements NewOrderNavigator {

  public static final int REQUEST_CODE = 1;

  public static final int RESULT_OK = 1;

  @Override
  public boolean onSupportNavigateUp() {
    //  todo: check for if order was saved or not.
    onBackPressed();
    return true;
  }

  @Override
  public void onOrderSaved() {
    //  order was saved successfully, now to send it via email
    setResult(RESULT_OK);
    finish();
  }

  @Override
  public void onOrderSent() {

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_order);

    //  Set up the toolbar
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }

    NewOrderFragment fragment = obtainViewFragment();

    ActivityUtils
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, R.id.fragment_container);

    subscribeToNavigationChanges();
  }

  private void subscribeToNavigationChanges() {
    NewOrderViewModel viewModel = obtainViewModel(this);

    //  This activity observes the navigation events in the ViewModel
    viewModel.getOrderUpdatedEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        NewOrderActivity.this.onOrderSaved();
      }
    });
  }

  public static NewOrderViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(NewOrderViewModel.class);
  }

  private NewOrderFragment obtainViewFragment() {
    NewOrderFragment fragment = (NewOrderFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = NewOrderFragment.createInstance();

      Bundle args = new Bundle();
      args.putString(getString(R.string.bundle_key_order_id),
          getIntent().getStringExtra(getString(R.string.bundle_key_order_id)));

      fragment.setArguments(args);
    }

    return fragment;
  }
}