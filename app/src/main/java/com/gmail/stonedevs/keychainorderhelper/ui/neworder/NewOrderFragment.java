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


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackbarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.databinding.FragmentNewOrderBinding;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for the New Order screen. Users can enter a store name, order date, and click on the list
 * to adjust quantities of keychains.
 */
public class NewOrderFragment extends Fragment {

  private NewOrderViewModel mViewModel;

  private FragmentNewOrderBinding mBinding;

  public NewOrderFragment() {
    // Required empty public constructor
  }

  public static NewOrderFragment createInstance() {
    return new NewOrderFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    View rootView = inflater.inflate(R.layout.fragment_new_order, container, false);

    if (mBinding == null) {
      mBinding = FragmentNewOrderBinding.bind(rootView);
    }

    mViewModel = NewOrderActivity.obtainViewModel(getActivity());

    mBinding.setViewModel(mViewModel);

    setHasOptionsMenu(false);
    setRetainInstance(false);

    return mBinding.getRoot();
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupSnackBar();

    setupActionBar();

    loadData();
  }

  private void setupSnackBar() {
    mViewModel.getSnackBarMessage().observe(this, new SnackbarObserver() {
      @Override
      public void onNewMessage(int snackbarMessageResourceId) {
        SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
      }
    });
  }

  private void setupActionBar() {
    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

    if (actionBar == null) {
      return;
    }

    actionBar.setTitle(R.string.action_bar_title_newOrderFragment);
  }

  private void loadData() {
    //  Tell ViewModel that it's a new order.
    mViewModel.start(null);
  }
}
