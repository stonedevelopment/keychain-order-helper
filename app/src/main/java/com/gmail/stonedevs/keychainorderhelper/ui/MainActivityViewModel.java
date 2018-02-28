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

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;

/**
 * Exposes SingleLiveEvents and Snackbar to {@link MainActivity}.
 */

public class MainActivityViewModel extends AndroidViewModel {

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Commands directed by System logic
  private final SingleLiveEvent<Void> mOpenInitialSettingsDialogCommand = new SingleLiveEvent<>();

  //  Commands directed by User via on-screen buttons.
  private final SingleLiveEvent<Void> mOrderListCommand = new SingleLiveEvent<>();

  //  ViewModel variables
  private String mRepName;
  private String mRepTerritory;

  public MainActivityViewModel(@NonNull Application application) {
    super(application);

    setupDefaultValues();
  }

  String getRepName() {
    return mRepName;
  }

  String getRepTerritory() {
    return mRepTerritory;
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<Void> getOpenInitialSettingsDialogCommand() {
    return mOpenInitialSettingsDialogCommand;
  }

  SingleLiveEvent<Void> getOrderListCommand() {
    return mOrderListCommand;
  }

  /**
   * Called when the fragment is ready.
   */
  void start() {
    validateRequiredFields();
  }

  /**
   * Fills rep variables with saved or default values.
   */
  private void setupDefaultValues() {
    Context c = getApplication().getApplicationContext();

    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(c);

    //  Get saved rep values from SharedPreferences
    mRepName = prefs.getString(c.getString(R.string.pref_key_rep_name), "");
    mRepTerritory = prefs.getString(c.getString(R.string.pref_key_rep_territory), "");
  }

  /**
   * Are both required fields filled out?
   */
  private boolean isReady() {
    return !TextUtils.isEmpty(mRepName);
  }

  /**
   * If the Required Fields are not empty, open {@link OrderListActivity},
   * Otherwise, open dialog for user to enter their name and territory.
   */
  private void validateRequiredFields() {
    if (isReady()) {
      getOrderListCommand().call();
    } else {
      getOpenInitialSettingsDialogCommand().call();
    }
  }
}