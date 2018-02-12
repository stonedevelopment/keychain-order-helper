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
import android.support.annotation.Nullable;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.Date;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel implements DataSource.LoadOneCallback {

  public final ObservableField<String> storeName = new ObservableField<>();

  public final ObservableField<Date> orderDate = new ObservableField<>();

  public final ObservableBoolean dataLoading = new ObservableBoolean();

  private final SnackBarMessage mSnackbarText = new SnackBarMessage();

  private final SingleLiveEvent<Void> mOrderUpdated = new SingleLiveEvent<>();

  private final Repository mRepository;

  @Nullable
  private String mOrderId;

  private boolean mIsNewOrder;

  private boolean mIsDataLoaded = false;

  public NewOrderViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
  }

  public void start(String orderId) {
    if (dataLoading.get()) {
      //  still loading, ignore call.
      return;
    }

    mOrderId = orderId;

    if (orderId == null) {
      //  new order
      mIsNewOrder = true;
      return;
    }

    if (mIsDataLoaded) {
      return;
    }

    mIsNewOrder = false;
    dataLoading.set(true);

    mRepository.get(orderId, this);
  }

  @Override
  public void onDataLoaded(Order order) {
    storeName.set(order.getStoreName());
    orderDate.set(order.getOrderDate());

    dataLoading.set(false);
    mIsDataLoaded = true;
  }

  @Override
  public void onDataNotAvailable() {
    dataLoading.set(false);
  }

  void save() {
    Order order = new Order(storeName.get(), orderDate.get());
    if (!order.isComplete()) {
      //  // TODO: 2/11/2018 Update when an order is considered incomplete with empty keychains.
      mSnackbarText.setValue(R.string.dialog_message_incomplete_order);
    }

    if (isNewOrder() || mOrderId == null) {
      createOrder(order);
    } else {
      order = new Order(mOrderId, storeName.get(), orderDate.get());
      updateOrder(order);
    }
  }

  SnackBarMessage getSnackBarMessage() {
    return mSnackbarText;
  }

  SingleLiveEvent<Void> getOrderUpdatedEvent() {
    return mOrderUpdated;
  }

  private boolean isNewOrder() {
    return mIsNewOrder;
  }

  private void createOrder(Order newOrder) {
    mRepository.save(newOrder);
    mOrderUpdated.call();
  }

  private void updateOrder(Order order) {
    if (isNewOrder()) {
      throw new RuntimeException("updateOrder() was called, but order is new.");
    }

    mRepository.save(order);
    mOrderUpdated.call();
  }
}