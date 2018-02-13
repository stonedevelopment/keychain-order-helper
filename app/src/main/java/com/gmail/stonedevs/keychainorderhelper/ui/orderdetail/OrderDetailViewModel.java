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
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadOrderCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;

/**
 * Listens to user actions from item list in {@link OrderDetailFragment} and redirects them to the
 * fragment's action listener.
 */
public class OrderDetailViewModel extends AndroidViewModel implements LoadOrderCallback {

  public final ObservableField<Order> order = new ObservableField<>();

  public final ObservableList<OrderItem> items = new ObservableArrayList<>();

  public final ObservableBoolean dataLoading = new ObservableBoolean();

  private final SingleLiveEvent<Order> mSendOrderCommand = new SingleLiveEvent<>();

  private final Repository mRepository;

  private final SnackBarMessage mSnackBarMessage = new SnackBarMessage();

  public OrderDetailViewModel(
      @NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
  }

  SnackBarMessage getSnackBarMessage() {
    return mSnackBarMessage;
  }

  SingleLiveEvent<Order> getSendOrderCommand() {
    return mSendOrderCommand;
  }

  @NonNull
  protected String getOrderId() {
    return order.get().getId();
  }

  Repository getRepository() {
    return mRepository;
  }

  public void start(@NonNull String orderId) {
    dataLoading.set(true);
    mRepository.getOrder(orderId, this);
  }

  public void setOrder(@NonNull Order order) {
    this.order.set(order);
  }

  private void showSnackBarMessage(@StringRes Integer message) {
    mSnackBarMessage.setValue(message);
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
