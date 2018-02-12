package com.gmail.stonedevs.keychainorderhelper.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.List;


/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class OrderListViewModel extends AndroidViewModel {

  private LiveData<List<Order>> mOrderList;

  public OrderListViewModel(@NonNull Application application) {
    super(application);

    Repository repository = Repository.getInstance(application);
    mOrderList = repository.loadOrders();
  }

  public LiveData<List<Order>> getOrderList() {
    return mOrderList;
  }
}
