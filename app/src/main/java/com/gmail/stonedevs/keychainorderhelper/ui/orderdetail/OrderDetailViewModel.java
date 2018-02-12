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

package com.gmail.stonedevs.keychainorderhelper.ui.orderdetail;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadOneCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.KeychainEntity;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;

/**
 * Listens to user actions from item list in {@link OrderDetailFragment} and redirects them to the
 * fragment's action listener.
 */
public class OrderDetailViewModel extends AndroidViewModel implements LoadOneCallback {

  public ObservableField<Order> order = new ObservableField<>();

  public ObservableList<KeychainEntity> items = new ObservableArrayList<>();

  public ObservableBoolean dataLoading = new ObservableBoolean();

  private SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();

  private Repository mRepository;

  private final SnackBarMessage mSnackBarMessage = new SnackBarMessage();

  public OrderDetailViewModel(
      @NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
  }

  public SnackBarMessage getSnackBarMessage() {
    return mSnackBarMessage;
  }

  public SingleLiveEvent<Void> getSendOrderCommand() {
    return mSendOrderCommand;
  }

  @NonNull
  protected String getOrderId() {
    return order.get().getId();
  }

  public void start(@NonNull String orderId) {
    dataLoading.set(true);
    mRepository.get(orderId, this);
  }

  public void setOrder(@NonNull Order order) {
    this.order.set(order);
  }

  private void showSnackBarMessage(@StringRes Integer message) {
    mSnackBarMessage.setValue(message);
  }

  public void sendOrder() {
    mSendOrderCommand.call();
  }

  @Override
  public void onDataLoaded(Order order) {
    setOrder(order);
    dataLoading.set(false);
  }

  @Override
  public void onDataNotAvailable() {
    //  For future use, with remote data source.
    dataLoading.set(false);
  }
}
