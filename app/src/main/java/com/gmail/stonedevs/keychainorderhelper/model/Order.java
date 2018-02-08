package com.gmail.stonedevs.keychainorderhelper.model;

import android.content.Context;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.R;
import java.util.ArrayList;

public class Order {

  @NonNull
  private final String storeName;

  @NonNull
  private final String orderDate;

  private final ArrayList<Integer> orderQuantities;

  @NonNull
  private final Integer orderTotal;

  public Order(@NonNull String storeName, @NonNull String orderDate,
      ArrayList<Integer> orderQuantities, @NonNull Integer orderTotal) {
    this.storeName = storeName;
    this.orderDate = orderDate;
    if (orderQuantities != null) {
      this.orderQuantities = orderQuantities;
    } else {
      this.orderQuantities = new ArrayList<>(0);
    }
    this.orderTotal = orderTotal;
  }

  @NonNull
  public String getStoreName() {
    return storeName;
  }

  @NonNull
  public String getOrderDate() {
    return orderDate;
  }

  @NonNull
  public Integer getOrderTotal() {
    return orderTotal;
  }

  @NonNull
  public ArrayList<Integer> getOrderQuantities() {
    return orderQuantities;
  }

  public String getOrderTotalText(Context c) {
    return String
        .format(c.getString(R.string.string_format_list_item_order_total_text), getOrderTotal());
  }
}
