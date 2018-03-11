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
import com.gmail.stonedevs.keychainorderhelper.db.dao.OrderDao;
import com.gmail.stonedevs.keychainorderhelper.db.dao.OrderItemDao;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.List;

/**
 * Concrete implementation of a data source as a db.
 */

public class LocalDataSource implements DataSource {

  private static final String TAG = LocalDataSource.class.getSimpleName();

  private static volatile LocalDataSource sInstance;

  private OrderDao mOrderDao;
  private OrderItemDao mOrderItemDao;

  private AppExecutors mAppExecutors;

  private LocalDataSource(@NonNull AppExecutors appExecutors, @NonNull OrderDao orderDao,
      @NonNull OrderItemDao orderItemDao) {
    mAppExecutors = appExecutors;
    mOrderDao = orderDao;
    mOrderItemDao = orderItemDao;
  }

  public static synchronized LocalDataSource getInstance(AppExecutors appExecutors,
      OrderDao orderDao, @NonNull OrderItemDao orderItemDao) {
    if (sInstance == null) {
      sInstance = new LocalDataSource(appExecutors, orderDao, orderItemDao);
    }

    return sInstance;
  }

  /**
   * Note: {@link LoadAllCallback#onDataNotAvailable()} is fired if the database doesn't exist
   * or the table is empty.
   */
  @Override
  public void getAllOrders(@NonNull final LoadAllCallback callback) {
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
   * Note: {@link LoadCallback#onDataNotAvailable()} is fired if {@link Order} isn't found.
   */
  @Override
  public void getOrder(@NonNull final String orderId, @NonNull final LoadCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        final Order order = mOrderDao.get(orderId);
        final List<OrderItem> orderItems = mOrderItemDao.get(orderId);

        final CompleteOrder completeOrder = new CompleteOrder(order, orderItems);

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            if (order == null) {
              callback.onDataNotAvailable();
            } else {
              callback.onDataLoaded(completeOrder);
            }
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void saveOrder(@NonNull final CompleteOrder completeOrder,
      @NonNull final InsertCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        mOrderDao.insert(completeOrder.getOrder());
        mOrderItemDao.insert(completeOrder.getOrderItems());

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            callback.onDataInserted();
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void saveOrders(@NonNull final List<CompleteOrder> completeOrders,
      @NonNull final InsertCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        for (CompleteOrder completeOrder : completeOrders) {
          mOrderDao.insert(completeOrder.getOrder());
          mOrderItemDao.insert(completeOrder.getOrderItems());
        }

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            callback.onDataInserted();
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void deleteOrder(@NonNull final Order order, @NonNull final DeleteCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        final int rowsDeleted = mOrderDao.delete(order);

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            callback.onDataDeleted(rowsDeleted);
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void deleteOrders(@NonNull final List<Order> orders,
      @NonNull final DeleteCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        final int rowsDeleted = mOrderDao.delete(orders);

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            callback.onDataDeleted(rowsDeleted);
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  @Override
  public void deleteAllOrders(@NonNull final DeleteCallback callback) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        final int rowsDeleted = mOrderDao.deleteAll();

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            callback.onDataDeleted(rowsDeleted);
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }
}
