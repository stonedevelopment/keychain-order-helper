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
import android.util.SparseArray;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailActivity;

/**
 * ViewModel for the Order List screen.
 *
 * todo refactor to TabbedActivityViewModel
 *
 * will effectively tell activity which tab to show
 *
 * Treat this like a parent ViewModel for children ViewModels
 */
public class TabbedActivityViewModel extends AndroidViewModel {

  private static final String TAG = TabbedActivityViewModel.class.getSimpleName();

  //  SnackBar
  private final SnackBarMessage mSnackBarMessenger = new SnackBarMessage();

  //  Data repository
  private final Repository mRepository;

  //  Tab that's currently being shown?
  private int mCurrentTab;

  private SparseArray<TabItemViewModel> mViewModels;

  public TabbedActivityViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);
    mRepository = repository;
    mCurrentTab = 0;
    mViewModels = new SparseArray<>(0);
  }

  //  Commands directed by User via on-screen interactions.
  private final SingleLiveEvent<Void> mNewOrderCommand = new SingleLiveEvent<>();
  private final SingleLiveEvent<String> mOrderDetailCommand = new SingleLiveEvent<>();

  SnackBarMessage getSnackBarMessenger() {
    return mSnackBarMessenger;
  }

  SingleLiveEvent<Void> getNewOrderCommand() {
    return mNewOrderCommand;
  }

  SingleLiveEvent<String> getOrderDetailCommand() {
    return mOrderDetailCommand;
  }

  Repository getRepository() {
    return mRepository;
  }

  int getCurrentTab() {
    return mCurrentTab;
  }

  /**
   * Forces a child view model to update its data for views.
   *
   * Called by {@link OrderListFragment#onActivityCreated(Bundle)}
   */
  public void updateCurrentTab() {
    mViewModels.get(mCurrentTab).start();
  }

  /**
   * Starts the view model's initializations.
   *
   * Called by {@link OrderListFragment#onActivityCreated(Bundle)}
   */
  public void start(int tabNumber) {
    if (mCurrentTab == tabNumber) {
      //  Same tab, ignore.
      return;
    }

    //  Update order category
    mCurrentTab = tabNumber;

    //  Start up tab item view model
    mViewModels.get(tabNumber).start();
  }

  TabItemViewModel obtainViewModel(int tabNumber) {
    TabItemViewModel viewModel = mViewModels.get(tabNumber);
    if (viewModel != null) {
      return viewModel;
    }

    viewModel = new TabItemViewModel(this, tabNumber);
    mViewModels.put(tabNumber, viewModel);

    return viewModel;
  }

  void commandNewOrder() {
    mNewOrderCommand.call();
  }

  void commandOrderDetails(String orderId) {
    mOrderDetailCommand.setValue(orderId);
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

}