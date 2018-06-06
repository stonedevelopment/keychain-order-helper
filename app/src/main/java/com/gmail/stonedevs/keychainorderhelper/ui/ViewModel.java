/*
 * Copyright 2018, Jared Shane Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.ui;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder.OrderType;
import com.gmail.stonedevs.keychainorderhelper.util.EmailUtils;
import com.gmail.stonedevs.keychainorderhelper.util.excel.GenerateExcelFileCallback;
import com.gmail.stonedevs.keychainorderhelper.util.excel.GenerateExcelFileTask;

/**
 * ViewModel model for any screen that wishes to view, edit, or send an order.
 */
public abstract class ViewModel extends AndroidViewModel implements LoadCallback,
    GenerateExcelFileCallback, InsertCallback {

  private static final String TAG = ViewModel.class.getSimpleName();

  //  Events
  private final SingleLiveEvent<Intent> mIntentReadyEvent = new SingleLiveEvent<>();

  //  Events: UI
  private final SingleLiveEvent<CompleteOrder> mUpdateUIEvent = new SingleLiveEvent<>();

  //  Events: Data
  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<CompleteOrder> mDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mErrorLoadingDataEvent = new SingleLiveEvent<>();

  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  private final Repository mRepository;

  private String mOrderId;

  //  Object that contains the Order, and its details, and the list of OrderItems.
  private CompleteOrder mOrder;

  //  Are we getting data from database?
  private boolean mLoadingData;

  //  Are we in the sending order process?
  private boolean mSendingOrder;

  //  Are we in the sending order acknowledgement process?
  private boolean mSendingAcknowledgement;

  public ViewModel(
      @NonNull Application application, @NonNull Repository repository) {
    super(application);
    mRepository = repository;
  }

  public String getOrderId() {
    return mOrderId;
  }

  protected void setOrderId(String orderId) {
    mOrderId = orderId;
  }

  protected CompleteOrder getOrder() {
    return mOrder;
  }

  protected boolean orderNotNull() {
    return mOrder != null;
  }

  public String getStoreName() {
    return getOrder().getStoreName();
  }

  /**
   * Updates the current order's store name. If order object is null, nullify view model's order id
   * variable as this is most likely being called by the layout after garbage collection. Meaning,
   * let's just reset and start a new order when {@link #start(String)} is called.
   */
  public void updateStoreName(String storeName) {
    if (mOrder != null) {
      mOrder.setStoreName(storeName);
    } else {
      mOrderId = null;
    }
  }

  /**
   * Updates the current order's order date. Used directly before we make the attempt to send an
   * order. Keeps the date fresh for each attempt.
   */
  private void updateOrderDate() {
    mOrder.updateOrderDate();
  }

  private void updateOrderType(OrderType orderType) {
    mOrder.setOrderType(orderType);
  }

  protected boolean isLoadingData() {
    return mLoadingData;
  }

  public SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  public SingleLiveEvent<Intent> getIntentReadyEvent() {
    return mIntentReadyEvent;
  }

  public SingleLiveEvent<CompleteOrder> getUpdateUIEvent() {
    return mUpdateUIEvent;
  }

  public SingleLiveEvent<Boolean> getDataLoadingEvent() {
    return mDataLoadingEvent;
  }

  public SingleLiveEvent<CompleteOrder> getDataLoadedEvent() {
    return mDataLoadedEvent;
  }

  public SingleLiveEvent<Void> getErrorLoadingDataEvent() {
    return mErrorLoadingDataEvent;
  }

  /**
   * Readies order for observers to use.
   */
  protected void readyOrder() {
    updateUI();

    endLoadingPhase();

    mDataLoadedEvent.setValue(mOrder);
  }

  /**
   * Order object is ready to be used by ViewModel.
   *
   * Called by {@link #onDataLoaded(CompleteOrder)}
   */
  protected void readyOrder(@NonNull CompleteOrder order) {
    mOrder = order;

    readyOrder();
  }

  protected void beginLoadingPhase() {
    mLoadingData = true;
    mDataLoadingEvent.setValue(true);
  }

  private void endLoadingPhase() {
    mLoadingData = false;
    mDataLoadingEvent.setValue(false);
  }

  protected void loadOrder() {
    mRepository.getOrder(mOrderId, this);
  }

  private void saveOrder() {
    mRepository.saveOrder(mOrder, this);
  }

  /**
   * Are we currently in the process of sending an order?
   */
  public boolean isSendingOrder() {
    return mSendingOrder;
  }

  /**
   * Helper method for dialogs that show before send order dialog.
   */
  public void initializeSendOrderPhase() {
    mSendingOrder = true;
  }

  /**
   * Save Order to database, start preparations for email intent.
   */
  public void beginSendOrderPhase(Activity context) {
    //  Update order type
    updateOrderType(OrderType.ORDER);

    //  Update order date to now.
    updateOrderDate();

    //  Save order to database.
    saveOrder();

    //  Execute prepare send task.
    generateExcelFile(context);
  }

  /**
   * Ends order process, essentially a helper method for future dialogs that don't need to
   * immediately show send order dialog.
   */
  public void endSendOrderPhase() {
    mSendingOrder = false;
  }

  /**
   * Are we currently in the process of sending an order?
   */
  public boolean isSendingAcknowledgement() {
    return mSendingAcknowledgement;
  }

  /**
   * Helper method for dialogs that show before send order acknowledgement dialog.
   */
  public void initializeSendAcknowledgementPhase() {
    mSendingAcknowledgement = true;
  }

  /**
   * Start preparations for email intent.
   *
   * Being that this is an acknowledgement, we will be sending an email with contents of this order
   * to notify when last order was made.
   */
  public void beginSendAcknowledgementPhase(OrderType orderType) {
    //  Update order type
    updateOrderType(orderType);

    //  Execute prepare send task.
    sendAcknowledgementByEmail();
  }

  /**
   * Ends order process, essentially a helper method for future dialogs that don't need to
   * immediately show send order acknowledgement dialog.
   */
  public void endSendAcknowledgementPhase() {
    mSendingAcknowledgement = false;
  }

  public void updateUI() {
    mUpdateUIEvent.setValue(mOrder);
  }

  /**
   * Begin task of generating excel
   */
  private void generateExcelFile(Activity context) {
    GenerateExcelFileTask task = new GenerateExcelFileTask(context, mOrder, this);
    task.execute();
  }

  private void sendOrderByEmail(Uri uri) {
    Intent intent = EmailUtils
        .createSendOrderEmailIntent(getApplication().getApplicationContext(), mOrder, uri);

    mIntentReadyEvent.setValue(intent);
  }

  private void sendAcknowledgementByEmail() {
    Intent intent = EmailUtils
        .createSendAcknowledgementEmailIntent(getApplication().getApplicationContext(),
            mOrder);

    mIntentReadyEvent.setValue(intent);
  }

  @Override
  public void onFileGenerationSuccess(Uri uri) {
    sendOrderByEmail(uri);
  }

  @Override
  public void onFileGenerationFail() {
    // TODO: 4/2/2018 Tell Firebase about fail.
    mSnackBarMessenger.setValue(R.string.snackbar_message_generate_file_failed);
  }

  @Override
  public void onDataLoaded(CompleteOrder order) {
    readyOrder(order);
  }

  @Override
  public void onDataNotAvailable() {
    Log.e(TAG, "onDataNotAvailable: " + mOrder.getOrder().toString());

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