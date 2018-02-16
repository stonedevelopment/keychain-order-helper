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

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel {

  private final ObservableField<String> mObservableStoreName = new ObservableField<>();
  private final ObservableField<Date> mObservableOrderDate = new ObservableField<>();
  private final ObservableBoolean mObservableDataLoading = new ObservableBoolean();

  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Commands directed by System
  private final SingleLiveEvent<Void> mOpenDialogCommand = new SingleLiveEvent<>();

  //  Commands directed by User via on-screen interactions.
  private final SingleLiveEvent<Void> mResetOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();

  public NewOrderViewModel(@NonNull Application application) {
    super(application);
  }

  public void start() {
    resetOrder();
  }

  void updateStoreName(String storeName) {
    if (!Objects.equals(mObservableStoreName.get(), storeName)) {
      mObservableStoreName.set(storeName);
    }
  }

  void updateOrderDate(Date orderDate) {
    if (!Objects.equals(mObservableOrderDate.get(), orderDate)) {
      mObservableOrderDate.set(orderDate);
    }
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<Void> getOpenDialogCommand() {
    return mOpenDialogCommand;
  }

  SingleLiveEvent<Void> getResetOrderCommand() {
    return mResetOrderCommand;
  }

  SingleLiveEvent<Void> getSendOrderCommand() {
    return mSendOrderCommand;
  }

  void resetOrder() {
    mObservableStoreName.set(null);
    mObservableOrderDate.set(Calendar.getInstance().getTime());

    //  pull default values from repository for list of keychains.

  }
}