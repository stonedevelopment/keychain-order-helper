package com.gmail.stonedevs.keychainorderhelper.db.entity;

import static android.arch.persistence.room.ForeignKey.CASCADE;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.model.NewOrder;
import java.util.UUID;

/**
 * Defines an item tied to its {@link Order} and {@link Keychain}. Quantity should be the only
 * variable with the ability to change. This is done by User clicking on the RecyclerView in {@link
 * NewOrder}.
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
  @ColumnInfo(name = "order_id")
  private final String mOrderId;

  @NonNull
  @ColumnInfo(name = "cell_address")
  private final String mCellAddress;

  @NonNull
  @ColumnInfo(name = "quantity")
  private Integer mQuantity;

  @Ignore
  public OrderItem(String orderId, String cellAddress, Integer quantity) {
    this(UUID.randomUUID().toString(), cellAddress, orderId, quantity);
  }

  public OrderItem(
      @NonNull String id, @NonNull String orderId, @NonNull String cellAddress,
      @NonNull Integer quantity) {
    this.mId = id;
    this.mCellAddress = cellAddress;
    this.mOrderId = orderId;
    this.mQuantity = quantity;
  }

  @NonNull
  public String getId() {
    return mId;
  }

  @NonNull
  public String getKeychainId() {
    return mCellAddress;
  }

  @NonNull
  public String getOrderId() {
    return mOrderId;
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
    return "id:" + getId() + ", keychainId:" + getKeychainId() + ", orderId:"
        + getOrderId() + ", quantity:" + getQuantity();
  }
}
