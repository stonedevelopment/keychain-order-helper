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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for the main screen.
 *
 * Users can start a new order, view a list of created orders, or open settings.
 */
public class MainActivityFragment extends Fragment {

  private MainActivityViewModel mViewModel;

  public MainActivityFragment() {
    // Required empty public constructor
  }

  public static MainActivityFragment createInstance() {
    return new MainActivityFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    mViewModel = MainActivity.obtainViewModel(getActivity());

    View view = inflater.inflate(R.layout.fragment_main, container, false);

    //  Button that allows User to create a new order.
    Button newOrderButton = view.findViewById(R.id.newOrderButton);
    newOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mViewModel.getCheckReadyEvent().call();
      }
    });

    //  Button that allows User to view a list of previously saved Orders.
    Button orderListButton = view.findViewById(R.id.orderListButton);
    orderListButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mViewModel.getOrderListCommand().call();
      }
    });

    //  Button that allows User to view and set Settings (SharedPreferences).
    Button settingsButton = view.findViewById(R.id.settingsButton);
    settingsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
      }
    });

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    subscribeToSnackBarMessenger();
  }

  private void subscribeToSnackBarMessenger() {
    mViewModel.getSnackBarMessenger().observe(this, new SnackbarObserver() {
      @Override
      public void onNewMessage(int resourceId) {
        SnackbarUtils.showSnackbar(getView(), getString(resourceId));
      }
    });
  }
}