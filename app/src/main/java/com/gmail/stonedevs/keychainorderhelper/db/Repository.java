package com.gmail.stonedevs.keychainorderhelper.db;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class Repository implements DataSource {

  private static Repository sInstance;

  private final DataSource mLocalDataSource;

  private Map<String, Order> mCachedOrders;

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

  /**
   * Gets orders from cache or locale data source, whichever is available first.
   *
   * Note: {@link LoadAllCallback#onDataNotAvailable()} is fired if it fails to get data.
   */
  @Override
  public void getAll(@NonNull final LoadAllCallback callback) {

    //  respond immediately with cache if available
    if (mCachedOrders != null) {
      callback.onDataLoaded(new ArrayList<>(mCachedOrders.values()));
      return;
    }

    //  query the local storage if available.
    mLocalDataSource.getAll(new LoadAllCallback() {
      @Override
      public void onDataLoaded(List<Order> orders) {
        refreshCache(orders);

        callback.onDataLoaded(new ArrayList<>(mCachedOrders.values()));
      }

      @Override
      public void onDataNotAvailable() {
        //  for future use, get data from remote source.
        callback.onDataNotAvailable();
      }
    });
  }

  /**
   * Gets an order from local data source, unless table is new or empty.
   *
   * Note: {@link LoadAllCallback#onDataNotAvailable()} is fired if it fails to get data.
   */
  @Override
  public void get(@NonNull String orderId, @NonNull final LoadOneCallback callback) {
    final Order cachedOrder = getOrderById(orderId);

    //  respond immediately if cache is available.
    if (cachedOrder != null) {
      callback.onDataLoaded(cachedOrder);
      return;
    }

    mLocalDataSource.get(orderId, new LoadOneCallback() {
      @Override
      public void onDataLoaded(Order order) {
        //  update cache to keep the ui up to date
        if (mCachedOrders == null) {
          mCachedOrders = new LinkedHashMap<>();
        }

        mCachedOrders.put(order.getId(), order);

        callback.onDataLoaded(order);
      }

      @Override
      public void onDataNotAvailable() {
        //  for future use, get data from remote source.
        callback.onDataNotAvailable();
      }
    });
  }

  @Override
  public void save(@NonNull Order order) {
    mLocalDataSource.save(order);

    if (mCachedOrders == null) {
      mCachedOrders = new LinkedHashMap<>();
    }

    mCachedOrders.put(order.getId(), order);
  }

  @Override
  public void save(@NonNull List<Order> orders) {
    mLocalDataSource.save(orders);

    if (mCachedOrders == null) {
      mCachedOrders = new LinkedHashMap<>();
    }

    for (Order order : orders) {
      mCachedOrders.put(order.getId(), order);
    }
  }

  @Override
  public void refreshData() {
    //  for future use, to use with remote data.
  }

  @Override
  public void delete(@NonNull String orderId) {
    mLocalDataSource.delete(orderId);

    if (mCachedOrders == null) {
      mCachedOrders = new LinkedHashMap<>();
    } else {
      mCachedOrders.remove(orderId);
    }
  }

  @Override
  public void deleteAll() {
    mLocalDataSource.deleteAll();

    if (mCachedOrders == null) {
      mCachedOrders = new LinkedHashMap<>();
    }

    mCachedOrders.clear();
  }

  private void refreshCache(List<Order> orders) {
    if (mCachedOrders == null) {
      mCachedOrders = new LinkedHashMap<>();
    }

    mCachedOrders.clear();

    for (Order order : orders) {
      mCachedOrders.put(order.getId(), order);
    }
  }

  /**
   * For future use, with remote data source.
   */
  private void refreshLocalDataSource(List<Order> orders) {
    mLocalDataSource.deleteAll();
    mLocalDataSource.save(orders);
  }

  @Nullable
  private Order getOrderById(@NonNull String orderId) {
    if (mCachedOrders == null || mCachedOrders.isEmpty()) {
      return null;
    } else {
      return mCachedOrders.get(orderId);
    }
  }
}