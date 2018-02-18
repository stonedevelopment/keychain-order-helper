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
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;
import java.util.List;

/**
 * Main UI for the New Order screen.
 *
 * Users can enter a store name, order date, and click on the list to adjust quantities of
 * keychains.
 */
public class NewOrderFragment extends Fragment implements OnFocusChangeListener {

  private final static String TAG = NewOrderFragment.class.getSimpleName();

  private EditText mStoreNameEditText;

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

    View view = inflater.inflate(R.layout.fragment_new_order, container, false);

    //  Store Name EditText
    mStoreNameEditText = view.findViewById(R.id.storeNameEditText);
    mStoreNameEditText.setOnFocusChangeListener(this);
    mStoreNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //  do nothing, for now?
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        //  do nothing, for now?
      }

      @Override
      public void afterTextChanged(Editable s) {
        mViewModel.updateStoreName(s.toString());
      }
    });

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupAdapter();

    setupFab();

    subscribeToSnackBarMessenger();

    subscribeToUIObservableEvents();
  }

  @Override
  public void onResume() {
    super.onResume();

    mViewModel.start();
  }

  private void setupAdapter() {
    RecyclerView recyclerView = getView().findViewById(R.id.keychainListRecyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    mAdapter = new NewOrderAdapter();

    recyclerView.setAdapter(mAdapter);
  }

  private void setupFab() {
    FloatingActionButton fab = getActivity().findViewById(R.id.fab);
    fab.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mViewModel.getSendOrderCommand().call();
      }
    });

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
    mViewModel.getUpdateUIStoreNameText().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
        mStoreNameEditText.setText(s);
      }
    });

    mViewModel.getDataLoadingEvent().observe(this, new Observer<Boolean>() {
      @Override
      public void onChanged(@Nullable Boolean isDataLoading) {
        //  Determine whether data is loading, react accordingly.
        if (isDataLoading) {
          //  Show progress bar.
          ProgressBar progressBar = getView().findViewById(R.id.progressBar);
          progressBar.setVisibility(View.VISIBLE);

          //  Hide container layout.
          ScrollView layout = getView().findViewById(R.id.layout);
          layout.setVisibility(View.GONE);
        } else {
          //  Hide progress bar.
          ProgressBar progressBar = getView().findViewById(R.id.progressBar);
          progressBar.setVisibility(View.GONE);

          //  Show container layout.
          ScrollView layout = getView().findViewById(R.id.layout);
          layout.setVisibility(View.VISIBLE);
        }
      }
    });

    mViewModel.getDataLoadedEvent().observe(this, new Observer<List<NewOrderAdapterItem>>() {
      @Override
      public void onChanged(@Nullable List<NewOrderAdapterItem> items) {
        mAdapter.replaceData(items);
      }
    });

    mViewModel.getErrorLoadingDataEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        throw new RuntimeException("Keychain list failed to retrieve items.");
      }
    });
  }

  /**
   * Focus change override to force keyboard to close
   */
  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus) {
      InputMethodManager mImMan = (InputMethodManager) getContext()
          .getSystemService(Context.INPUT_METHOD_SERVICE);
      if (mImMan != null) {
        mImMan.hideSoftInputFromWindow(v.getWindowToken(), 0);
      }
    }
  }
}
