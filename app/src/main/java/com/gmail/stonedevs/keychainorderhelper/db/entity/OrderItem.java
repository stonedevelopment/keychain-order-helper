package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.model.NewOrder;
import java.util.Objects;
import java.util.UUID;

/**
 * Defines an item tied to its {@link Order} and {@link Keychain}. Quantity should be the only
 * variable with the ability to change. This is done by User clicking on the RecyclerView in {@link
 * NewOrder}.
 */

@Entity(foreignKeys = {
    @ForeignKey(entity = Keychain.class,
        parentColumns = "id",
        childColumns = "keychain_id"),
    @ForeignKey(entity = Order.class,
        parentColumns = "id",
        childColumns = "order_id")},
    indices = {
        @Index(value = {"keychain_id"}),
        @Index(value = {"order_id"})})

public class OrderItem {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private final String mId;

  @NonNull
  @ColumnInfo(name = "keychain_id")
  private final String mKeychainId;

  @NonNull
  @ColumnInfo(name = "order_id")
  private final String mOrderId;

  @NonNull
  @ColumnInfo(name = "quantity")
  private Integer mQuantity;

  @Ignore
  public OrderItem(String keychainId, String orderId, Integer quantity) {
    this(UUID.randomUUID().toString(), keychainId, orderId, quantity);
  }

  public OrderItem(
      @NonNull String id, @NonNull String keychainId, @NonNull String orderId,
      @NonNull Integer quantity) {
    this.mId = id;
    this.mKeychainId = keychainId;
    this.mOrderId = orderId;
    this.mQuantity = quantity;
  }

  @NonNull
  public String getId() {
    return mId;
  }

  @NonNull
  public String getKeychainId() {
    return mKeychainId;
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
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    OrderItem item = (OrderItem) obj;
    return Objects.equals(getId(), item.getId()) &&
        Objects.equals(getKeychainId(), item.getKeychainId()) &&
        Objects.equals(getOrderId(), item.getOrderId()) &&
        Objects.equals(getQuantity(), item.getQuantity());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getKeychainId(), getOrderId(), getQuantity());
  }

  @Override
  public String toString() {
    return "id:" + getId() + ", keychainId:" + getKeychainId() + ", orderId:"
        + getOrderId() + ", quantity:" + getQuantity();
  }
}
