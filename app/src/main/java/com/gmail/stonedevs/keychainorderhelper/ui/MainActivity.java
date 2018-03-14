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

package com.gmail.stonedevs.keychainorderhelper.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.InitialSettingsDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.InitialSettingsDialogFragment.OnSaveListener;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;

/**
 * Initialization activity used to perform update requirements. Calls {@link OrderListActivity} upon
 * completion.
 */
public class MainActivity extends AppCompatActivity implements MainActivityNavigation,
    OnSaveListener {

  private static final String TAG = MainActivity.class.getSimpleName();

  public static final int REQUEST_CODE = 1;

  private MainActivityViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupViewModel();

    subscribeToViewModelCommands();

    startViewModel();
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToViewModelCommands() {
    mViewModel.getOpenInitialSettingsDialogCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        startInitialSettingsDialogFragment();
      }
    });

    mViewModel.getOrderListCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        startOrderListActivity();
      }
    });
  }

  private void startViewModel() {
    mViewModel.start();
  }

  private MainActivityViewModel obtainViewModel(FragmentActivity activity) {
    //  Use a Factory to inject dependencies into the ViewModel.
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(MainActivityViewModel.class);
  }


  @Override
  public void startInitialSettingsDialogFragment() {
    Bundle args = new Bundle();

    String repName = mViewModel.getRepName();
    args.putString(getString(R.string.pref_key_rep_name), repName);

    String repTerritory = mViewModel.getRepTerritory();
    args.putString(getString(R.string.pref_key_rep_territory), repTerritory);

    //  Create instance of dialog fragment used to help User fill in the blanks.
    InitialSettingsDialogFragment dialogFragment = InitialSettingsDialogFragment
        .createInstance(args);

    //  Make sure dialog can not be cancelable.
    dialogFragment.setCancelable(false);

    //  Initializations complete, show that dialog!
    dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
  }

  @Override
  public void startOrderListActivity() {
    Intent intent = new Intent(this, OrderListActivity.class);
    startActivityForResult(intent, OrderListActivity.REQUEST_CODE);

    finish();
  }

  @Override
  public void onSave() {
    Toast.makeText(this, R.string.toast_dialog_settings_success,
        Toast.LENGTH_SHORT).show();

    startOrderListActivity();
  }
}