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

import android.os.Bundle;
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
import java.util.List;

/**
 * TODO: Add a class header comment!
 */
public class TabItemViewModel implements LoadAllCallback,
    DeleteCallback {

  private final TabbedActivityViewModel mViewModel;

  //  Are we getting data from database?
  private boolean mLoadingData;

  //  The tab number this vm belongs to
  private int mTabNumber;

  TabItemViewModel(TabbedActivityViewModel viewModel, int tabNumber) {
    mViewModel = viewModel;
    mTabNumber = tabNumber;
  }

  //  Events
  private final SingleLiveEvent<Boolean> mDataLoadingEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<List<Order>> mDataLoadedEvent = new SingleLiveEvent<>();
  private final SingleLiveEvent<Void> mNoDataLoadedEvent = new SingleLiveEvent<>();

  //  Commands directed by User via on-screen interactions.
  private final SingleLiveEvent<Void> mNewOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<String> mOrderDetailCommand = new SingleLiveEvent<>();

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

  private Repository getRepository() {
    return mViewModel.getRepository();
  }

  SnackBarMessage getSnackBarMessenger() {
    return mViewModel.getSnackBarMessenger();
  }

  /**
   * Starts the view model's initializations.
   *
   * Called by {@link OrderListFragment#onActivityCreated(Bundle)}
   */
  public void start() {
    if (mLoadingData) {
      //  Loading data, ignore.
      return;
    }

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
    getRepository().getAllOrders(mTabNumber, this);
  }

  /**
   * Persist ActionMode, because we're relying on the response of the repository to let us know when
   * data was deleted. Otherwise, ActionMode will complete and refresh data before the callback was
   * called.
   *
   * Sidenote:  We could possibly use this to allow for configuration changes to continue in this
   * manner instead of resetting the ui.
   */
  void deleteOrders(List<CompleteOrder> orders) {
    beginLoadingPhase();

    getRepository().deleteOrders(orders, this);
  }

  void commandNewOrder() {
    mViewModel.commandNewOrder();
  }

  void commandOrderDetails(String orderId) {
    mViewModel.commandOrderDetails(orderId);
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
      getSnackBarMessenger().setValue(resourceId);
    } else {
      getSnackBarMessenger().setValue(R.string.snackbar_message_data_deleted_success_zero);
    }

    loadData();
  }

  @Override
  public void onDataNotDeleted() {
    getSnackBarMessenger().setValue(R.string.snackbar_message_data_deleted_fail);
  }
}
