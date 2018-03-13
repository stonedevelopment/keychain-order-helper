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

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder.OrderType;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareIntentCallback;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareSendActionIntentAsyncTask;
import java.util.Date;

/**
 * Listens to user actions from item list in {@link OrderDetailFragment} and redirects them to the
 * fragment's action listener.
 */
public class OrderDetailViewModel extends AndroidViewModel implements OrderDetailCallback,
    LoadCallback, PrepareIntentCallback, InsertCallback {

  private static final String TAG = OrderDetailViewModel.class.getSimpleName();

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events
  private final SingleLiveEvent<Intent> mIntentReadyEvent = new SingleLiveEvent<>();

  //  Events: UI Changes
  private final SingleLiveEvent<CompleteOrder> mUpdateUIEvent = new SingleLiveEvent<>();

  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<CompleteOrder> mDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mErrorLoadingDataEvent = new SingleLiveEvent<>();

  private final Repository mRepository;

  private String mOrderId;

  //  Object that contains the Order and its OrderItems.
  private CompleteOrder mCompleteOrder;

  //  Are we getting data from database?
  private boolean mLoadingData;

  //  Are we in the sending order process?
  private boolean mSendingOrder;

  //  Are we in the sending order acknowledgement process?
  private boolean mSendingAcknowledgement;

  public OrderDetailViewModel(
      @NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
  }

  public void start(String orderId) {
    if (mLoadingData) {
      //  Loading data, ignore.
      return;
    }

    mOrderId = orderId;

    beginLoadingPhase();
    loadOrder();
  }

  String getOrderId() {
    return mCompleteOrder.getOrderId();
  }

  private void updateOrderDate() {
    mCompleteOrder.updateOrderDate();
  }

  private void updateOrderType(OrderType orderType) {
    mCompleteOrder.setOrderType(orderType);
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
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

  private void beginLoadingPhase() {
    mLoadingData = true;
    mDataLoadingEvent.setValue(true);
  }

  private void endLoadingPhase() {
    mLoadingData = false;
    mDataLoadingEvent.setValue(false);
  }

  private void loadOrder() {
    mRepository.getOrder(mOrderId, this);
  }

  private void saveOrder() {
    mRepository.saveOrder(mCompleteOrder, this);
  }

  private void saveAcknowledgement() {
    String storeName = mCompleteOrder.getStoreName();
    Date orderDate = mCompleteOrder.getOrderDate();
    OrderType orderType = mCompleteOrder.getOrderType();

    CompleteOrder order = new CompleteOrder(new Order(storeName, orderDate), null, orderType);

    mRepository.saveOrder(order, this);
  }

  /**
   * Are we currently in the process of sending an order?
   */
  boolean isSendingOrder() {
    return mSendingOrder;
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
   * Helper method for dialogs that show before send order acknowledgement dialog.
   */
  void initializeSendAcknowledgementPhase() {
    mSendingAcknowledgement = true;
  }

  /**
   * Start preparations for email intent.
   *
   * Being that this is an acknowledgement, we will be sending an email with contents of this order
   * to notify when last order was made.
   */
  void beginSendAcknowledgementPhase(Activity context) {
    //  Update order type
    updateOrderType(OrderType.ACKNOWLEDGEMENT_WITH_ORDER);

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

  private void updateUI() {
    mUpdateUIEvent.setValue(mCompleteOrder);
  }

  private void executeFinalPreparations(Activity context) {
    PrepareSendActionIntentAsyncTask task = new PrepareSendActionIntentAsyncTask(context,
        mCompleteOrder, this);
    task.execute();
  }

  @Override
  public void onIntentReadyForAction(Intent intent) {
    mIntentReadyEvent.setValue(intent);
  }

  @Override
  public void onDataLoaded(CompleteOrder order) {
    mCompleteOrder = order;
    mCompleteOrder.setOrderType(OrderType.ORDER);

    updateUI();

    mDataLoadedEvent.setValue(order);

    endLoadingPhase();
  }

  @Override
  public void onDataNotAvailable() {
    Log.e(TAG, "onDataNotAvailable: " + mCompleteOrder.getOrder().toString());

    endLoadingPhase();

    mErrorLoadingDataEvent.call();
  }

  @Override
  public void onDataInserted() {
    if (!mSendingOrder || !mSendingAcknowledgement) {
      updateUI();
    }
  }
}