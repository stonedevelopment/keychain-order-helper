package com.gmail.stonedevs.keychainorderhelper.model.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.gmail.stonedevs.keychainorderhelper.model.Order;
import java.util.ArrayList;

@JsonPropertyOrder({"storeName", "orderDate", "orderQuantities", "orderTotal"})
public class JSONOrderEntry {

  private String storeName;
  private String orderDate;
  private ArrayList<Integer> orderQuantities;
  private Integer orderTotal;

  public JSONOrderEntry() {
  }

  public JSONOrderEntry(Order order) {
    this(order.getStoreName(), order.getOrderDate(), order.getOrderQuantities(),
        order.getOrderTotal());
  }

  public JSONOrderEntry(String storeName, String orderDate, ArrayList<Integer> orderQuantities,
      Integer orderTotal) {
    this.storeName = storeName;
    this.orderQuantities = orderQuantities;
    this.orderDate = orderDate;
    this.orderTotal = orderTotal;
  }

  public String getStoreName() {
    return storeName;
  }

  public ArrayList<Integer> getOrderQuantities() {
    return orderQuantities;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public Integer getOrderTotal() {
    return orderTotal;
  }
}
