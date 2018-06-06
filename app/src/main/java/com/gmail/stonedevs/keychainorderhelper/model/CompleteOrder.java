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

package com.gmail.stonedevs.keychainorderhelper.model;

import android.text.TextUtils;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Container class which holds an {@link Order} object, its corresponding {@link OrderItem} objects,
 * and its type.
 */
public class CompleteOrder {

  private final Order mOrder;

  private final List<OrderItem> mOrderItems;

  private OrderType mOrderType;

  public enum OrderType {
    ORDER, ACKNOWLEDGEMENT, ACKNOWLEDGEMENT_WITH_ORDER
  }

  /**
   * Default constructor, used for orders.
   */
  public CompleteOrder(Order order, List<OrderItem> orderItems) {
    this(order, orderItems, OrderType.ORDER);
  }

  /**
   * Full constructor, can be used for orders or acknowledgements.
   */
  public CompleteOrder(Order order, List<OrderItem> orderItems, OrderType orderType) {
    this.mOrder = order;
    mOrderItems = orderItems;
    mOrderType = orderType;
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

  public void updateOrderDate() {
    mOrder.setOrderDate(Calendar.getInstance().getTime());
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

  public void setOrderQuantity(int quantity) {
    mOrder.setOrderQuantity(quantity);
  }

  public OrderType getOrderType() {
    return mOrderType;
  }

  public void setOrderType(OrderType orderType) {
    mOrderType = orderType;
  }

  public int getOrderCategory() {
    return mOrder.getOrderCategory();
  }

  public void setOrderCategory(int orderCategory) {
    mOrder.setOrderCategory(orderCategory);
  }
}
