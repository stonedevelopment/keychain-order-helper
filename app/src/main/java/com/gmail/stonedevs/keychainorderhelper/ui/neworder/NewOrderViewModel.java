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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.DeleteCallback;
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
    InsertCallback, LoadCallback, PrepareIntentCallback, DeleteCallback {

  private final static String TAG = NewOrderViewModel.class.getSimpleName();

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events: Data Loading Changes
  private final SingleLiveEvent<CompleteOrder> mOrderReadyEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Intent> mIntentReadyEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mOrderCanceledEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mSavedChangesEvent = new SingleLiveEvent<>();

  //  Events: UI Changes
  private final SingleLiveEvent<CompleteOrder> mUpdateUIEvent = new SingleLiveEvent<>();

  //  Commands: User Direction
  private final SingleLiveEvent<Void> mCancelOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mResetOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mPrepareIntentCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();

  //  Data repository
  private final Repository mRepository;

  private final AppExecutors mAppExecutors;

  //  View model's data variables
  private String mOrderId;
  private CompleteOrder mCompleteOrder;

  private boolean mIsNewOrder;
  private boolean mIsDataLoading;
  private boolean mSendOrderAfterSave;
  private boolean mFinishActivityAfterSave;

  public NewOrderViewModel(@NonNull Application application, Repository repository) {
    super(application);

    mRepository = repository;
    mAppExecutors = new AppExecutors();
  }

  void start() {
    if (mIsDataLoading) {
      //  Loading data, ignore.
      return;
    }

    if (mOrderId == null) {
      //  new order
      Log.w(TAG, "start: creating new order");

      //  create new order
      createOrder();
    } else {
      //  created order
      Log.w(TAG, "start: order id exists: " + mOrderId);

      if (mCompleteOrder == null) {
        //  load data from repository
        //  display data once loaded
        Log.w(TAG, "start: completeOrder is null, loading data");
        loadOrder(mOrderId);
      } else {
        //  display loaded data
        Log.w(TAG, "start: completeOrder is not null, returning cached data");
        Log.w(TAG, "start: compare: " + mCompleteOrder.getOrderId() + " = " + mOrderId);
        onOrderReady(mCompleteOrder);
      }
    }
  }

  boolean isNewOrder() {
    return mIsNewOrder;
  }

  String getOrderId() {
    return mCompleteOrder.getOrderId();
  }

  void setOrderId(String orderId) {
    mOrderId = orderId;
  }

  void setIsNewOrder(boolean isNewOrder) {
    mIsNewOrder = isNewOrder;
  }

  void updateStoreName(String storeName) {
    if (mCompleteOrder != null) {
      mCompleteOrder.setStoreName(storeName);
    }
  }

  boolean hasTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    String prefsTerritory = prefs
        .getString(getApplication().getString(R.string.pref_key_rep_territory), null);

    return mCompleteOrder.hasOrderTerritory() || !TextUtils.isEmpty(prefsTerritory);
  }

  /**
   * Return Territory if saved to ViewModel or saved in sharedPreferences.
   */
  String getTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    return mCompleteOrder.hasOrderTerritory() ? mCompleteOrder.getOrderTerritory() : prefs
        .getString(getApplication().getString(R.string.pref_key_rep_territory), null);
  }

  void setTerritory(String territory) {
    mCompleteOrder.setOrderTerritory(territory);
  }

  int getOrderQuantity() {
    return mCompleteOrder.getOrderQuantity();
  }

  void updateOrderQuantityBy(int change) {
    mCompleteOrder.updateOrderQuantityBy(change);
    mUpdateUIEvent.setValue(mCompleteOrder);
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<CompleteOrder> getOrderReadyEvent() {
    return mOrderReadyEvent;
  }

  SingleLiveEvent<Void> getOrderCanceledEvent() {
    return mOrderCanceledEvent;
  }

  SingleLiveEvent<Void> getSavedChangesEvent() {
    return mSavedChangesEvent;
  }

  SingleLiveEvent<Intent> getIntentReadyEvent() {
    return mIntentReadyEvent;
  }

  SingleLiveEvent<CompleteOrder> getUpdateUIEvent() {
    return mUpdateUIEvent;
  }

  SingleLiveEvent<Void> getCancelOrderCommand() {
    return mCancelOrderCommand;
  }

  SingleLiveEvent<Void> getResetOrderCommand() {
    return mResetOrderCommand;
  }

  SingleLiveEvent<Void> getPrepareIntentCommand() {
    return mPrepareIntentCommand;
  }

  SingleLiveEvent<Void> getSendOrderCommand() {
    return mSendOrderCommand;
  }

  private void createOrder() {
    mIsDataLoading = true;

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
            onOrderReady(new CompleteOrder(order, orderItems));
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  private void loadOrder(String orderId) {
    mIsDataLoading = true;

    mRepository.getOrder(orderId, this);
  }

  void resetOrder() {
    createOrder();
  }

  private void saveOrder(boolean sendOrderAfterSave, boolean finishActivity) {
    mSendOrderAfterSave = sendOrderAfterSave;
    mFinishActivityAfterSave = finishActivity;
    mRepository.saveOrder(mCompleteOrder, this);
  }

  private void deleteOrder() {
    mRepository.deleteOrder(mCompleteOrder.getOrder(), this);
  }

  /**
   * Save order to persist data being edited by User, but phone state might have changed. Does not
   * send order after this save.
   *
   * Called by {@link NewOrderActivity#onRestoreInstanceState(Bundle)}
   */
  void persistOrder() {
    saveOrder(false, false);
  }

  /**
   * User chose to save changes on exit. Does not send order after this save.
   */
  void saveChanges() {
    saveOrder(false, true);
  }

  /**
   * User chose to cancel order. Delete saved Order if was persisted.
   */
  void cancelOrder() {
    deleteOrder();
  }

  /**
   * Order object is ready to be used by ViewModel.
   *
   * Called by {@link #onDataLoaded(CompleteOrder)} and {@link #onOrderReady(CompleteOrder)}
   */
  private void readyOrder(CompleteOrder order) {
    mCompleteOrder = order;
    mOrderReadyEvent.setValue(order);

    mIsDataLoading = false;
  }

  /**
   * Save Order with intentions of sending email after.
   *
   * Called by {@link NewOrderActivity#showConfirmSendOrderDialog()}
   */
  void prepareToSendOrder() {
    saveOrder(true, false);
  }

  void executeFinalPreparations(Activity context) {
    PrepareSendActionIntentAsyncTask task = new PrepareSendActionIntentAsyncTask(context,
        mCompleteOrder,
        this);
    task.execute();
  }

  boolean readyToSend() {
    return mCompleteOrder != null && !(isStoreNameEmpty()
        || isOrderQuantityZero() || !doesOrderQuantityMeetMinimumRequirements());
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
  public void onOrderReady(CompleteOrder order) {
    readyOrder(order);
  }

  @Override
  public void onOrderSaved() {
    if (mSendOrderAfterSave) {
      mSendOrderAfterSave = false;
      mPrepareIntentCommand.call();
    } else {
      if (mFinishActivityAfterSave) {
        mFinishActivityAfterSave = false;
        mSavedChangesEvent.call();
      }
    }
  }

  @Override
  public void onOrderCanceled() {
    mOrderCanceledEvent.call();
  }

  @Override
  public void onIntentReadyForAction(Intent intent) {
    mIntentReadyEvent.setValue(intent);
  }

  /**
   * Callback used after data was saved to database successfully. If mSendOrderAfterSave is true
   * then command calling Activity to prepare Intent to send.
   *
   * @see InsertCallback#onDataInserted()
   */
  @Override
  public void onDataInserted() {
    onOrderSaved();
  }

  @Override
  public void onDataNotAvailable() {
    //  no order was found with orderId
    //  create new order with saved contents?
//    saveOrder(false, false);
  }

  @Override
  public void onDataLoaded(CompleteOrder order) {
    onOrderReady(order);
  }

  @Override
  public void onDataDeleted(int rowsDeleted) {
    onOrderCanceled();

    if (rowsDeleted > 0) {
      Log.d(TAG, "onDataDeleted: persistent order was deleted");
    } else {
      Log.d(TAG, "onDataDeleted: no order was saved, so no deletion.");
    }
  }
}