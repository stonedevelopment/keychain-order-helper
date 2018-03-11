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

package com.gmail.stonedevs.keychainorderhelper.model.json;

import java.util.ArrayList;

/**
 * TODO: Add a class header comment!
 */

public class JSONOrderEntry {

  private String storeName;
  private String orderDate;
  private ArrayList<Integer> orderQuantities;
  private Integer orderTotal;

  public JSONOrderEntry() {
  }

  public JSONOrderEntry(JSONOrder order) {
    this(order.getStoreName(), order.getOrderDate(), order.getOrderQuantities(),
        order.getOrderTotal());
  }

  public JSONOrderEntry(String storeName, String orderDate, ArrayList<Integer> orderQuantities,
      Integer orderTotal) {
    this.storeName = storeName;
    this.orderQuantities = orderQuantities;
    this.orderDate = orderDate;
    this.orderTotal = orderTotal;
  }

  public String getStoreName() {
    return storeName;
  }

  public ArrayList<Integer> getOrderQuantities() {
    return orderQuantities;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public Integer getOrderTotal() {
    return orderTotal;
  }
}
