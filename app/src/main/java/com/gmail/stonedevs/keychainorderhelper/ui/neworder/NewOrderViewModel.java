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

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Intent;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareOrderAsyncTask;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel implements NewOrderCallback,
    InsertCallback {

  private final static String TAG = NewOrderViewModel.class.getSimpleName();

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events: Data Loading Changes
  private final SingleLiveEvent<CompleteOrder> mOrderCreatedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Intent> mIntentReadyEvent = new SingleLiveEvent<>();

  //  Events: UI Changes
  private final SingleLiveEvent<String> mUpdateUIStoreNameTextEvent = new SingleLiveEvent<>();

  //  Commands: User Direction
  private final SingleLiveEvent<Void> mCancelOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mResetOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mPrepareOrderCommand = new SingleLiveEvent<>();

  //  Data repository
  private final Repository mRepository;

  private final AppExecutors mAppExecutors;

  //  View model's data variables
  private CompleteOrder mCompleteOrder;

  public NewOrderViewModel(@NonNull Application application, Repository repository) {
    super(application);

    mRepository = repository;
    mAppExecutors = new AppExecutors();
  }

  public void start() {
    if (mCompleteOrder == null) {
      createNewOrder();
    } else {
      onOrderCreated(mCompleteOrder);
    }
  }

  String getStoreName() {
    return mCompleteOrder.getStoreName();
  }

  void setStoreName(String storeName) {
    mCompleteOrder.setStoreName(storeName);
  }

  int getOrderQuantity() {
    return mCompleteOrder.getOrderQuantity();
  }

  void updateOrderQuantityBy(int change) {
    mCompleteOrder.updateOrderQuantityBy(change);
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<CompleteOrder> getOrderCreatedEvent() {
    return mOrderCreatedEvent;
  }

  SingleLiveEvent<Intent> getIntentReadyEvent() {
    return mIntentReadyEvent;
  }

  SingleLiveEvent<String> getUpdateUIStoreNameTextEvent() {
    return mUpdateUIStoreNameTextEvent;
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

  SingleLiveEvent<Void> getPrepareOrderCommand() {
    return mPrepareOrderCommand;
  }

  private void createNewOrder() {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        String storeName = "";
        Date orderDate = Calendar.getInstance().getTime();

        final Order order = new Order(storeName, orderDate);

        String orderId = order.getId();

        String[] names = getApplication().getResources()
            .getStringArray(R.array.excel_cell_values_names);

        final List<OrderItem> orderItems = new ArrayList<>(0);
        for (String name : names) {
          orderItems.add(new OrderItem(orderId, name, 0));
        }

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            onOrderCreated(new CompleteOrder(order, orderItems));
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  void resetOrder() {
    createNewOrder();
  }

  void prepareToSendOrder() {
    saveOrder();
  }

  void executeFinalPreparations(Activity context) {
    PrepareOrderAsyncTask task = new PrepareOrderAsyncTask(context, mCompleteOrder,
        this);
    task.execute();
  }

  private void saveOrder() {
    mRepository.saveOrder(mCompleteOrder, this);
  }

  boolean isReady() {
    return mCompleteOrder != null && !(isStoreNameEmpty() || isOrderQuantityZero()
        || !doesOrderQuantityMeetMinimumRequirements());
  }

  boolean isStoreNameEmpty() {
    return mCompleteOrder.getStoreName().isEmpty();
  }

  boolean isOrderQuantityZero() {
    return mCompleteOrder.getOrderQuantity() == 0;
  }

  boolean doesOrderQuantityMeetMinimumRequirements() {
    int orderQuantityMinimumRequirement = getApplication().getResources()
        .getInteger(R.integer.order_quantity_minimum_requirement);

    return mCompleteOrder.getOrderQuantity() >= orderQuantityMinimumRequirement;
  }

  @Override
  public void onOrderCreated(CompleteOrder order) {
    mCompleteOrder = order;
    mOrderCreatedEvent.setValue(order);
  }

  @Override
  public void onOrderReadyToSend(Intent intent) {
    mIntentReadyEvent.setValue(intent);
  }

  @Override
  public void onDataInserted() {
    mPrepareOrderCommand.call();
  }
}