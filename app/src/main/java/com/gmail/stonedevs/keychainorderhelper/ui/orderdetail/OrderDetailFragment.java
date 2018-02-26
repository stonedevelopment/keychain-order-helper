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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for Order Detail screen.
 *
 * Users are shown a list of keychains and their quantity.
 * Users can resend the order if need be.
 */
public class OrderDetailFragment extends Fragment {

  private OrderDetailViewModel mViewModel;

  private OrderDetailAdapter mAdapter;

  public OrderDetailFragment() {
    // Required empty public constructor
  }

  public static OrderDetailFragment createInstance() {
    return new OrderDetailFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    mViewModel = OrderDetailActivity.obtainViewModel(getActivity());

    return inflater.inflate(R.layout.fragment_order_detail, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupAdapter();

    subscribeToSnackBarMessenger();

    subscribeToViewModelEvents();
  }

  @Override
  public void onResume() {
    super.onResume();

    String orderId = getArguments().getString(getString(R.string.bundle_key_order_id));
    mViewModel.start(orderId);
  }

  private void setupAdapter() {
    RecyclerView recyclerView = getView().findViewById(R.id.keychainListRecyclerView);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        layoutManager.getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);

    mAdapter = new OrderDetailAdapter();

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
    mViewModel.getDataLoadingEvent().observe(this, new Observer<Boolean>() {
      @Override
      public void onChanged(@Nullable Boolean isDataLoading) {
        //  Determine whether data is loading, react accordingly.
        if (isDataLoading) {
          //  Show progress bar.
          ProgressBar progressBar = getView().findViewById(R.id.progressBar);
          progressBar.setVisibility(View.VISIBLE);

          //  Hide recyclerView.
          RecyclerView recyclerView = getView().findViewById(R.id.keychainListRecyclerView);
          recyclerView.setVisibility(View.GONE);
        } else {
          //  Hide progress bar.
          ProgressBar progressBar = getView().findViewById(R.id.progressBar);
          progressBar.setVisibility(View.GONE);

          //  Show recyclerView.
          RecyclerView recyclerView = getView().findViewById(R.id.keychainListRecyclerView);
          recyclerView.setVisibility(View.VISIBLE);
        }
      }
    });

    mViewModel.getDataLoadedEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        mAdapter.setData(order.getOrderItems());

        mViewModel.getUpdateUIEvent().setValue(order);
      }
    });
  }
}