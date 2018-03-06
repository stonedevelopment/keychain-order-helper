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
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
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

  public static final String BUNDLE_KEY_ORDER_ID = "order_id";
  public static final String BUNDLE_KEY_STORE_NAME = "store_name";
  public static final String BUNDLE_KEY_ORDER_DATE = "order_date";
  public static final String BUNDLE_KEY_ORDER_ITEMS = "order_items";
  public static final String BUNDLE_KEY_ORDER_QUANTITY = "order_quantity";
  public static final String BUNDLE_KEY_ORDER_TERRITORY = "order_territory";

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
  private CompleteOrder mWorkingOrder;

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

      if (mWorkingOrder == null) {
        //  load data from repository
        //  display data once loaded
        Log.w(TAG, "start: completeOrder is null, loading data");
        restoreOrder();
        loadOrder(mOrderId);
      } else {
        //  display loaded data
        Log.w(TAG, "start: completeOrder is not null, returning cached data");
        Log.w(TAG, "start: compare: " + mWorkingOrder.getOrderId() + " = " + mOrderId);
        onOrderReady(mWorkingOrder);
      }
    }
  }

  void stop() {
    persistOrder();
  }

  boolean isNewOrder() {
    return mIsNewOrder;
  }

  String getOrderId() {
    return mWorkingOrder.getOrderId();
  }

  void setOrderId(String orderId) {
    mOrderId = orderId;
  }

  void setIsNewOrder(boolean isNewOrder) {
    mIsNewOrder = isNewOrder;
  }

  void updateStoreName(String storeName) {
    if (mWorkingOrder != null) {
      mWorkingOrder.setStoreName(storeName);
    }
  }

  boolean hasTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    String prefsTerritory = prefs
        .getString(getApplication().getString(R.string.pref_key_rep_territory), null);

    return mWorkingOrder.hasOrderTerritory() || !TextUtils.isEmpty(prefsTerritory);
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

  void updateOrderQuantityBy(int change) {
    mWorkingOrder.updateOrderQuantityBy(change);
    mUpdateUIEvent.setValue(mWorkingOrder);
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
    mRepository.saveOrder(mWorkingOrder, this);
  }

  private void deleteOrder() {
    mRepository.deleteOrder(mWorkingOrder.getOrder(), this);
  }

  /**
   * USE CASE:
   * If User is in middle of ordering and gets interrupted, save Order to SharedPreferences to
   * persist data over time.
   */
  private void persistOrder() {
    try {
      Log.d(TAG, "persistOrder: " + mWorkingOrder.getOrder().toString());

      Order order = mWorkingOrder.getOrder();
      String orderId = order.getId();
      String storeName = order.getStoreName();
      Date orderDate = order.getOrderDate();
      String orderTerritory = order.getOrderTerritory();
      Integer orderQuantity = order.getOrderQuantity();

      ObjectMapper mapper = new ObjectMapper();
      List<OrderItem> orderItemList = mWorkingOrder.getOrderItems();
      String orderItemJson = mapper.writeValueAsString(orderItemList);

      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
      Editor editor = prefs.edit();
      editor.putString(BUNDLE_KEY_ORDER_ID, orderId);
      editor.putString(BUNDLE_KEY_STORE_NAME, storeName);
      editor.putLong(BUNDLE_KEY_ORDER_DATE, orderDate.getTime());
      editor.putString(BUNDLE_KEY_ORDER_TERRITORY, orderTerritory);
      editor.putInt(BUNDLE_KEY_ORDER_QUANTITY, orderQuantity);
      editor.putString(BUNDLE_KEY_ORDER_ITEMS, orderItemJson);
      editor.apply();
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  private void restoreOrder() {
    try {
      Log.d(TAG, "restoreOrder: ");

      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
      String orderId = prefs.getString(BUNDLE_KEY_ORDER_ID, null);

      String storeName = prefs.getString(BUNDLE_KEY_STORE_NAME, null);
      Date orderDate = new Date(prefs.getLong(BUNDLE_KEY_ORDER_DATE,
          Calendar.getInstance().getTimeInMillis()));
      String orderTerritory = prefs.getString(BUNDLE_KEY_ORDER_TERRITORY, null);
      Integer orderQuantity = prefs.getInt(BUNDLE_KEY_ORDER_QUANTITY, 0);

      ObjectMapper mapper = new ObjectMapper();
      String orderItemJson = prefs.getString(BUNDLE_KEY_ORDER_ITEMS, null);
      TypeReference<List<OrderItem>> mapType = new TypeReference<List<OrderItem>>() {
      };
      List<OrderItem> orderItemList = mapper.readValue(orderItemJson, mapType);

      Order order = new Order(orderId, storeName, orderDate, orderTerritory, orderQuantity);
      readyOrder(new CompleteOrder(order, orderItemList));
    } catch (IOException e) {
      e.printStackTrace();
    }
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
    mWorkingOrder = order;
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
        mWorkingOrder,
        this);
    task.execute();
  }

  boolean readyToSend() {
    return mWorkingOrder != null && !(isStoreNameEmpty()
        || isOrderQuantityZero() || !doesOrderQuantityMeetMinimumRequirements());
  }

  boolean isStoreNameEmpty() {
    return mWorkingOrder.getStoreName().isEmpty();
  }

  boolean isOrderQuantityZero() {
    return mWorkingOrder.getOrderQuantity() == 0;
  }

  boolean doesOrderQuantityMeetMinimumRequirements() {
    int orderQuantityMinimumRequirement = getApplication().getResources()
        .getInteger(R.integer.order_quantity_minimum_requirement);

    return mWorkingOrder.getOrderQuantity() >= orderQuantityMinimumRequirement;
  }

  @Override
  public void onOrderSaved() {
    if (mSendOrderAfterSave) {
      mSendOrderAfterSave = false;
      mPrepareIntentCommand.call();
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