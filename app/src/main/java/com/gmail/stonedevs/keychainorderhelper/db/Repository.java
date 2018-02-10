package com.gmail.stonedevs.keychainorderhelper.db;

import android.os.AsyncTask;
import com.gmail.stonedevs.keychainorderhelper.db.dao.OrderDao;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderEntity;
import java.util.List;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class Repository {

  private static Repository sInstance;

  private OrderDao mOrderDao;

  private Repository(final AppDatabase db) {
    mOrderDao = db.orderDao();
  }

  public static synchronized Repository getInstance(final AppDatabase db) {
    if (sInstance == null) {
      sInstance = new Repository(db);
    }

    return sInstance;
  }

  public OrderEntity loadOrder(final String orderId) {
    return mOrderDao.get(orderId);
  }

  public List<OrderEntity> loadAllOrders() {
    return mOrderDao.getAll();
  }

  public void insert(OrderEntity orderEntity) {
    new insertTask(mOrderDao).execute(orderEntity);
  }

  private static class insertTask extends AsyncTask<OrderEntity, Void, Void> {

    private OrderDao mDao;

    insertTask(OrderDao dao) {
      mDao = dao;
    }

    @Override
    protected Void doInBackground(final OrderEntity... orderEntities) {
      mDao.insert(orderEntities);
      return null;
    }
  }
}
