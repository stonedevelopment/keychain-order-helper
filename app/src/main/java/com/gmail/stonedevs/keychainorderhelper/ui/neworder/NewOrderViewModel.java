/*
 * Copyright 2018, Jared Shane Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.ui.neworder;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.LoadCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.ViewModel;
import com.gmail.stonedevs.keychainorderhelper.util.OrderUtils;
import com.gmail.stonedevs.keychainorderhelper.util.excel.GenerateExcelFileCallback;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * ViewModel for the New Order screen.
 */
public class NewOrderViewModel extends ViewModel implements InsertCallback, LoadCallback,
    GenerateExcelFileCallback {

  private final static String TAG = NewOrderViewModel.class.getSimpleName();

  //  Events
  private final SingleLiveEvent<Integer> mUpdateItemQuantitiesEvent = new SingleLiveEvent<>();

  private final AppExecutors mAppExecutors;

  public NewOrderViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application, repository);
    mAppExecutors = new AppExecutors();
  }

  public SingleLiveEvent<Integer> getUpdateItemQuantitiesEvent() {
    return mUpdateItemQuantitiesEvent;
  }

  /**
   * Starts the view model's initializations.
   * todo update documentation
   * Called by {@link NewOrderFragment#onActivityCreated(Bundle)}
   */
  public void start(String orderId) {
    if (isLoadingData()) {
      //  Loading data, ignore.
      return;
    }

    if (orderNotNull()) {
      //  main order object exists and is ready for ui to update with its contents.
      readyOrder();
    } else {
      //  main order object is null, load from database.
      beginLoadingPhase();
      setOrderId(orderId);
      loadOrder();
    }
  }

  /**
   * todo update documentation
   */
  protected void start(int orderCategory) {
    if (isLoadingData()) {
      //  Loading data, ignore.
      return;
    }

    if (orderNotNull()) {
      //  main order object exists and is ready for ui to update with its contents.
      readyOrder();
    } else {
      //  main order object is null, create a new one or load from database.
      beginLoadingPhase();

      //  if order id wasn't set, then it's a new order, create it, otherwise load it.
      createOrder(orderCategory);
    }
  }

  @Override
  public void updateUI() {
    super.updateUI();

    mUpdateItemQuantitiesEvent.setValue(getOrder().getOrderQuantity());
  }

  /**
   * Returns if Order is perceived to be a New Order, lazily checking if order id is null.
   */
  boolean isNewOrder() {
    return getOrderId() == null;
  }

  /**
   * Is the store name saved in current order object empty?
   */
  boolean isStoreNameEmpty() {
    return TextUtils.isEmpty(getOrder().getStoreName());
  }

  /**
   * Whether or not we have a valid territory, either by ViewModel persistence or SharedPreferences.
   */
  boolean hasTerritory() {
    return hasSetTerritory() || hasPrefTerritory();
  }

  /**
   * Retrieve the territory set by User from the current order's object.
   */
  private String getSetTerritory() {
    return getOrder().getOrderTerritory();
  }

  /**
   * Does our current order object have a territory set?
   */
  private boolean hasSetTerritory() {
    return !TextUtils.isEmpty(getSetTerritory());
  }

  /**
   * Retrieve the territory set by User's initial settings.
   */
  private String getPrefTerritory() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
    return prefs.getString(getApplication().getString(R.string.pref_key_rep_territory), null);
  }

  /**
   * Do we have a territory saved in preferences?
   */
  private boolean hasPrefTerritory() {
    return !TextUtils.isEmpty(getPrefTerritory());
  }

  /**
   * Get the assigned territory, either set by current order object or in preferences. The set
   * territory takes precedence since that is what User set directly.
   */
  String getTerritory() {
    return getOrder().hasOrderTerritory() ? getSetTerritory() : getPrefTerritory();
  }

  /**
   * Update current order's territory with User input.
   */
  private void setTerritory(String territory) {
    getOrder().setOrderTerritory(territory);
  }

  /**
   * Updates the current order's territory, if it doesn't match the territory saved in preferences.
   */
  boolean updateTerritory(String territory) {
    if (!Objects.equals(territory, getPrefTerritory())) {
      if (!Objects.equals(territory, getSetTerritory())) {
        setTerritory(territory);
        return true;
      }
    } else {
      if (hasSetTerritory()) {
        setTerritory(null);
        return true;
      }
    }

    return false;
  }

  int getOrderQuantity() {
    return getOrder().getOrderQuantity();
  }

  boolean isOrderQuantityZero() {
    return getOrder().getOrderQuantity() == 0;
  }

  boolean doesOrderQuantityMeetMinimumRequirements() {
    int orderQuantityMinimumRequirement = OrderUtils
        .getOrderQuantityMinimum(getApplication(), getOrderCategory());

    return getOrder().getOrderQuantity() >= orderQuantityMinimumRequirement;
  }

  private void resetOrderQuantity() {
    getOrder().setOrderQuantity(0);
  }

  void setOrderQuantity(int quantity) {
    getOrder().setOrderQuantity(quantity);
  }

  void updateOrderQuantityBy(int change) {
    getOrder().updateOrderQuantityBy(change);
  }

  int getOrderCategory() {
    return getOrder().getOrderCategory();
  }

  private void createOrder(final int orderCategory) {
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        String storeName = "";
        Date orderDate = Calendar.getInstance().getTime();

        final Order order = new Order(storeName, orderDate, orderCategory);

        String orderId = order.getId();

        String[] names = getNames(orderCategory);

        final List<OrderItem> orderItems = new ArrayList<>(0);
        for (String name : names) {
          orderItems.add(new OrderItem(name, 0, orderId));
        }

        mAppExecutors.mainThread().execute(new Runnable() {
          @Override
          public void run() {
            readyOrder(new CompleteOrder(order, orderItems));
          }
        });
      }
    };

    mAppExecutors.diskIO().execute(runnable);
  }

  private String[] getNames(int orderCategory) {
    Resources resources = getApplication().getResources();

    switch (orderCategory) {
      case 0:
        return resources.getStringArray(R.array.excel_cell_values_keychains);
      case 1:
        return resources.getStringArray(R.array.excel_cell_values_taffy);
      default:
        return new String[]{};
    }
  }

  /**
   * Is this order complete and ready to send?
   */
  boolean readyToSendOrder() {
    return orderNotNull() && !(isStoreNameEmpty()
        || isOrderQuantityZero() || !doesOrderQuantityMeetMinimumRequirements());
  }

  boolean readyToSendAcknowledgement() {
    return !isStoreNameEmpty();
  }
}