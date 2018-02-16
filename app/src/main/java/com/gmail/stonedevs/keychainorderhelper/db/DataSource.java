package com.gmail.stonedevs.keychainorderhelper.db;

import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.entity.CompleteOrder;
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

    void onDataLoaded(CompleteOrder order);
  }

  interface LoadAllOrdersCallback extends DataNotAvailableCallback {

    void onDataLoaded(List<CompleteOrder> orders);
  }

  void getAllOrders(@NonNull LoadAllOrdersCallback callback);

  void getOrder(@NonNull String orderId, @NonNull LoadOrderCallback callback);

  void saveOrder(@NonNull CompleteOrder order);

  void saveOrders(@NonNull List<CompleteOrder> orders);

  void refreshData();

  void deleteOrder(@NonNull CompleteOrder order);

  void deleteAllOrders();
}