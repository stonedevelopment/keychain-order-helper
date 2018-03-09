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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareIntentCallback;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareSendActionIntentAsyncTask;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel implements NewOrderCallback,
    InsertCallback, LoadCallback, PrepareIntentCallback {

  private final static String TAG = NewOrderViewModel.class.getSimpleName();

  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events
  private final SingleLiveEvent<CompleteOrder> mOrderReadyEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Intent> mIntentReadyEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<CompleteOrder> mUpdateUIEvent = new SingleLiveEvent<>();

  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<CompleteOrder> mDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mErrorLoadingDataEvent = new SingleLiveEvent<>();

  //  Commands
  private final SingleLiveEvent<Void> mCancelOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mResetOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();

  private final Repository mRepository;
  private final AppExecutors mAppExecutors;

  //  RowID of Order, lazily used to determine if creating order or editing.
  private String mOrderId;

  //  Object that contains the Order and its OrderItems.
  private CompleteOrder mWorkingOrder;

  //  Are we getting data from database?
  private boolean mLoadingData;

  //  Are we in the sending order process?
  private boolean mSendingOrder;

  public NewOrderViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
    mAppExecutors = new AppExecutors();
  }

  void start() {
    if (mLoadingData) {
      //  Loading data, ignore.
      return;
    }

    if (mWorkingOrder == null) {
      //  main order object is null, create a new one or load from database.
      beginLoadingPhase();

      //  if order id wasn't set, then it's a new order, create it, otherwise load it.
      if (mOrderId == null) {
        createOrder();
      } else {
        loadOrder(mOrderId);
      }
    } else {
      //  main order object exists and is ready for ui to update with its contents.
      readyOrder();
    }
  }

  void setOrderId(String orderId) {
    mOrderId = orderId;
  }

  /**
   * Returns if Order is perceived to be a New Order, lazily checking if mOrderId is
   * null.
   */
  boolean isNewOrder() {
    return mOrderId == null;
  }

  boolean isStoreNameEmpty() {
    return mWorkingOrder.getStoreName().isEmpty();
  }

  void updateStoreName(String storeName) {
    if (mWorkingOrder != null) {
      mWorkingOrder.setStoreName(storeName);
    } else {
      mOrderId = null;
    }
  }

  private void updateOrderDate() {
    mWorkingOrder.updateOrderDate();
  }

  boolean hasTerritory() {
    return hasSetTerritory() || hasPrefTerritory();
  }

  boolean hasSetTerritory() {
    return mWorkingOrder.hasOrderTerritory();
  }

  boolean hasPrefTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    String prefsTerritory = prefs
        .getString(getApplication().getString(R.string.pref_key_rep_territory), null);

    return !TextUtils.isEmpty(prefsTerritory);
  }

  /**
   * Return Territory if saved to ViewModel or saved in sharedPreferences.
   */
  String getTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    return mWorkingOrder.hasOrderTerritory() ? mWorkingOrder.getOrderTerritory() : prefs
        .getString(getApplication().getString(R.string.pref_key_rep_territory), null);
  }

  void setTerritory(String territory) {
    mWorkingOrder.setOrderTerritory(territory);
  }

  int getOrderQuantity() {
    return mWorkingOrder.getOrderQuantity();
  }

  boolean isOrderQuantityZero() {
    return mWorkingOrder.getOrderQuantity() == 0;
  }

  boolean doesOrderQuantityMeetMinimumRequirements() {
    int orderQuantityMinimumRequirement = getApplication().getResources()
        .getInteger(R.integer.order_quantity_minimum_requirement);

    return mWorkingOrder.getOrderQuantity() >= orderQuantityMinimumRequirement;
  }

  void updateOrderQuantityBy(int change) {
    mWorkingOrder.updateOrderQuantityBy(change);

    updateUI();
  }

  boolean isSendingOrder() {
    return mSendingOrder;
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<CompleteOrder> getOrderReadyEvent() {
    return mOrderReadyEvent;
  }

  SingleLiveEvent<Intent> getIntentReadyEvent() {
    return mIntentReadyEvent;
  }

  SingleLiveEvent<CompleteOrder> getUpdateUIEvent() {
    return mUpdateUIEvent;
  }

  SingleLiveEvent<Boolean> getDataLoadingEvent() {
    return mDataLoadingEvent;
  }

  SingleLiveEvent<CompleteOrder> getDataLoadedEvent() {
    return mDataLoadedEvent;
  }

  SingleLiveEvent<Void> getErrorLoadingDataEvent() {
    return mErrorLoadingDataEvent;
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

  private void createOrder() {
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
            readyOrder(new CompleteOrder(order, orderItems));
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  private void loadOrder(String orderId) {
    mRepository.getOrder(orderId, this);
  }

  private void saveOrder() {
    mRepository.saveOrder(mWorkingOrder, this);
  }

  void resetOrder() {
    createOrder();
  }

  /**
   * Readies order for observers to use.
   */
  private void readyOrder() {
    updateUI();

    endLoadingPhase();

    mOrderReadyEvent.setValue(mWorkingOrder);
  }

  /**
   * Order object is ready to be used by ViewModel.
   *
   * Called by {@link #onDataLoaded(CompleteOrder)}
   */
  private void readyOrder(@NonNull CompleteOrder order) {
    mWorkingOrder = order;

    readyOrder();
  }

  private void beginLoadingPhase() {
    mLoadingData = true;
    mDataLoadingEvent.setValue(true);
  }

  private void endLoadingPhase() {
    mLoadingData = false;
    mDataLoadingEvent.setValue(false);
  }

  /**
   * Is this order complete and ready to send?
   */
  boolean readyToSend() {
    return mWorkingOrder != null && !(isStoreNameEmpty()
        || isOrderQuantityZero() || !doesOrderQuantityMeetMinimumRequirements());
  }

  /**
   * Helper method for dialogs that show before send order dialog.
   */
  void initializeSendPhase() {
    Log.w(TAG, "initializeSendPhase: " + mSendingOrder);
    mSendingOrder = true;
  }

  /**
   * Save Order to database, start preparations for email intent.
   */
  void beginSendPhase(Activity context) {
    Log.w(TAG, "beginSendPhase: ");

    //  Update order date to now.
    updateOrderDate();

    //  Save order to database.
    saveOrder();

    //  Execute prepare send task.
    executeFinalPreparations(context);
  }

  /**
   * Ends order process, essentially a helper method for future dialogs that don't need to
   * immediately show send order dialog.
   */
  void endSendPhase() {
    Log.w(TAG, "endSendPhase: ");

    mSendingOrder = false;
  }

  private void updateUI() {
    mUpdateUIEvent.setValue(mWorkingOrder);
  }

  /**
   * Begin task of generating excel, and releasing an intent to send email with.
   */
  private void executeFinalPreparations(Activity context) {
    PrepareSendActionIntentAsyncTask task = new PrepareSendActionIntentAsyncTask(context,
        mWorkingOrder,
        this);
    task.execute();
  }

  @Override
  public void onIntentReadyForAction(Intent intent) {
    mIntentReadyEvent.setValue(intent);
  }

  @Override
  public void onDataInserted() {
    //  do nothing, this is a callback of a background task
  }

  @Override
  public void onDataLoaded(CompleteOrder order) {
    readyOrder(order);
  }

  @Override
  public void onDataNotAvailable() {
    Log.e(TAG, "onDataNotAvailable: " + mWorkingOrder.getOrder().toString());

    endLoadingPhase();

    mErrorLoadingDataEvent.call();
  }
}