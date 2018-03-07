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

import static com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailActivity.REQUEST_CODE_ACTION_SEND;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareIntentCallback;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareSendActionIntentAsyncTask;

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

  //  Commands directed by User via on-screen interactions.
  private final SingleLiveEvent<Void> mSendOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mEditOrderCommand = new SingleLiveEvent<>();

  private final Repository mRepository;

  private String mOrderId;

  //  Object that contains the Order and its OrderItems.
  private CompleteOrder mCompleteOrder;

  //  Are we getting data from database?
  private boolean mLoadingData;

  //  Are we in the sending order process?
  private boolean mSendingOrder;

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

  SingleLiveEvent<Void> getSendOrderCommand() {
    return mSendOrderCommand;
  }

  SingleLiveEvent<Void> getEditOrderCommand() {
    return mEditOrderCommand;
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

  /**
   * Helper method for dialogs that show before send order dialog.
   */
  void initializeSendPhase() {
    mSendingOrder = true;
  }

  /**
   * Save Order to database, start preparations for email intent.
   */
  void beginSendPhase(Activity context) {
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
    mSendingOrder = false;
  }

  private void updateUI() {
    mUpdateUIEvent.setValue(mCompleteOrder);
  }

  private void executeFinalPreparations(Activity context) {
    PrepareSendActionIntentAsyncTask task = new PrepareSendActionIntentAsyncTask(context,
        mCompleteOrder,
        this);
    task.execute();
  }

  void handleActivityResult(int requestCode, int resultCode) {
    switch (requestCode) {
      case REQUEST_CODE_ACTION_SEND:
        mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_ok);
        break;
    }
  }

  @Override
  public void onIntentReadyForAction(Intent intent) {
    mIntentReadyEvent.setValue(intent);
  }

  @Override
  public void onDataLoaded(CompleteOrder order) {
    mCompleteOrder = order;

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
    updateUI();
  }
}