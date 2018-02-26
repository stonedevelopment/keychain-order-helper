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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.app.Activity;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.view.View;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.DeleteCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadAllCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailActivity;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class OrderListViewModel extends AndroidViewModel implements LoadAllCallback,
    DeleteCallback {

  private static final String TAG = OrderListViewModel.class.getSimpleName();

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Events
  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<List<Order>> mDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mNoDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<ActionMode> mDataDeletedEvent = new SingleLiveEvent<>();

  //  Commands directed by User via on-screen interactions.
  private final SingleLiveEvent<View> mNewOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<String> mOrderDetailCommand = new SingleLiveEvent<>();

  //  Data repository
  private final Repository mRepository;

  private ActionMode mActionMode;

  public OrderListViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);
    mRepository = repository;
  }

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<Boolean> getDataLoadingEvent() {
    return mDataLoadingEvent;
  }

  SingleLiveEvent<List<Order>> getDataLoadedEvent() {
    return mDataLoadedEvent;
  }

  SingleLiveEvent<Void> getNoDataLoadedEvent() {
    return mNoDataLoadedEvent;
  }

  SingleLiveEvent<ActionMode> getDataDeletedEvent() {
    return mDataDeletedEvent;
  }

  SingleLiveEvent<View> getNewOrderCommand() {
    return mNewOrderCommand;
  }

  SingleLiveEvent<String> getOrderDetailCommand() {
    return mOrderDetailCommand;
  }

  public void start() {
    loadData();
  }

  void handleActivityResult(int requestCode, int resultCode) {
    switch (requestCode) {
      case NewOrderActivity.REQUEST_CODE:
        switch (resultCode) {
          case Activity.RESULT_CANCELED:
            mSnackBarMessenger.setValue(R.string.snackbar_message_cancel_order_success);
            break;
          case Activity.RESULT_OK:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_success);
            break;
          case NewOrderActivity.RESULT_SENT_ERROR_NO_APPS:
            mSnackBarMessenger
                .setValue(R.string.snackbar_message_send_order_fail_no_supported_apps);
            break;
        }
        break;

      case OrderDetailActivity.REQUEST_CODE:
        switch (resultCode) {
          case OrderDetailActivity.RESULT_DATA_LOAD_ERROR:
            //  send failed message
            mSnackBarMessenger.setValue(R.string.snackbar_message_data_loading_error);
        }
        break;
    }
  }

  private void loadData() {
    //  Let fragment know we're updating
    mDataLoadingEvent.setValue(true);
    //  Start retrieval of order data.
    mRepository.getAllOrders(this);
  }

  void startDeleteModeProcess(ActionMode mode, List<Order> orders) {
    mDataLoadingEvent.setValue(true);
    mActionMode = mode;
    mRepository.deleteOrders(orders, this);
  }

  @Override
  public void onDataNotAvailable() {
    //  Update screen components first.
    mNoDataLoadedEvent.call();
    //  Then enable it to view message.
    mDataLoadingEvent.setValue(false);
  }

  @Override
  public void onDataLoaded(List<Order> orders) {
    //  Let adapter know its safe to fill items
    mDataLoadedEvent.setValue(orders);
    //  Finally, enable layout to view items
    mDataLoadingEvent.setValue(false);
  }

  @Override
  public void onDataDeleted(int rowsDeleted) {
    mDataDeletedEvent.setValue(mActionMode);
    mRepository.getAllOrders(this);
  }
}