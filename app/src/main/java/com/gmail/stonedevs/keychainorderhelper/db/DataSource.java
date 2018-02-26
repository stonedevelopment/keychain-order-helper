package com.gmail.stonedevs.keychainorderhelper.db;

import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import java.util.List;

/**
 * Created by Shane Stone on 2/11/2018.
 *
 * Email: stonedevs@gmail.com
 */

public interface DataSource {

  interface DataNotAvailableCallback {

    void onDataNotAvailable();
  }

  interface LoadCallback extends DataNotAvailableCallback {

    void onDataLoaded(CompleteOrder order);
  }

  interface LoadAllCallback extends DataNotAvailableCallback {

    void onDataLoaded(List<Order> orders);
  }

  interface InsertCallback {

    void onDataInserted();
  }

  interface DeleteCallback {

    void onDataDeleted(int rowsDeleted);
  }

  void getOrder(@NonNull String orderId, @NonNull LoadCallback callback);

  void getAllOrders(@NonNull LoadAllCallback callback);

  void saveOrder(@NonNull CompleteOrder order, @NonNull InsertCallback callback);

  void saveOrders(@NonNull List<CompleteOrder> orders, @NonNull InsertCallback callback);

  void deleteOrder(@NonNull Order order, @NonNull DeleteCallback callback);

  void deleteOrders(@NonNull List<Order> orders, @NonNull DeleteCallback callback);

  void deleteAllOrders(@NonNull DeleteCallback callback);
}