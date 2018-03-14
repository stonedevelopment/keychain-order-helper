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

package com.gmail.stonedevs.keychainorderhelper.model.json;

import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.ArrayList;

/**
 * @see Order
 * @deprecated Used by initial version to store orders in a json file.
 */
@Deprecated
public class JSONOrder {

  @NonNull
  private final String storeName;

  @NonNull
  private final String orderDate;

  private final ArrayList<Integer> orderQuantities;

  @NonNull
  private final Integer orderTotal;

  public JSONOrder(@NonNull String storeName, @NonNull String orderDate,
      ArrayList<Integer> orderQuantities, @NonNull Integer orderTotal) {
    this.storeName = storeName;
    this.orderDate = orderDate;
    if (orderQuantities != null) {
      this.orderQuantities = orderQuantities;
    } else {
      this.orderQuantities = new ArrayList<>(0);
    }
    this.orderTotal = orderTotal;
  }

  @NonNull
  public String getStoreName() {
    return storeName;
  }

  @NonNull
  public String getOrderDate() {
    return orderDate;
  }

  @NonNull
  public Integer getOrderTotal() {
    return orderTotal;
  }

  @NonNull
  public ArrayList<Integer> getOrderQuantities() {
    return orderQuantities;
  }
}
