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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.DataNotAvailableCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.DeleteCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadAllCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailActivity;
import java.util.List;

/**
 * ViewModel for the Order List screen.
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

  //  Commands directed by User via on-screen interactions.
  private final SingleLiveEvent<Void> mNewOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<String> mOrderDetailCommand = new SingleLiveEvent<>();

  //  Data repository
  private final Repository mRepository;

  //  Are we getting data from database?
  private boolean mLoadingData;

  //  Order List Category
  private int mOrderCategory;

  public OrderListViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);
    mRepository = repository;
    mOrderCategory = 0;
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

  SingleLiveEvent<Void> getNewOrderCommand() {
    return mNewOrderCommand;
  }

  SingleLiveEvent<String> getOrderDetailCommand() {
    return mOrderDetailCommand;
  }

  int getOrderCategory() {
    return mOrderCategory;
  }

  /**
   * Starts the view model's initializations with previously saved order category.
   *
   * Called by {@link OrderListFragment#onActivityCreated(Bundle)}
   */
  public void start() {
    start(mOrderCategory);
  }

  /**
   * Starts the view model's initializations.
   *
   * Called by {@link OrderListFragment#onActivityCreated(Bundle)}
   */
  public void start(int orderCategory) {
    if (mLoadingData && mOrderCategory == orderCategory) {
      //  Loading data, ignore.
      return;
    }

    //  Update order category
    mOrderCategory = orderCategory;

    beginLoadingPhase();
    loadData();
  }

  /**
   * Tell fragment to enable progress bar, we're about to load some data.
   */
  private void beginLoadingPhase() {
    mLoadingData = true;
    mDataLoadingEvent.setValue(true);
  }

  /**
   * Tell fragment to disable progress bar, it's ok to display the ui again.
   */
  private void endLoadingPhase() {
    mLoadingData = false;
    mDataLoadingEvent.setValue(false);
  }

  /**
   * Query repository for all orders in database.
   */
  private void loadData() {
    mRepository.getAllOrders(mOrderCategory, this);
  }

  /**
   * Persist ActionMode, because we're relying on the response of the repository to let us know when
   * data was deleted. Otherwise, ActionMode will complete and refresh data before the callback was
   * called.
   *
   * Sidenote:  We could possibly use this to allow for configuration changes to continue in this
   * manner instead of resetting the ui.
   */
  void deleteOrders(List<Order> orders) {
    beginLoadingPhase();

    mRepository.deleteOrders(orders, this);
  }

  /**
   * Handle the logic of an Activity's resultCode. Any UI manipulation should be at the Activity
   * level.
   */
  void handleActivityResult(int requestCode, int resultCode) {
    switch (requestCode) {
      case NewOrderActivity.REQUEST_CODE:
        switch (resultCode) {
          case NewOrderActivity.RESULT_SENT_ORDER_OK:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_ok);
            break;
          case NewOrderActivity.RESULT_SENT_ACKNOWLEDGEMENT_OK:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_acknowledgement_ok);
            break;
          case NewOrderActivity.RESULT_SENT_CANCEL:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_cancel);
            break;
          case NewOrderActivity.RESULT_SENT_ERROR_NO_APPS:
            mSnackBarMessenger
                .setValue(R.string.snackbar_message_send_order_error_no_supported_apps);
            break;
          case NewOrderActivity.RESULT_DATA_LOAD_ERROR:
            //  send failed message
            mSnackBarMessenger.setValue(R.string.snackbar_message_data_loading_error);
            break;
        }
        break;

      case OrderDetailActivity.REQUEST_CODE:
        switch (resultCode) {
          case OrderDetailActivity.RESULT_SENT_ORDER_OK:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_ok);
            break;
          case OrderDetailActivity.RESULT_SENT_ACKNOWLEDGEMENT_OK:
            mSnackBarMessenger.setValue(R.string.snackbar_message_send_order_acknowledgement_ok);
            break;
          case OrderDetailActivity.RESULT_SENT_CANCEL:
            mSnackBarMessenger.setValue(R.string.snackbar_message_save_order_cancel);
            break;
          case OrderDetailActivity.RESULT_SENT_ERROR_NO_APPS:
            mSnackBarMessenger
                .setValue(R.string.snackbar_message_send_order_error_no_supported_apps);
            break;
          case OrderDetailActivity.RESULT_DATA_LOAD_ERROR:
            //  send failed message
            mSnackBarMessenger.setValue(R.string.snackbar_message_data_loading_error);
            break;
        }
        break;
    }
  }

  /**
   * @see DataNotAvailableCallback#onDataNotAvailable()
   */
  @Override
  public void onDataNotAvailable() {
    //  Update screen components first.
    mNoDataLoadedEvent.call();
    endLoadingPhase();
  }

  /**
   * @see DataSource.LoadCallback#onDataLoaded(CompleteOrder)
   */
  @Override
  public void onDataLoaded(List<Order> orders) {
    //  Let adapter know its safe to fill items
    mDataLoadedEvent.setValue(orders);
    endLoadingPhase();
  }

  /**
   * @see DataSource.DeleteCallback#onDataDeleted(int)
   */
  @Override
  public void onDataDeleted(int rowsDeleted) {
    if (rowsDeleted > 0) {
      int resourceId = rowsDeleted > 1 ? R.string.snackbar_message_data_deleted_success_multiple
          : R.string.snackbar_message_data_deleted_success_one;
      mSnackBarMessenger.setValue(resourceId);
    } else {
      mSnackBarMessenger.setValue(R.string.snackbar_message_data_deleted_success_zero);
    }

    loadData();
  }
}