package com.gmail.stonedevs.keychainorderhelper.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class NewOrderViewModel extends AndroidViewModel {

  private MutableLiveData<String> mStoreName = new MutableLiveData<>();
  private MutableLiveData<Date> mOrderDate = new MutableLiveData<>();
  private MutableLiveData<List<Integer>> mOrderQuantities = new MutableLiveData<>();

  public NewOrderViewModel(@NonNull Application application) {
    super(application);

    String defaultStoreName =
        BuildConfig.DEBUG ? application.getString(R.string.editStoreName_debug_default_value) : "";
    Date defaultOrderDate = Calendar.getInstance().getTime();

    setStoreName(defaultStoreName);
    setOrderDate(defaultOrderDate);
    setOrderQuantities(new ArrayList<Integer>(0));
  }

  public MutableLiveData<String> getStoreName() {
    return mStoreName;
  }

  public void setStoreName(String storeName) {
    getStoreName().setValue(storeName);
  }

  public MutableLiveData<Date> getOrderDate() {
    return mOrderDate;
  }

  public void setOrderDate(Date orderDate) {
    getOrderDate().setValue(orderDate);
  }

  public MutableLiveData<List<Integer>> getOrderQuantities() {
    return mOrderQuantities;
  }

  public void setOrderQuantities(List<Integer> orderQuantities) {
    getOrderQuantities().setValue(orderQuantities);
  }

  public void insert(Order order) {
    Repository repository = Repository.getInstance(this.getApplication());
    repository.insertOrder(order);
  }
}
