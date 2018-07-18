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

package com.gmail.stonedevs.keychainorderhelper.ui;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.SingleLiveEvent;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource.InsertCallback;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;
import com.gmail.stonedevs.keychainorderhelper.util.JSONUtils;
import com.gmail.stonedevs.keychainorderhelper.util.PrefUtils;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ViewModel for the MainActivity.
 */

public class MainActivityViewModel extends AndroidViewModel implements InsertCallback {

  private static final String TAG = MainActivityViewModel.class.getSimpleName();

  //  Commands directed by System logic
  private final SingleLiveEvent<Void> mOpenInitialSettingsDialogCommand = new SingleLiveEvent<>();

  //  Commands directed by User via on-screen buttons.
  private final SingleLiveEvent<Void> mOrderListCommand = new SingleLiveEvent<>();

  private Repository mRepository;

  //  ViewModel variables
  private String mRepName;
  private String mRepTerritory;
  private String mCompanyDivision;

  public MainActivityViewModel(@NonNull Application application, @NonNull Repository repository) {
    super(application);
    mRepository = repository;
  }

  String getRepName() {
    return mRepName;
  }

  String getRepTerritory() {
    return mRepTerritory;
  }

  String getCompanyDivision() {
    return mCompanyDivision;
  }

  private boolean isReady() {
    return isRepNameReady() && isCompanyDivisionReady();
  }

  private boolean isRepNameReady() {
    return !TextUtils.isEmpty(mRepName);
  }

  private boolean isCompanyDivisionReady() {
    return !TextUtils.isEmpty(mCompanyDivision);
  }

  SingleLiveEvent<Void> getOpenInitialSettingsDialogCommand() {
    return mOpenInitialSettingsDialogCommand;
  }

  SingleLiveEvent<Void> getOrderListCommand() {
    return mOrderListCommand;
  }

  /**
   * Called when the activity is ready.
   */
  void start() {
    setupDefaultValues();
    performUpdateCleanUp();
    validateRequiredFields();
  }

  /**
   * Grab saved values from preferences.
   */
  private void setupDefaultValues() {
    Context c = getApplication().getApplicationContext();

    mRepName = PrefUtils.getRepName(c);
    mRepTerritory = PrefUtils.getRepTerritory(c);
    mCompanyDivision = PrefUtils.getCompanyDivision(c);
  }

  /**
   * Perform the steps required to keep newest version up to date and clean.
   */
  private void performUpdateCleanUp() {
    updateFrom4to5();
  }

  /**
   * Perform update responsibility check from version 4 (0.0.4) to version 5 (0.1.0)
   *
   * Removes an old preference key and fills its value into new key.
   *
   * Imports old orders.json file into database.
   */
  private void updateFrom4to5() {
    updateFrom4to5_prefs();
    updateFrom4to5_json();
  }

  private void updateFrom4to5_prefs() {
    Context context = getApplication();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

    String oldKey = context.getString(R.string.pref_key_rep_territory_old);
    if (prefs.contains(oldKey)) {
      String newKey = context.getString(R.string.pref_key_rep_territory);
      String oldValue = prefs.getString(oldKey, null);

      Editor editor = prefs.edit();
      editor.putString(newKey, oldValue);
      editor.remove(oldKey);
      editor.apply();
    }
  }

  private void updateFrom4to5_json() {
    //  Import orders.json into database.
    List<JSONOrder> jsonOrders = JSONUtils.getJSONOrders(getApplication());
    if (jsonOrders == null) {
      return;
    }

    List<CompleteOrder> completeOrders = new ArrayList<>(0);
    for (JSONOrder jsonOrder : jsonOrders) {
      try {
        String storeName = jsonOrder.getStoreName();
        SimpleDateFormat orderDateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Date orderDate = orderDateFormat.parse(jsonOrder.getOrderDate());
        int orderTotal = jsonOrder.getOrderTotal();

        Order order = new Order(storeName, orderDate);
        order.setOrderQuantity(orderTotal);

        //  import order quantities and convert into order items
        List<Integer> orderQuantities = jsonOrder.getOrderQuantities();
        String[] names = getApplication().getResources()
            .getStringArray(R.array.excel_cell_values_keychains);

        if (orderQuantities.size() != names.length) {
          //  if the lists don't match, move on to next
          continue;
        }

        List<OrderItem> orderItems = new ArrayList<>(0);
        for (int i = 0; i < names.length; i++) {
          String name = names[i];
          int quantity = orderQuantities.get(i);
          orderItems.add(new OrderItem(name, quantity, order.getId()));
        }

        completeOrders.add(new CompleteOrder(order, orderItems));
      } catch (ParseException e) {
        //  do nothing, continue to next one.
      }
    }

    //  insert list into repository
    mRepository.saveOrders(completeOrders, this);

    //  Remove orders.json
    boolean fileDeleted = JSONUtils.removeOrderJSONFile(getApplication());
    if (!fileDeleted) {
      // TODO: 3/11/2018 Update firebase that file was not removed
      Log.e(TAG, "updateFrom4to5: file was not removed.");
    }
  }

  /**
   * Update from version 8 (0.1.3) to 9 (0.1.4)
   */
  private void updateFrom8to9() {

  }

  private void updateFrom8to9_prefs() {
    Context context = getApplication();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);


  }

  /**
   * If the Required Fields are not empty, open {@link OrderListActivity},
   * Otherwise, open dialog for user to enter their name and territory.
   */
  private void validateRequiredFields() {
    if (isReady()) {
      getOrderListCommand().call();
    } else {
      getOpenInitialSettingsDialogCommand().call();
    }
  }

  @Override
  public void onDataInserted() {
    //  do nothing, result of background task
  }
}