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

package com.gmail.stonedevs.keychainorderhelper.ui.orderdetail;

import android.app.Application;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.ui.ViewModel;

/**
 * ViewModel for the Order Detail screen.
 */
public class OrderDetailViewModel extends ViewModel {

  private static final String TAG = OrderDetailViewModel.class.getSimpleName();

  public OrderDetailViewModel(
      @NonNull Application application, @NonNull Repository repository) {
    super(application, repository);
  }

  public void start(String orderId) {
    if (isLoadingData()) {
      //  Loading data, ignore.
      return;
    }

    if (orderNotNull()) {
      readyOrder();
    } else {
      beginLoadingPhase();
      setOrderId(orderId);
      loadOrder();
    }
  }
}