/*
 * Copyright (c) 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.model;

import android.text.TextUtils;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import java.util.Date;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class CompleteOrder {

  private final Order mOrder;

  private final List<OrderItem> mOrderItems;

  public CompleteOrder(Order order, List<OrderItem> orderItems) {
    this.mOrder = order;
    mOrderItems = orderItems;
  }

  public Order getOrder() {
    return mOrder;
  }

  public String getOrderId() {
    return mOrder.getId();
  }

  public String getStoreName() {
    return mOrder.getStoreName();
  }

  public void setStoreName(String storeName) {
    mOrder.setStoreName(storeName);
  }

  public Date getOrderDate() {
    return mOrder.getOrderDate();
  }

  public List<OrderItem> getOrderItems() {
    return mOrderItems;
  }

  public boolean hasOrderTerritory() {
    return !TextUtils.isEmpty(getOrderTerritory());
  }

  public String getOrderTerritory() {
    return mOrder.getOrderTerritory();
  }

  public void setOrderTerritory(String orderTerritory) {
    mOrder.setOrderTerritory(orderTerritory);
  }

  public int getOrderQuantity() {
    return mOrder.getOrderQuantity();
  }

  public void updateOrderQuantityBy(int change) {
    int quantity = getOrderQuantity();

    quantity += change;

    if (quantity < 0) {
      quantity = 0;
    }

    setOrderQuantity(quantity);
  }

  private void setOrderQuantity(int quantity) {
    mOrder.setOrderQuantity(quantity);
  }
}
