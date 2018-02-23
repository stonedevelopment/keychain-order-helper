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

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.util.DateUtil;
import java.util.Date;
import java.util.UUID;

@Entity
public class Order {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private final String mId;

  @NonNull
  @ColumnInfo(name = "store_name")
  private String mStoreName;

  @NonNull
  @ColumnInfo(name = "order_date")
  private final Date mOrderDate;

  @NonNull
  @ColumnInfo(name = "order_quantity")
  private Integer mOrderQuantity;

  @Ignore
  public Order(String storeName, Date orderDate) {
    this(UUID.randomUUID().toString(), storeName, orderDate, 0);
  }

  public Order(@NonNull String id, @NonNull String storeName, @NonNull Date orderDate,
      @NonNull Integer orderQuantity) {
    mId = id;
    mStoreName = storeName;
    mOrderDate = orderDate;
    mOrderQuantity = orderQuantity;
  }

  @NonNull
  public String getId() {
    return mId;
  }

  @NonNull
  public String getStoreName() {
    return mStoreName;
  }

  public void setStoreName(@NonNull String storeName) {
    mStoreName = storeName;
  }

  @NonNull
  public Date getOrderDate() {
    return mOrderDate;
  }

  @NonNull
  public Integer getOrderQuantity() {
    return mOrderQuantity;
  }

  public void setOrderQuantity(@NonNull Integer quantity) {
    mOrderQuantity = quantity;
  }

  @Override
  public String toString() {
    return "id:" + getId() + ", store_name:" + getStoreName() + ", order_date:" + getOrderDate()
        + "(" + DateUtil.getFormattedDateForLayout(getOrderDate()) + ")";
  }
}