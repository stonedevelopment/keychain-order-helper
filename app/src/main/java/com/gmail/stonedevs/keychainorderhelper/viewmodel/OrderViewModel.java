package com.gmail.stonedevs.keychainorderhelper.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;


/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class OrderViewModel extends AndroidViewModel {

  private LiveData<Order> mObservableOrderEntity;
  public ObservableField<Order> boundOrderEntity = new ObservableField<>();

  public OrderViewModel(@NonNull Application application) {
    super(application);
  }

  public LiveData<Order> getObservableOrderEntity() {
    return mObservableOrderEntity;
  }

  public void createObservableOrderEntity(String orderId) {
    Repository repository = Repository.getInstance(this.getApplication());
    mObservableOrderEntity = repository.loadOrder(orderId);
  }

  public void setBoundOrderEntity(Order order) {
    this.boundOrderEntity.set(order);
  }
}
