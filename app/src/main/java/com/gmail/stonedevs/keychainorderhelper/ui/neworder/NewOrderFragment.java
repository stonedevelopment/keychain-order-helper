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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;

/**
 * Main UI for the New Order screen.
 *
 * Users can enter a store name, order date, and click on the list to adjust quantities of
 * keychains.
 */
public class NewOrderFragment extends Fragment {

  private NewOrderViewModel mViewModel;

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
    EditText storeNameEditText = view.findViewById(R.id.storeNameEditText);
    storeNameEditText.addTextChangedListener(new TextWatcher() {
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

    //  Order Date Button
    Button orderDateButton = view.findViewById(R.id.orderDateButton);
    orderDateButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        //  Open date picker dialog.
      }
    });

    //  todo Keychain List RecyclerView

    //  todo Set Keychain List RecyclerView Adapter

    //  Reset Order Button
    Button resetButton = view.findViewById(R.id.resetOrderButton);
    resetButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        //  Get ViewModel's ResetOrderCommand
        mViewModel.getResetOrderCommand().call();
      }
    });

    //  Send Order Button
    Button sendOrderButton = view.findViewById(R.id.sendOrderButton);
    sendOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        //  Get ViewModel's SendOrderCommand
        mViewModel.getSendOrderCommand().call();
      }
    });

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    subscribeToSnackBarMessenger();
  }

  private void subscribeToSnackBarMessenger() {
    mViewModel.getSnackBarMessenger().observe(this, new SnackbarObserver() {
      @Override
      public void onNewMessage(int resourceId) {
        SnackbarUtils.showSnackbar(getView(), getString(resourceId));
      }
    });
  }
}
