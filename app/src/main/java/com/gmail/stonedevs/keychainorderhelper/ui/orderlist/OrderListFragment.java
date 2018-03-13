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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackBarObserver;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Main UI for the view orders screen.
 *
 * User can select an order to view by clicking on a list item.
 * FUTURE:  User can request pop up menu by long-clicking on a list item.
 * todo Generate report per Daran.
 */
public class OrderListFragment extends Fragment {

  private static final String TAG = OrderListFragment.class.getSimpleName();

  private OrderListViewModel mViewModel;

  private OrderListAdapter mAdapter;

  public OrderListFragment() {
    // Required empty public constructor
  }

  public static OrderListFragment createInstance() {
    return new OrderListFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_order_list, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupViewModel();

    setupAdapter();

    subscribeToSnackBarMessenger();

    subscribeToViewModelEvents();

    startViewModel();
  }

  private void setupViewModel() {
    mViewModel = OrderListActivity.obtainViewModel(getActivity());
  }

  private void setupAdapter() {
    RecyclerView recyclerView = getView().findViewById(R.id.orderListRecyclerView);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    mAdapter = new OrderListAdapter(getActivity(), mViewModel);

    recyclerView.setAdapter(mAdapter);
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

    mViewModel.getDataLoadedEvent().observe(this, new Observer<List<Order>>() {
      @Override
      public void onChanged(@Nullable List<Order> orders) {
        mAdapter.replaceData(orders);

        //  Hide no data textView
        TextView textView = getView().findViewById(R.id.noOrdersFoundText);
        textView.setVisibility(View.GONE);
      }
    });

    mViewModel.getNoDataLoadedEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        mAdapter.replaceData(new ArrayList<Order>(0));

        //  Show no data textView
        TextView textView = getView().findViewById(R.id.noOrdersFoundText);
        textView.setVisibility(View.VISIBLE);
      }
    });
  }

  private void startViewModel() {
    mViewModel.start();
  }
}