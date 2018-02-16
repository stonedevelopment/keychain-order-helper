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

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableList;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.SnackBarMessage;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadAllOrdersCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.ui.orderdetail.OrderDetailActivity;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class OrderListViewModel extends AndroidViewModel {

  public final ObservableList<Order> items = new ObservableArrayList<>();
  public final ObservableBoolean empty = new ObservableBoolean();
  public final ObservableBoolean dataLoading = new ObservableBoolean();

  private final SnackBarMessage mSnackBarMessage = new SnackBarMessage();

  private final Repository mRepository;

  private final SingleLiveEvent<String> mOrderDetailCommand = new SingleLiveEvent<>();

  public OrderListViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);
    mRepository = repository;
  }

  SingleLiveEvent<String> getOrderDetailCommand() {
    return mOrderDetailCommand;
  }

  SnackBarMessage getSnackBarMessage() {
    return mSnackBarMessage;
  }

  public void start() {
    loadData(false);
  }

  /**
   * For future use, with editing orders on {@link OrderDetailActivity} screen, for now: just
   * viewing details.
   */

  void handleActivityResult(int requestCode, int resultCode) {
    if (OrderDetailActivity.REQUEST_CODE == requestCode) {
      switch (resultCode) {
        case OrderDetailActivity.RESULT_SENT_OK:
          //  send success message
          mSnackBarMessage.setValue(R.string.snackbar_message_send_order_success);
          break;
        case OrderDetailActivity.RESULT_SENT_CANCEL:
          //  send failed message
          mSnackBarMessage.setValue(R.string.snackbar_message_send_order_fail);
      }
    }
  }

  private void loadData(boolean forceUpdate) {
    loadData(forceUpdate, true);
  }

  private void loadData(boolean forceUpdate, final boolean showLoadingUI) {
    if (showLoadingUI) {
      dataLoading.set(true);
    }

    if (forceUpdate) {
      mRepository.refreshData();
    }

    mRepository.getAllOrders(new LoadAllOrdersCallback() {
      @Override
      public void onDataLoaded(List<Order> orders) {
        if (showLoadingUI) {
          dataLoading.set(false);
        }

        items.clear();
        items.addAll(orders);
        empty.set(items.isEmpty());
      }

      @Override
      public void onDataNotAvailable() {
        //  sample code shows error, but let's just set to no items in list.
        if (showLoadingUI) {
          dataLoading.set(false);
        }

        items.clear();
        empty.set(true);
      }
    });
  }
}
