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

package com.gmail.stonedevs.keychainorderhelper.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.RequiredFieldsDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.RequiredFieldsDialogFragment.OnRequiredFieldsCheckListener;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class MainActivity extends AppCompatActivity implements MainActivityNavigation {

  private static final String TAG = MainActivity.class.getSimpleName();

  public static final int REQUEST_CODE = 1;

  private MainActivityViewModel mViewModel;

  // TODO: 2/14/2018 Left off: Cleaned up MainActivity and NewOrderActivity

  // TODO: 2/14/2018 Test long store names and their ellipsize functionality.

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupActionBar();

    setupViewFragment();

    setupViewModel();

    subscribeToNavigationChanges();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mViewModel.handleActivityResult(requestCode, resultCode);
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
    MainActivityFragment fragment = obtainViewFragment();

    ActivityUtils
        .addFragmentInActivity(getSupportFragmentManager(), fragment, fragment.getId());
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToNavigationChanges() {
    mViewModel.getCheckReadyEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {

        //  If rep fields are empty, open dialog for easy editing, otherwise, send user to new order
        //  activity, they followed instructions.
        if (mViewModel.isReady()) {
          //  Trigger view model's new order command to start the new order activity.
          mViewModel.getNewOrderCommand().call();
        } else {
          //  Trigger view model's command to tell its observer to open the required fields dialog.
          mViewModel.getOpenDialogCommand().call();
        }
      }
    });

    mViewModel.getNewOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        startNewOrderActivity();
      }
    });

    mViewModel.getOrderListCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        startOrderListActivity();
      }
    });
  }

  private MainActivityFragment obtainViewFragment() {
    MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = MainActivityFragment.createInstance();
      fragment.setArguments(obtainArguments());
    }

    return fragment;
  }

  private Bundle obtainArguments() {
    return new Bundle();
  }

  public static MainActivityViewModel obtainViewModel(FragmentActivity activity) {
    //  Use a Factory to inject dependencies into the ViewModel.
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(MainActivityViewModel.class);
  }

  @Override
  public void startRequiredFieldsDialogFragment() {
    Bundle args = new Bundle();

    //  Fill argument bundle with either saved, if debugging fill with default values :)
    args.putString(getString(R.string.pref_key_rep_name),
        BuildConfig.DEBUG ? getString(R.string.pref_debug_default_value_rep_name)
            : mViewModel.getRepName());
    args.putString(getString(R.string.pref_key_rep_territory),
        BuildConfig.DEBUG ? getString(R.string.pref_debug_default_value_rep_territory)
            : mViewModel.getRepTerritory());

    //  Create instance of dialog fragment use to help User fill in the blanks.
    RequiredFieldsDialogFragment dialogFragment = RequiredFieldsDialogFragment
        .createInstance(args);

    //  Set up a listener that will determine whether we allow User into New Order Activity.
    dialogFragment.setListener(new OnRequiredFieldsCheckListener() {
      @Override
      public void onSuccess(int resourceId) {
        //  User successfully entered all values completed. Let them know.
        mViewModel.getSnackBarMessenger().setValue(resourceId);

        //  Trigger view model's new order command to start the new order activity.
        mViewModel.getNewOrderCommand().call();
      }

      @Override
      public void onFail(int resourceId) {
        //  User did not fill out all fields required completely, let them know.
        mViewModel.getSnackBarMessenger().setValue(resourceId);
      }
    });

    //  Initializations complete, show that dialog!
    dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
  }

  @Override
  public void startNewOrderActivity() {
    Intent intent = new Intent(this, NewOrderActivity.class);
    startActivityForResult(intent, NewOrderActivity.REQUEST_CODE);
  }

  @Override
  public void startOrderListActivity() {
    Intent intent = new Intent(this, OrderListActivity.class);
    startActivityForResult(intent, OrderListActivity.REQUEST_CODE);
  }
}
