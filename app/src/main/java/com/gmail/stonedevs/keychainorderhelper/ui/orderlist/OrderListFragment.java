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
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.db.entity.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;
import java.util.List;

/**
 * Main UI for the view orders screen.
 *
 * User can select an order to view by clicking on a list item.
 * FUTURE:  User can request pop up menu by long-clicking on a list item.
 * todo Generate report per Daran.
 */
public class OrderListFragment extends Fragment {

  private OrderListAdapter mAdapter;

  private OrderListViewModel mViewModel;

  public OrderListFragment() {
    // Required empty public constructor
  }

  public static OrderListFragment createInstance() {
    return new OrderListFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mViewModel = OrderListActivity.obtainViewModel(getActivity());

    return inflater.inflate(R.layout.fragment_order_list, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupAdapter();

    subscribeToSnackBarMessenger();

    subscribeToUIObservableEvents();
  }

  @Override
  public void onResume() {
    super.onResume();

    mViewModel.start();
  }

  private void setupAdapter() {
    RecyclerView recyclerView = getView().findViewById(R.id.orderListRecyclerView);

    mAdapter = new OrderListAdapter(mViewModel);

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

  private void subscribeToUIObservableEvents() {
    mViewModel.getDataLoadingEvent().observe(this, new Observer<Boolean>() {
      @Override
      public void onChanged(@Nullable Boolean isDataLoading) {
        //  Determine whether data is loading, react accordingly.
        if (isDataLoading) {
          //  Show progress bar.
          ProgressBar progressBar = getView().findViewById(R.id.progressBar);
          progressBar.setVisibility(View.VISIBLE);

          //  Hide container layout.
          ConstraintLayout layout = getView().findViewById(R.id.layout);
          layout.setVisibility(View.GONE);
        } else {
          //  Hide progress bar.
          ProgressBar progressBar = getView().findViewById(R.id.progressBar);
          progressBar.setVisibility(View.GONE);

          //  Show container layout.
          ConstraintLayout layout = getView().findViewById(R.id.layout);
          layout.setVisibility(View.VISIBLE);
        }
      }
    });

    mViewModel.getDataLoadedEvent().observe(this, new Observer<List<CompleteOrder>>() {
      @Override
      public void onChanged(@Nullable List<CompleteOrder> orders) {
        mAdapter.replaceData(orders);

        //  Hide no data textView
        TextView textView = getView().findViewById(R.id.noOrdersFoundText);
        textView.setVisibility(View.GONE);

        //  Show recyclerView
        RecyclerView recyclerView = getView().findViewById(R.id.orderListRecyclerView);
        recyclerView.setVisibility(View.VISIBLE);
      }
    });

    mViewModel.getNoDataLoadedEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Hide recyclerView
        RecyclerView recyclerView = getView().findViewById(R.id.orderListRecyclerView);
        recyclerView.setVisibility(View.GONE);

        //  Show no data textView
        TextView textView = getView().findViewById(R.id.noOrdersFoundText);
        textView.setVisibility(View.VISIBLE);
      }
    });
  }
}