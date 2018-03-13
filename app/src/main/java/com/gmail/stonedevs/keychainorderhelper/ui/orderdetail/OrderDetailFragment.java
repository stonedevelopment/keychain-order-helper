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

import static com.gmail.stonedevs.keychainorderhelper.util.BundleUtils.BUNDLE_KEY_ORDER_ID;

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
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackBarObserver;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;
import java.util.ArrayList;
import java.util.List;

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

  public static OrderDetailFragment createInstance(String orderId) {
    Bundle args = new Bundle();
    args.putString(BUNDLE_KEY_ORDER_ID, orderId);

    OrderDetailFragment fragment = new OrderDetailFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_order_detail, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupAdapter();

    setupViewModel();

    subscribeToSnackBarMessenger();

    subscribeToViewModelEvents();

    startViewModel();
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

  private void setupViewModel() {
    mViewModel = OrderDetailActivity.obtainViewModel(getActivity());
  }

  private void subscribeToSnackBarMessenger() {
    mViewModel.getSnackBarMessenger().observe(this, new SnackBarObserver() {
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
        ProgressBar progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(isDataLoading ? View.VISIBLE : View.GONE);
      }
    });

    mViewModel.getDataLoadedEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        //  Strip out the items without a quantity, but retain original list for email.
        List<OrderItem> orders = new ArrayList<>(0);
        for (OrderItem item : order.getOrderItems()) {
          if (item.getQuantity() > 0) {
            orders.add(item);
          }
        }

        mAdapter.setData(orders);

        //  Hide no data textView
        TextView textView = getView().findViewById(R.id.noKeychainsFoundText);
        textView.setVisibility((orders.size() > 0) ? View.GONE : View.VISIBLE);
      }
    });
  }

  private void startViewModel() {
    String orderId = getArguments().getString(getString(R.string.bundle_key_order_id));
    mViewModel.start(orderId);
  }
}