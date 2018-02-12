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

  interface LoadAllCallback {

    void onDataLoaded(List<Order> orders);

    void onDataNotAvailable();
  }

  interface LoadOneCallback {

    void onDataLoaded(Order order);

    void onDataNotAvailable();
  }

  void getAll(@NonNull LoadAllCallback callback);

  void get(@NonNull String orderId, @NonNull LoadOneCallback callback);

  void save(@NonNull Order order);

  void save(@NonNull List<Order> orders);

  void refreshData();

  void delete(@NonNull String orderId);

  void deleteAll();
}