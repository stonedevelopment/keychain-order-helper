package com.gmail.stonedevs.keychainorderhelper.db.entity;

import static android.arch.persistence.room.ForeignKey.CASCADE;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import java.util.UUID;

/**
 * Defines an item tied to its {@link Order}.
 */

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

  @Ignore
  public OrderItem(String orderId, String name, Integer quantity) {
    this(UUID.randomUUID().toString(), name, quantity, orderId);
  }

  public OrderItem(
      @NonNull String id, @NonNull String name,
      @NonNull Integer quantity, @NonNull String orderId) {
    this.mId = id;
    this.mName = name;
    this.mQuantity = quantity;
    this.mOrderId = orderId;
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
    return "id:" + getId() + ", orderId:" + getOrderId() + ", name: " + getName() + ", quantity:"
        + getQuantity();
  }
}
