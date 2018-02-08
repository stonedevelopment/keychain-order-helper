package com.gmail.stonedevs.keychainorderhelper.model.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({"storeName", "orderDate", "orderQuantities", "orderTotal"})
public class JSONOrderEntry {

  private String storeName;
  private String orderDate;
  private ArrayList<Integer> orderQuantities;
  private Integer orderTotal;

  public JSONOrderEntry() {
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
