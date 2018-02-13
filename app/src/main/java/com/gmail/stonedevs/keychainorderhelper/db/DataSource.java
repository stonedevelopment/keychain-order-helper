package com.gmail.stonedevs.keychainorderhelper.db;

import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
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

  interface LoadOrderCallback extends DataNotAvailableCallback {

    void onDataLoaded(Order order);
  }

  interface LoadAllOrdersCallback extends DataNotAvailableCallback {

    void onDataLoaded(List<Order> orders);
  }

  void getAllOrders(@NonNull LoadAllOrdersCallback callback);

  void getOrder(@NonNull String orderId, @NonNull LoadOrderCallback callback);

  void saveOrder(@NonNull Order order);

  void saveOrders(@NonNull List<Order> orders);

  void refreshData();

  void deleteOrder(@NonNull String orderId);

  void deleteAllOrders();
}