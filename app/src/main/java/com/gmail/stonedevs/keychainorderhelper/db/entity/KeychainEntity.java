package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import java.util.UUID;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

@Entity(tableName = "keychain",
    foreignKeys = {
        @ForeignKey(entity = CellEntity.class,
            parentColumns = "id",
            childColumns = "cellId"),
        @ForeignKey(entity = Order.class,
            parentColumns = "id",
            childColumns = "parentId")},
    indices = {
        @Index(value = {"cellId"}),
        @Index(value = {"orderId"})})

public class KeychainEntity {

  @NonNull
  @PrimaryKey
  private final String id;

  @NonNull
  private final String cellId;

  @NonNull
  private final String orderId;

  @NonNull
  private Integer quantity;

  @Ignore
  public KeychainEntity(String cellId, String orderId, Integer quantity) {
    this(UUID.randomUUID().toString(), cellId, orderId, quantity);
  }

  public KeychainEntity(
      @NonNull String id, @NonNull String cellId, @NonNull String orderId,
      @NonNull Integer quantity) {
    this.id = id;
    this.cellId = cellId;
    this.orderId = orderId;
    this.quantity = quantity;
  }

  @NonNull
  public String getId() {
    return id;
  }

  @NonNull
  public String getCellId() {
    return cellId;
  }

  @NonNull
  public String getOrderId() {
    return orderId;
  }

  @NonNull
  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(@NonNull Integer quantity) {
    this.quantity = quantity;
  }
}
