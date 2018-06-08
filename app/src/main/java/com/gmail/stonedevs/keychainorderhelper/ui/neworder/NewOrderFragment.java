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

package com.gmail.stonedevs.keychainorderhelper.ui.neworder;

import static com.gmail.stonedevs.keychainorderhelper.util.BundleUtils.BUNDLE_KEY_ORDER_CATEGORY;
import static com.gmail.stonedevs.keychainorderhelper.util.BundleUtils.BUNDLE_KEY_ORDER_ID;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackBarObserver;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for the New Order screen.
 *
 * Users can click on the list to adjust quantities of items, and edit the territory this order
 * belongs to.
 */
public class NewOrderFragment extends Fragment {

  //  Debug tag used for logging.
  private static final String TAG = NewOrderFragment.class.getSimpleName();

  private NewOrderViewModel mViewModel;

  private NewOrderAdapter mAdapter;

  /**
   * Required default constructor.
   */
  public NewOrderFragment() {
    // Required empty public constructor
  }

  /**
   * Creates an instance of this fragment. If a non-null argument is passed, this means that the
   * activity will be reactive for editing an order, instead of its default behavior of creating a
   * new order. todo update this documentation
   *
   * @param orderCategory The argument passed for if we're creating an order. This is the category
   * for the {@link Order} entity class.
   * @return The created instance of this fragment.
   */
  public static NewOrderFragment createInstance(int orderCategory) {
    Bundle args = new Bundle();
    args.putInt(BUNDLE_KEY_ORDER_CATEGORY, orderCategory);

    NewOrderFragment fragment = new NewOrderFragment();
    fragment.setArguments(args);
    return fragment;
  }

  /**
   * Creates an instance of this fragment. If a non-null argument is passed, this means that the
   * activity will be reactive for editing an order, instead of its default behavior of creating a
   * new order. todo update this documentation
   *
   * @param orderId The argument passed for if we're editing an order. This is the row id for the
   * {@link Order} entity class.
   * @return The created instance of this fragment.
   */
  public static NewOrderFragment createInstance(String orderId) {
    Bundle args = new Bundle();
    args.putString(BUNDLE_KEY_ORDER_ID, orderId);

    NewOrderFragment fragment = new NewOrderFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_new_order, container, false);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupViewModel();

    setupAdapter();

    subscribeToSnackBarMessenger();

    subscribeToViewModelEvents();

    startViewModel();

    setupActionBar();
  }

  private void setupViewModel() {
    mViewModel = NewOrderActivity.obtainViewModel(getActivity());
  }

  private void setupActionBar() {
    ActionBar actionBar = ((NewOrderActivity) getActivity()).getSupportActionBar();
    if (actionBar == null) {
      return;
    }

    if (mViewModel.isNewOrder()) {
      actionBar.setTitle(R.string.layout_actionbar_title_new_order);
    } else {
      actionBar.setTitle(R.string.layout_actionbar_title_edit_order);
    }
  }

  private void setupAdapter() {
    RecyclerView recyclerView = getView().findViewById(R.id.orderItemListRecyclerView);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        layoutManager.getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);

    mAdapter = new NewOrderAdapter(getActivity(), mViewModel);

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
    mViewModel.getDataLoadedEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        mAdapter.replaceData(order.getOrderItems());
        mAdapter.updateItemQuantities(getContext(), order.getOrderCategory());
      }
    });

    mViewModel.getDataLoadingEvent().observe(this, new Observer<Boolean>() {
      @Override
      public void onChanged(@Nullable Boolean isDataLoading) {
        //  Determine whether data is loading, react accordingly.
        ProgressBar progressBar = getView().findViewById(R.id.progressBar);
        progressBar.setVisibility(isDataLoading ? View.VISIBLE : View.GONE);
      }
    });
  }

  private void startViewModel() {
    String orderId = getArguments().getString(getString(R.string.bundle_key_order_id));
    //  attempt to grab an order id from intent bundle

    //  if orderId is not empty, the User is attempting to edit a previously created order
    if (!TextUtils.isEmpty(orderId)) {
      //  create an instance of a fragment used for editing
      mViewModel.start(orderId);
    } else {
      //  create an instance of a fragment used for creating
      int orderCategory = getArguments().getInt(BUNDLE_KEY_ORDER_CATEGORY);
      mViewModel.start(orderCategory);
    }
  }
}