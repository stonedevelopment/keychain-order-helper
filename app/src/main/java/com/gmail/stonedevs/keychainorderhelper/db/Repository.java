package com.gmail.stonedevs.keychainorderhelper.db;

import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import java.util.List;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class Repository implements DataSource {

  private static Repository sInstance;

  private final DataSource mLocalDataSource;

  /**
   * Used to prevent direct instantiation, see {@link #getInstance(DataSource)}.
   */
  private Repository(DataSource localDataSource) {
    mLocalDataSource = localDataSource;
  }

  /**
   * Returns the single instance of this class, creating it if necessary.
   *
   * @param localDataSource the device storage data source
   * @return the {@link Repository} instance
   */
  public static synchronized Repository getInstance(DataSource localDataSource) {
    if (sInstance == null) {
      sInstance = new Repository(localDataSource);
    }

    return sInstance;
  }

  /**
   * Used to force {@link #getInstance(DataSource)} to create a new instance next it's called.
   */
  public static void destroyInstance() {
    sInstance = null;
  }

  @Override
  public void getOrder(@NonNull String orderId, @NonNull LoadCallback callback) {
    mLocalDataSource.getOrder(orderId, callback);
  }

  @Override
  public void getAllOrders(@NonNull LoadAllCallback callback) {
    mLocalDataSource.getAllOrders(callback);
  }

  @Override
  public void saveOrder(@NonNull CompleteOrder order, @NonNull InsertCallback callback) {
    mLocalDataSource.saveOrder(order, callback);
  }

  @Override
  public void saveOrders(@NonNull List<CompleteOrder> orders, @NonNull InsertCallback callback) {
    mLocalDataSource.saveOrders(orders, callback);
  }

  @Override
  public void deleteOrder(@NonNull Order order, @NonNull DeleteCallback callback) {
    mLocalDataSource.deleteOrder(order, callback);
  }

  @Override
  public void deleteOrders(@NonNull List<Order> orders, @NonNull DeleteCallback callback) {
    mLocalDataSource.deleteOrders(orders, callback);
  }

  @Override
  public void deleteAllOrders(@NonNull DeleteCallback callback) {
    mLocalDataSource.deleteAllOrders(callback);
  }
}