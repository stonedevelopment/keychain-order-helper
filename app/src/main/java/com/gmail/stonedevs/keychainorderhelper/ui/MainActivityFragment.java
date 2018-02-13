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

package com.gmail.stonedevs.keychainorderhelper.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.crashlytics.android.Crashlytics;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage.SnackbarObserver;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.RequiredFieldsDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.RequiredFieldsDialogFragment.OnRequiredFieldsCheckListener;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtils;
import com.gmail.stonedevs.keychainorderhelper.util.SnackbarUtils;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class MainActivityFragment extends Fragment {

  private MainActivityViewModel mViewModel;

  SingleLiveEvent<Void> mNewOrderEvent = new SingleLiveEvent<>();
  SingleLiveEvent<Void> mViewOrdersEvent = new SingleLiveEvent<>();

  public MainActivityFragment() {
  }

  public static MainActivityFragment createInstance() {
    return new MainActivityFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    mViewModel = MainActivity.obtainViewModel(getActivity());

    View view = inflater.inflate(R.layout.fragment_main, container, false);

    Button orderButton = view.findViewById(R.id.
        orderButton);
    orderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String prefRepName = prefs.getString(getString(R.string.pref_key_rep_name), "");
        String prefRepTerritory = prefs
            .getString(getString(R.string.pref_key_rep_territory), "");

        //  if required fields are empty, open dialog for easy editing
        //  else, send user to order fragment, they followed instructions.
        if (prefRepName.isEmpty() || prefRepTerritory.isEmpty()) {
          Bundle args = new Bundle();
          args.putString(getString(R.string.pref_key_rep_name),
              BuildConfig.DEBUG ? getString(R.string.pref_debug_default_value_rep_name)
                  : prefRepName);
          args.putString(getString(R.string.pref_key_rep_territory),
              BuildConfig.DEBUG ? getString(R.string.pref_debug_default_value_rep_territory)
                  : prefRepTerritory);

          RequiredFieldsDialogFragment dialogFragment = RequiredFieldsDialogFragment
              .createInstance(args);

          dialogFragment.setListener(new OnRequiredFieldsCheckListener() {
            @Override
            public void onSuccess(int resourceId) {
              //  Send a signal to MainActivity that user wants to start new order
              mViewModel.getSnackbarMessage().setValue(resourceId);
              startNewOrder();
            }

            @Override
            public void onFail(int resourceId) {
              mViewModel.getSnackbarMessage().setValue(resourceId);
            }
          });

          dialogFragment.show(getActivity().getSupportFragmentManager(), dialogFragment.getTag());
        } else {
          startNewOrder();
        }
      }
    });

    Button viewOrderButton = view.findViewById(R.id.
        viewOrderButton);
    viewOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mViewOrdersEvent.call();
      }
    });

    Button crashButton = view.findViewById(R.id.crashButton);
    if (BuildConfig.DEBUG) {
      crashButton.setVisibility(View.VISIBLE);
      crashButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          //  Crash!
          Crashlytics.getInstance().crash();
        }
      });
    }

    Button generateButton = view.findViewById(R.id.generateButton);
    if (BuildConfig.DEBUG) {
      generateButton.setVisibility(View.VISIBLE);
      generateButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          //  Generate
          try {
            ExcelUtils.GenerateStringArrayFormat(getActivity());
          } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    Button settingsButton = view.findViewById(R.id.settingsButton);
    settingsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
      }
    });

    return view;
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    setupSnackBar();
  }

  private void setupSnackBar() {
    mViewModel.getSnackbarMessage().observe(this, new SnackbarObserver() {
      @Override
      public void onNewMessage(int snackbarMessageResourceId) {
        SnackbarUtils.showSnackbar(getView(), getString(snackbarMessageResourceId));
      }
    });
  }

  /**
   * Tell {@link MainActivity} that user wants to create a new order.
   */
  void startNewOrder() {
    mNewOrderEvent.call();
  }
}