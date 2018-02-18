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
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel implements NewOrderCreationCallback {

  private final static String TAG = NewOrderViewModel.class.getSimpleName();

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events: Data Loading Changes
  private final SingleLiveEvent<Boolean> mLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<List<OrderItem>> mDataReadyEvent = new SingleLiveEvent<>();
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
    if (mCompleteOrder == null) {
      Log.d(TAG, "start: null");
      createNewOrder();
    } else {
      Log.d(TAG, "start: " + mCompleteOrder.getOrder().toString());
      onOrderCreated(mCompleteOrder);
    }
  }

  void updateStoreName(String storeName) {
    mCompleteOrder.setStoreName(storeName);
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<Boolean> getDataLoadingEvent() {
    return mLoadingEvent;
  }

  SingleLiveEvent<List<OrderItem>> getDataLoadedEvent() {
    return mDataReadyEvent;
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

  private void createNewOrder() {
    mLoadingEvent.setValue(true);

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

        new AppExecutors().mainThread().execute(new Runnable() {
          @Override
          public void run() {
            onOrderCreated(new CompleteOrder(order, orderItems));
          }
        });
      }
    };

    new AppExecutors().diskIO().execute(runnable);
  }

  void resetOrder() {
    createNewOrder();
  }

  @Override
  public void onOrderCreated(CompleteOrder order) {
    mCompleteOrder = order;

    mLoadingEvent.setValue(false);
    mDataReadyEvent.setValue(order.getOrderItems());
  }
}