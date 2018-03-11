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
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder.OrderType;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareIntentCallback;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareSendActionIntentAsyncTask;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * ViewModel for the New Order screen.
 */

public class NewOrderViewModel extends AndroidViewModel implements NewOrderCallback,
    InsertCallback, LoadCallback, PrepareIntentCallback {

  private final static String TAG = NewOrderViewModel.class.getSimpleName();

  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events
  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mErrorLoadingDataEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<CompleteOrder> mOrderReadyEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<CompleteOrder> mUpdateUIEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Intent> mIntentReadyEvent = new SingleLiveEvent<>();

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

  //  Are we in the sending acknowledgement process?
  private boolean mSendingAcknowledgement;

  public NewOrderViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
    mAppExecutors = new AppExecutors();
  }

  /**
   * Starts the view model's initializations.
   *
   * Called by {@link NewOrderFragment#onActivityCreated(Bundle)}
   */
  void start() {
    if (mLoadingData || mSendingOrder || mSendingAcknowledgement) {
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

  /**
   * Sets the order id in view model, lazily used to let view model know that we want to load an
   * order and not create one.
   */
  void setOrderId(String orderId) {
    mOrderId = orderId;
  }

  /**
   * Returns if Order is perceived to be a New Order, lazily checking if order id is null.
   */
  boolean isNewOrder() {
    return mOrderId == null;
  }

  /**
   * Is the store name saved in current order object empty?
   */
  boolean isStoreNameEmpty() {
    return TextUtils.isEmpty(mWorkingOrder.getStoreName());
  }

  String getStoreName() {
    return mWorkingOrder.getStoreName();
  }

  /**
   * Updates the current order's store name. If order object is null, nullify view model's order id
   * variable as this is most likely being called by the layout after garbage collection. Meaning,
   * let's just reset and start a new order when {@link #start()} is called.
   */
  void updateStoreName(String storeName) {
    if (mWorkingOrder != null) {
      mWorkingOrder.setStoreName(storeName);
    } else {
      mOrderId = null;
    }
  }

  /**
   * Updates the current order's order date. Used directly before we make the attempt to send an
   * order. Keeps the date fresh for each attempt.
   */
  private void updateOrderDate() {
    mWorkingOrder.updateOrderDate();
  }

  private void updateOrderType(OrderType orderType) {
    mWorkingOrder.setOrderType(orderType);
  }

  /**
   * Whether or not we have a valid territory, either by ViewModel persistence or SharedPreferences.
   */
  boolean hasTerritory() {
    return hasSetTerritory() || hasPrefTerritory();
  }

  /**
   * Retrieve the territory set by User from the current order's object.
   */
  private String getSetTerritory() {
    return mWorkingOrder.getOrderTerritory();
  }

  /**
   * Does our current order object have a territory set?
   */
  private boolean hasSetTerritory() {
    return !TextUtils.isEmpty(getSetTerritory());
  }

  /**
   * Retrieve the territory set by User's initial settings.
   */
  private String getPrefTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    return prefs.getString(getApplication().getString(R.string.pref_key_rep_territory), null);
  }

  /**
   * Do we have a territory saved in preferences?
   */
  private boolean hasPrefTerritory() {
    return !TextUtils.isEmpty(getPrefTerritory());
  }

  /**
   * Get the assigned territory, either set by current order object or in preferences. The set
   * territory takes precedence since that is what User set directly.
   */
  String getTerritory() {
    return mWorkingOrder.hasOrderTerritory() ? getSetTerritory() : getPrefTerritory();
  }

  /**
   * Update current order's territory with User input.
   */
  private void setTerritory(String territory) {
    mWorkingOrder.setOrderTerritory(territory);
  }

  /**
   * Updates the current order's territory, if it doesn't match the territory saved in preferences.
   */
  boolean updateTerritory(String territory) {
    if (!Objects.equals(territory, getPrefTerritory())) {
      if (!Objects.equals(territory, getSetTerritory())) {
        setTerritory(territory);
        return true;
      }
    } else {
      if (hasSetTerritory()) {
        setTerritory(null);
        return true;
      }
    }

    return false;
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

  private void resetOrderQuantity() {
    mWorkingOrder.setOrderQuantity(0);
  }

  void updateOrderQuantityBy(int change) {
    mWorkingOrder.updateOrderQuantityBy(change);
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

  SingleLiveEvent<Void> getErrorLoadingDataEvent() {
    return mErrorLoadingDataEvent;
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
   * Are we currently in the process of sending an order?
   */
  boolean isSendingOrder() {
    return mSendingOrder;
  }

  /**
   * Is this order complete and ready to send?
   */
  boolean readyToSendOrder() {
    return mWorkingOrder != null && !(isStoreNameEmpty()
        || isOrderQuantityZero() || !doesOrderQuantityMeetMinimumRequirements());
  }

  /**
   * Helper method for dialogs that show before send order dialog.
   */
  void initializeSendOrderPhase() {
    mSendingOrder = true;
  }

  /**
   * Save Order to database, start preparations for email intent.
   */
  void beginSendOrderPhase(Activity context) {
    //  Update order type
    updateOrderType(OrderType.ORDER);

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
  void endSendOrderPhase() {
    mSendingOrder = false;
  }

  /**
   * Are we currently in the process of sending an order?
   */
  boolean isSendingAcknowledgement() {
    return mSendingAcknowledgement;
  }

  /**
   * Are we ready to send an order acknowledgement?
   */
  boolean readyToSendAcknowledgment() {
    return !isStoreNameEmpty();
  }

  /**
   * Helper method for dialogs that show before send order acknowledgement dialog.
   */
  void initializeSendAcknowledgementPhase() {
    mSendingAcknowledgement = true;
  }

  /**
   * Start preparations for email intent.
   */
  void beginSendAcknowledgementPhase(Activity context) {
    //  Update order type
    updateOrderType(OrderType.ACKNOWLEDGEMENT);

    //  Execute prepare send task.
    executeFinalPreparations(context);
  }

  /**
   * Ends order process, essentially a helper method for future dialogs that don't need to
   * immediately show send order acknowledgement dialog.
   */
  void endSendAcknowledgementPhase() {
    mSendingAcknowledgement = false;
  }

  /**
   * Alert observer to update its ui components with current order object's contents.
   */
  void updateUI() {
    mUpdateUIEvent.setValue(mWorkingOrder);
  }

  /**
   * Begin task of generating excel, and releasing an intent to send email with.
   */
  private void executeFinalPreparations(Activity context) {
    PrepareSendActionIntentAsyncTask task = new PrepareSendActionIntentAsyncTask(context,
        mWorkingOrder, this);
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
    // TODO: 3/10/2018 Tell firebase that order was not found in database but was attempted.
    mErrorLoadingDataEvent.call();
  }
}