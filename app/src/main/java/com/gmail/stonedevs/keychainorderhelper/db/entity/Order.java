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
import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Entity(tableName = "orders")
public class Order {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private final String mId;

  @Nullable
  @ColumnInfo(name = "name")
  private final String mStoreName;

  @Nullable
  @ColumnInfo(name = "date")
  private final Date mOrderDate;

  @Ignore
  public Order(String storeName, Date orderDate) {
    this(UUID.randomUUID().toString(), storeName, orderDate);
  }

  public Order(@NonNull String id, @Nullable String storeName, @Nullable Date orderDate) {
    this.mId = id;
    this.mStoreName = storeName;
    this.mOrderDate = orderDate;
  }

  @NonNull
  public String getId() {
    return mId;
  }

  @Nullable
  public String getStoreName() {
    return mStoreName;
  }

  @Nullable
  public Date getOrderDate() {
    return mOrderDate;
  }

  public boolean isComplete() {
    return !TextUtils.isEmpty(mStoreName) && mOrderDate != null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Order order = (Order) obj;
    return Objects.equals(getId(), order.getId()) &&
        Objects.equals(getStoreName(), order.getStoreName()) &&
        Objects.equals(getOrderDate(), order.getOrderDate());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getStoreName(), getOrderDate());
  }

  @Override
  public String toString() {
    return "Order for Store Name: " + getStoreName();
  }
}