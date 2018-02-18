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
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadAllKeychainsCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Keychain;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel implements LoadAllKeychainsCallback {

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events: Data Loading Changes
  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<List<NewOrderAdapterItem>> mDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mErrorLoadingDataEvent = new SingleLiveEvent<>();

  //  Events: UI Changes
  private final SingleLiveEvent<String> mUpdateUIStoreNameText = new SingleLiveEvent<>();

  //  Commands: User Direction
  private final SingleLiveEvent<Void> mCancelOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mResetOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mOpenDatePickerCommand = new SingleLiveEvent<>();

  //  Data repository
  private final Repository mRepository;

  //  View model's data variables
  private CompleteOrder mCompleteOrder;

  public NewOrderViewModel(@NonNull Application application, Repository repository) {
    super(application);

    mRepository = repository;
  }

  public void start() {
    resetOrder();
  }

  void updateStoreName(String storeName, boolean updateUI) {

    if (updateUI) {
      mUpdateUIStoreNameText.setValue(storeName);
    }
  }

  private void setOrderDate(Date orderDate) {
    mOrderDate = orderDate;
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<Boolean> getDataLoadingEvent() {
    return mDataLoadingEvent;
  }

  SingleLiveEvent<List<NewOrderAdapterItem>> getDataLoadedEvent() {
    return mDataLoadedEvent;
  }

  SingleLiveEvent<Void> getErrorLoadingDataEvent() {
    return mErrorLoadingDataEvent;
  }

  SingleLiveEvent<String> getUpdateUIStoreNameText() {
    return mUpdateUIStoreNameText;
  }

  SingleLiveEvent<Void> getOpenDatePickerCommand() {
    return mOpenDatePickerCommand;
  }

  SingleLiveEvent<Void> getCancelOrderCommand() {
    return mCancelOrderCommand;
  }

  SingleLiveEvent<Void> getResetOrderCommand() {
    return mResetOrderCommand;
  }

  SingleLiveEvent<Void> getSendOrderCommand() {
    return mSendOrderCommand;
  }

  void createNewOrder() {
    Order order = new Order("", Calendar.getInstance().getTime());

    mCompleteOrder = new CompleteOrder(order, new ArrayList<OrderItem>(0));
  }

  void resetOrder() {
    updateStoreName(null, true);
    setOrderDate(Calendar.getInstance().getTime());

    //  todo pull default values from repository for list of keychains.
    mDataLoadingEvent.setValue(true);
    mRepository.getAllKeychains(this);
  }

  @Override
  public void onDataNotAvailable() {
    //  no keychains were found, instantiate keychains then return list again
    String[] keychainNames = getApplication().getResources()
        .getStringArray(R.array.excel_cell_values_names);
    String[] quantityCellAddresses = getApplication().getResources()
        .getStringArray(R.array.excel_cell_locations_quantities);

    mRepository.insertKeychains(keychainNames, quantityCellAddresses);
  }

  @Override
  public void onDataLoaded(List<Keychain> keychains) {
    List<NewOrderAdapterItem> items = new ArrayList<>(0);

    for (Keychain keychain : keychains) {
      items.add(new NewOrderAdapterItem(keychain, 0));
    }

    mDataLoadingEvent.setValue(false);
    mDataLoadedEvent.setValue(items);
  }
}