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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.SettingsActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

/**
 * Activity for viewing a list of previously created orders.
 *
 * @see OrderListNavigator
 */
public class OrderListActivity extends AppCompatActivity implements OrderListNavigator {

  private static final String TAG = OrderListActivity.class.getSimpleName();

  private OrderListViewModel mViewModel;

  public static final int REQUEST_CODE = MainActivity.REQUEST_CODE + 1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_order_list);

    setupActionBar();

    setupFabMenu();

    setupViewFragment();

    setupViewModel();

    subscribeToViewModelCommands();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    mViewModel.handleActivityResult(requestCode, resultCode);

    mViewModel.start();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void setupActionBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  private void setupViewFragment() {
    OrderListFragment fragment = obtainViewFragment();

    ActivityUtils
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, R.id.fragment_container);
  }

  private void setupFabMenu() {
    FloatingActionButton fabCreateOrder = findViewById(R.id.fab_create_order);
    fabCreateOrder.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startNewOrderActivity();
      }
    });
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToViewModelCommands() {
    mViewModel.getOrderDetailCommand().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String orderId) {
        startOrderDetailActivity(orderId);
      }
    });

    mViewModel.getNewOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        startNewOrderActivity();
      }
    });
  }

  private OrderListFragment obtainViewFragment() {
    OrderListFragment fragment = (OrderListFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = OrderListFragment.createInstance();
    }

    return fragment;
  }

  static OrderListViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(OrderListViewModel.class);
  }

  @Override
  public void startNewOrderActivity() {
    Intent intent = new Intent(this, NewOrderActivity.class);
    startActivityForResult(intent, NewOrderActivity.REQUEST_CODE);
  }

  @Override
  public void startOrderDetailActivity(String orderId) {
    Intent intent = new Intent(this, OrderDetailActivity.class);
    intent.putExtra(getString(R.string.bundle_key_order_id), orderId);
    startActivityForResult(intent, OrderDetailActivity.REQUEST_CODE);
  }
}