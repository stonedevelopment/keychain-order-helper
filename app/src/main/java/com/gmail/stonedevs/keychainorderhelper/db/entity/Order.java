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

package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.gmail.stonedevs.keychainorderhelper.db.AppDatabase;
import com.gmail.stonedevs.keychainorderhelper.util.DateUtil;
import java.util.Date;
import java.util.UUID;

@Entity(tableName = "orders")
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
  private Date mOrderDate;

  @ColumnInfo(name = "order_quantity")
  private int mOrderQuantity;

  @Nullable
  @ColumnInfo(name = "order_territory")
  private String mOrderTerritory;

  @ColumnInfo(name = "order_category")
  private int mOrderCategory;

  /**
   * todo update documentation
   *
   * @param storeName Name of the store the order is being made for
   * @param orderDate Date the order is being made on
   */
  @Ignore
  public Order(String storeName, Date orderDate) {
    this(storeName, orderDate, 0);
  }

  /**
   * Default constructor used with creating new Orders. Creates a random UUID for id, nullifies
   * orderTerritory, and sets the orderQuantity to 0.
   *
   * @param storeName Name of the store the order is being made for
   * @param orderDate Date the order is being made on
   * @param orderCategory Category of order being made (ex: keychains or taffy)
   */
  @Ignore
  public Order(String storeName, Date orderDate, int orderCategory) {
    this(UUID.randomUUID().toString(), storeName, orderDate, null, 0, orderCategory);
  }

  /**
   * Full constructor used by {@link AppDatabase} to make a POJO of Order.
   */
  public Order(@NonNull String id, @NonNull String storeName, @NonNull Date orderDate,
      @Nullable String orderTerritory, int orderQuantity, int orderCategory) {
    mId = id;
    mStoreName = storeName;
    mOrderDate = orderDate;
    mOrderTerritory = orderTerritory;
    mOrderQuantity = orderQuantity;
    mOrderCategory = orderCategory;
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

  public void setOrderDate(@NonNull Date orderDate) {
    mOrderDate = orderDate;
  }

  public int getOrderQuantity() {
    return mOrderQuantity;
  }

  public void setOrderQuantity(@NonNull Integer quantity) {
    mOrderQuantity = quantity;
  }

  @Nullable
  public String getOrderTerritory() {
    return mOrderTerritory;
  }

  public void setOrderTerritory(@Nullable String orderTerritory) {
    mOrderTerritory = orderTerritory;
  }

  public int getOrderCategory() {
    return mOrderCategory;
  }

  public void setOrderCategory(@NonNull Integer orderCategory) {
    mOrderCategory = orderCategory;
  }

  @Override
  public String toString() {
    return "id:" + getId()
        + ", store_name:" + getStoreName()
        + ", order_date:" + getOrderDate()
        + "(" + DateUtil.getFormattedDateForLayout(getOrderDate()) + ")"
        + ", order_territory:" + getOrderTerritory()
        + ", order_category:" + getOrderCategory();
  }

}