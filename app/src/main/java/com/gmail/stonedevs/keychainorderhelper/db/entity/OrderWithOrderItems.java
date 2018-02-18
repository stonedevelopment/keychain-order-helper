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

package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class OrderWithOrderItems {

  @Embedded
  private final Order mOrder;

  @Relation(parentColumn = "id",
      entityColumn = "order_id")
  private final List<OrderItem> mOrderItems;

  public OrderWithOrderItems(Order mOrder, List<OrderItem> mOrderItems) {
    this.mOrder = mOrder;
    this.mOrderItems = mOrderItems;
  }

  public Order getOrder() {
    return mOrder;
  }

  public List<OrderItem> getOrderItems() {
    return mOrderItems;
  }
}
