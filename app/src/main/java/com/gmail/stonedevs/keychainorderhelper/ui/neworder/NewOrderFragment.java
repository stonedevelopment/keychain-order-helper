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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for the New Order screen.
 *
 * Users must enter a store name, and can click on the list to adjust quantities of
 * keychains.
 */
public class NewOrderFragment extends Fragment {

  private final static String TAG = NewOrderFragment.class.getSimpleName();

  private NewOrderViewModel mViewModel;

  private NewOrderAdapter mAdapter;

  public NewOrderFragment() {
    // Required empty public constructor
  }

  public static NewOrderFragment createInstance() {
    return new NewOrderFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    mViewModel = NewOrderActivity.obtainViewModel(getActivity());

    boolean isNewOrder;
    String orderId;
    if (savedInstanceState != null) {
      orderId = savedInstanceState.getString(getString(R.string.bundle_key_order_id));
      isNewOrder = savedInstanceState.getBoolean(getString(R.string.bundle_key_is_new_order));

      Log.w(TAG,
          "onCreateView: savedInstanceState: orderId: " + orderId + ", isNewOrder: " + isNewOrder);
    } else {
      if (getArguments() == null ||
          getArguments().getString(getString(R.string.bundle_key_order_id)) == null) {
        orderId = null;
        isNewOrder = true;

        Log.w(TAG, "onCreateView: arguments null:  new order");
      } else {
        orderId = getArguments().getString(getString(R.string.bundle_key_order_id));
        isNewOrder = false;

        Log.w(TAG, "onCreateView: arguments not null, orderId: " + orderId);
      }
    }

    mViewModel.setOrderId(orderId);
    mViewModel.setIsNewOrder(isNewOrder);

    return inflater.inflate(R.layout.fragment_new_order, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupActionBar();

    setupAdapter();

    subscribeToSnackBarMessenger();

    subscribeToViewModelEvents();

    startViewModel();
  }

  @Override
  public void onStop() {
    super.onStop();

    stopViewModel();
  }

  private void setupActionBar() {
    ActionBar actionBar = ((NewOrderActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }

    Bundle args = getArguments();
    if (args == null) {
      actionBar.setTitle(R.string.layout_actionbar_title_new_order);
    } else {
      actionBar.setTitle(R.string.layout_actionbar_title_edit_order);
    }
  }

  private void setupAdapter() {
    RecyclerView recyclerView = getView().findViewById(R.id.keychainListRecyclerView);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        layoutManager.getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);

    mAdapter = new NewOrderAdapter(getActivity(), mViewModel);

    recyclerView.setAdapter(mAdapter);
  }

  private void subscribeToSnackBarMessenger() {
    mViewModel.getSnackBarMessenger().observe(this, new SnackbarObserver() {
      @Override
      public void onNewMessage(int resourceId) {
        SnackbarUtils.showSnackbar(getView(), getString(resourceId));
      }
    });
  }

  private void subscribeToViewModelEvents() {
    mViewModel.getOrderReadyEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        mViewModel.getUpdateUIEvent().setValue(order);
        mAdapter.replaceData(order.getOrderItems());
      }
    });
  }

  private void startViewModel() {
    mViewModel.start();
  }

  private void stopViewModel() {
    mViewModel.stop();
  }
}