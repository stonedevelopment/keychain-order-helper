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

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;

/**
 * Exposes SingleLiveEvents and Snackbar to {@link MainActivityFragment}.
 */

public class MainActivityViewModel extends AndroidViewModel {

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Commands directed by System
  private final SingleLiveEvent<Void> mOpenRequiredFieldsDialogCommand = new SingleLiveEvent<>();

  //  Commands directed by User via on-screen buttons.
  private final SingleLiveEvent<Void> mNewOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mOrderListCommand = new SingleLiveEvent<>();

  private String mRepName;
  private String mRepTerritory;

  public MainActivityViewModel(
      @NonNull Application application, Repository repository) {
    super(application);

  }

  void start() {
    //  Get values from prefs or their defaults.
    setupDefaultValues();

    checkReady();
  }

  void checkReady() {
    //  If required fields are not empty, open main activity,
    //  Otherwise, open dialog for User to enter name and territory.
    if (isReady()) {
      getOrderListCommand().call();
    } else {
      getOpenRequiredFieldsDialogCommand().call();
    }
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

  SingleLiveEvent<Void> getOpenRequiredFieldsDialogCommand() {
    return mOpenRequiredFieldsDialogCommand;
  }

  SingleLiveEvent<Void> getNewOrderCommand() {
    return mNewOrderCommand;
  }

  SingleLiveEvent<Void> getOrderListCommand() {
    return mOrderListCommand;
  }

  boolean isReady() {
    return !mRepName.isEmpty() && !mRepTerritory.isEmpty();
  }

  private void setupDefaultValues() {
    Context c = getApplication().getApplicationContext();

    SharedPreferences prefs = PreferenceManager
        .getDefaultSharedPreferences(c);

    //  Get saved rep values from SharedPreferences
    mRepName = prefs.getString(c.getString(R.string.pref_key_rep_name), "");
    mRepTerritory = prefs.getString(c.getString(R.string.pref_key_rep_territory), "");
  }

  void handleActivityResult(int requestCode, int resultCode) {
    switch (requestCode) {
      case NewOrderActivity.REQUEST_CODE:
        switch (resultCode) {
          case Activity.RESULT_CANCELED:
            mSnackBarMessenger.setValue(R.string.snackbar_message_cancel_order_success);
            break;
          case NewOrderActivity.SENT_RESULT_OK:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_success);
            break;
        }
        break;
    }
  }
}