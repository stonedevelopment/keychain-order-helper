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

package com.gmail.stonedevs.keychainorderhelper.db;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import com.gmail.stonedevs.keychainorderhelper.db.dao.OrderDao;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.List;

/**
 * Concrete implementation of a data source as a db.
 */

public class LocalDataSource implements DataSource {

  private static volatile LocalDataSource sInstance;

  private OrderDao mOrderDao;

  private AppExecutors mAppExecutors;

  private LocalDataSource(@NonNull AppExecutors appExecutors, @NonNull OrderDao orderDao) {
    mAppExecutors = appExecutors;
    mOrderDao = orderDao;
  }

  public static synchronized LocalDataSource getInstance(@NonNull AppExecutors appExecutors,
      @NonNull OrderDao orderDao) {
    if (sInstance == null) {
      sInstance = new LocalDataSource(appExecutors, orderDao);
    }

    return sInstance;
  }

  /**
   * Note: {@link LoadAllOrdersCallback#onDataNotAvailable()} is fired if the database doesn't exist or
   * the table is empty.
   */
  @Override
  public void getAllOrders(@NonNull final LoadAllOrdersCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        final List<Order> orders = mOrderDao.getAll();

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            if (orders.isEmpty()) {
              callback.onDataNotAvailable();
            } else {
              callback.onDataLoaded(orders);
            }
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  /**
   * Note: {@link LoadOrderCallback#onDataNotAvailable()} is fired if {@link Order} isn't found.
   */
  @Override
  public void getOrder(@NonNull final String orderId, @NonNull final LoadOrderCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        final Order order = mOrderDao.get(orderId);

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            if (order == null) {
              callback.onDataNotAvailable();
            } else {
              callback.onDataLoaded(order);
            }
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void saveOrder(@NonNull final Order order) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        mOrderDao.insert(order);
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void saveOrders(@NonNull final List<Order> orders) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        mOrderDao.insert(orders);
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  /**
   * Not required, {@link Repository} handles the logic of refreshing the data.
   */
  @Override
  public void refreshData() {
    //  do nothing
  }

  @Override
  public void deleteOrder(@NonNull final String orderId) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        Order order = mOrderDao.get(orderId);
        mOrderDao.delete(order);
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void deleteAllOrders() {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        mOrderDao.delete();
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @VisibleForTesting
  static void clearInstance() {
    sInstance = null;
  }
}
