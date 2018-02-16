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
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.db.entity.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.util.DateUtil;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for Order Detail screen.
 *
 * Users are shown a list of keychains and their quantity.
 * Users can resend the order if need be.
 */
public class OrderDetailFragment extends Fragment {

  private TextView mStoreNameTextView;
  private TextView mOrderDateTextView;

  private OrderDetailViewModel mViewModel;

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

    View view = inflater.inflate(R.layout.fragment_order_detail, container, false);

    //  Store Name
    mStoreNameTextView = view.findViewById(R.id.storeNameTextView);

    //  Order Date
    mOrderDateTextView = view.findViewById(R.id.orderDateTextView);

    //  todo keychainListRecyclerView

    //  todo keychainListRecyclerView Adapter

    //  Resend Order Button
    Button resendOrderButton = view.findViewById(R.id.resendOrderButton);
    resendOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        //  Get view model's Send Order command.
        mViewModel.getSendOrderCommand().call();
      }
    });

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    subscribeToSnackBarMessenger();

    subscribeToUIObservableEvents();
  }

  @Override
  public void onResume() {
    super.onResume();

    String orderId = getArguments().getString(getString(R.string.bundle_key_order_id));
    mViewModel.start(orderId);
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

    mViewModel.getDataLoadedEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder completeOrder) {
        Order order = completeOrder.getOrder();

        mStoreNameTextView.setText(order.getStoreName());
        mOrderDateTextView.setText(DateUtil.getFormattedDateForLayout(order.getOrderDate()));

        //  todo Fill keychain list with data
      }
    });

    mViewModel.getErrorLoadingDataEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Data was not received properly.
        //  todo Finish activity with error result.
        ((OrderDetailActivity) getActivity()).closeWithResult(OrderDetailActivity.RESULT_ERROR);
      }
    });
  }
}