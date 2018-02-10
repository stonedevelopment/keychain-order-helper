package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity()
public class OrderEntity {

  @NonNull
  @PrimaryKey
  private final String id;

  @NonNull
  private final String storeName;

  @NonNull
  private final Date orderDate;

  @NonNull
  private final List<Integer> orderQuantities;

  @NonNull
  private final Integer orderTotal;

  @Ignore
  public OrderEntity(String storeName, Date orderDate, List<Integer> orderQuantities,
      Integer orderTotal) {
    this(UUID.randomUUID().toString(), storeName, orderDate, orderQuantities, orderTotal);
  }

  public OrderEntity(@NonNull String id, @NonNull String storeName, @NonNull Date orderDate,
      @NonNull List<Integer> orderQuantities, @NonNull Integer orderTotal) {
    this.id = id;
    this.storeName = storeName;
    this.orderDate = orderDate;
    this.orderQuantities = orderQuantities;
    this.orderTotal = orderTotal;
  }

  @NonNull
  public String getId() {
    return id;
  }

  @NonNull
  public String getStoreName() {
    return storeName;
  }

  @NonNull
  public Date getOrderDate() {
    return orderDate;
  }

  @NonNull
  public List<Integer> getOrderQuantities() {
    return orderQuantities;
  }

  @NonNull
  public Integer getOrderTotal() {
    return orderTotal;
  }
}
