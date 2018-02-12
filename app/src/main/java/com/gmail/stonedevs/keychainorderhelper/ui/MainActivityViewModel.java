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
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;

/**
 * Exposes SingleLiveEvents and Snackbar to {@link MainActivityFragment}.
 */

public class MainActivityViewModel extends AndroidViewModel {

  private final SnackBarMessage mSnackbarText = new SnackBarMessage();

  private final SingleLiveEvent<Void> mNewOrderEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mViewOrdersEvent = new SingleLiveEvent<>();

  public MainActivityViewModel(
      @NonNull Application application) {
    super(application);
  }

  SnackBarMessage getSnackbarMessage() {
    return mSnackbarText;
  }

  SingleLiveEvent<Void> getNewOrderEvent() {
    return mNewOrderEvent;
  }

  SingleLiveEvent<Void> getViewOrdersEvent() {
    return mViewOrdersEvent;
  }

  void handleActivityResult(int requestCode, int resultCode) {
    if (NewOrderActivity.REQUEST_CODE == requestCode) {
      switch (resultCode) {
        case NewOrderActivity.RESULT_OK:
          mSnackbarText.setValue(R.string.toast_intent_send_order_by_email_success);
          break;
      }
    }
  }
}