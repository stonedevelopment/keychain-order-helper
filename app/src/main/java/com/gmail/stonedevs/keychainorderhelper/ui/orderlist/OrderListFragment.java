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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.databinding.FragmentOrderListBinding;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class OrderListFragment extends Fragment {

  private OrderListViewModel mViewModel;

  private FragmentOrderListBinding mBinding;

  public OrderListFragment() {
    // Required empty public constructor
  }

  public static OrderListFragment createInstance() {
    return new OrderListFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    mBinding = FragmentOrderListBinding.inflate(inflater, container, false);

    mViewModel = OrderListActivity.obtainViewModel(getActivity());

    mBinding.setViewModel(mViewModel);

    //  for future use, set menu options
    //  setHasOptionsMenu(true);

    return mBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupSnackBar();

    setupAdapter();
  }

  @Override
  public void onResume() {
    super.onResume();

    mViewModel.start();
  }

  private void setupSnackBar() {
    mViewModel.getSnackBarMessage().observe(this, new SnackbarObserver() {
      @Override
      public void onNewMessage(int snackbarMessageResourceId) {
        SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
      }
    });
  }

  private void setupAdapter() {
    RecyclerView recyclerView = mBinding.listRecyclerView;

    OrderListAdapter adapter = new OrderListAdapter(mViewModel, new ArrayList<Order>(0));

    recyclerView.setAdapter(adapter);
  }
}