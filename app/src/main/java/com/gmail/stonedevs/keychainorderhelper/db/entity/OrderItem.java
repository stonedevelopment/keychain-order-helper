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

import static android.arch.persistence.room.ForeignKey.CASCADE;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.AppDatabase;
import java.util.UUID;

@Entity(foreignKeys = {
    @ForeignKey(entity = Order.class,
        parentColumns = "id",
        childColumns = "order_id",
        onDelete = CASCADE)},
    indices = {
        @Index(value = {"order_id"})})
public class OrderItem {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private final String mId;

  @NonNull
  @ColumnInfo(name = "name")
  private String mName;

  @NonNull
  @ColumnInfo(name = "quantity")
  private Integer mQuantity;

  @NonNull
  @ColumnInfo(name = "order_id")
  private final String mOrderId;

  /**
   * Default constructor used with creating new Orders. Generates a new UUID to be used for its row
   * id.
   */
  @Ignore
  public OrderItem(String name, Integer quantity, String orderId) {
    this(UUID.randomUUID().toString(), name, quantity, orderId);
  }

  /**
   * Full constructor used by {@link AppDatabase} to make a POJO of OrderItem.
   */
  public OrderItem(
      @NonNull String id, @NonNull String name, @NonNull Integer quantity,
      @NonNull String orderId) {
    mId = id;
    mName = name;
    mQuantity = quantity;
    mOrderId = orderId;
  }

  @NonNull
  public String getId() {
    return mId;
  }

  @NonNull
  public String getOrderId() {
    return mOrderId;
  }

  @NonNull
  public String getName() {
    return mName;
  }

  public void setName(@NonNull String name) {
    mName = name;
  }

  @NonNull
  public Integer getQuantity() {
    return mQuantity;
  }

  public void setQuantity(@NonNull Integer quantity) {
    this.mQuantity = quantity;
  }

  @Override
  public String toString() {
    return "id:" + getId()
        + ", name: " + getName()
        + ", quantity:" + getQuantity()
        + ", orderId:" + getOrderId();
  }
}