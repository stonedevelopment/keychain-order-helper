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
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.prepareorder.PrepareOrderAsyncTask;

/**
 * Listens to user actions from item list in {@link OrderDetailFragment} and redirects them to the
 * fragment's action listener.
 */
public class OrderDetailViewModel extends AndroidViewModel implements OrderDetailCallback,
    LoadCallback, InsertCallback {

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

  //  Data repository
  private final Repository mRepository;

  //  View model's data variables
  private CompleteOrder mCompleteOrder;

  public OrderDetailViewModel(
      @NonNull Application application, @NonNull Repository repository) {
    super(application);

    mRepository = repository;
  }

  String getStoreName() {
    return mCompleteOrder.getStoreName();
  }

  void updateStoreName(String newStoreName) {
    mCompleteOrder.setStoreName(newStoreName);
    mRepository.saveOrder(mCompleteOrder, this);
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

  public void start(@NonNull String orderId) {
    mDataLoadingEvent.setValue(true);
    mRepository.getOrder(orderId, this);
  }

  void prepareToResendOrder(Activity context) {
    executeFinalPreparations(context);
  }

  private void executeFinalPreparations(Activity context) {
    PrepareOrderAsyncTask task = new PrepareOrderAsyncTask(context, mCompleteOrder,
        this);
    task.execute();
  }

  @Override
  public void onDataLoaded(CompleteOrder order) {
    mCompleteOrder = order;

    mDataLoadedEvent.setValue(order);
    mDataLoadingEvent.setValue(false);
  }

  @Override
  public void onDataNotAvailable() {
    //  If for some reason the order didn't pull from database
    mDataLoadingEvent.setValue(false);
    mErrorLoadingDataEvent.call();
  }

  @Override
  public void onOrderReadyToSend(Intent intent) {
    mIntentReadyEvent.setValue(intent);
  }

  @Override
  public void onDataInserted() {
    mDataLoadedEvent.setValue(mCompleteOrder);
  }
}