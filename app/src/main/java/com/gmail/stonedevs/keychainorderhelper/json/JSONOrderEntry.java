package com.gmail.stonedevs.keychainorderhelper.json;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

@JsonPropertyOrder({"storeName", "orderDate", "orderQuantities"})
public class JSONOrderEntry {

  private String storeName;
  private List<String> orderQuantities;
  private String orderDate;

  public JSONOrderEntry() {
  }

  public JSONOrderEntry(String storeName, List<String> orderQuantities, String orderDate) {
    this.storeName = storeName;
    this.orderQuantities = orderQuantities;
    this.orderDate = orderDate;
  }

  public String getStoreName() {
    return storeName;
  }

  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }

  public List<String> getOrderQuantities() {
    return orderQuantities;
  }

  public void setOrderQuantities(List<String> orderQuantities) {
    this.orderQuantities = orderQuantities;
  }

  public String getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(String orderDate) {
    this.orderDate = orderDate;
  }
}
