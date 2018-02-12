package com.gmail.stonedevs.keychainorderhelper.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class NewOrder {

  private String storeName;

  private Date orderDate;

  private List<Integer> orderQuantities;

  public NewOrder(String storeName, Date orderDate) {
    this(storeName, orderDate, new ArrayList<Integer>(0));
  }

  public NewOrder(String storeName, Date orderDate,
      List<Integer> orderQuantities) {
    this.storeName = storeName;
    this.orderDate = orderDate;
    this.orderQuantities = orderQuantities;
  }

  public String getStoreName() {
    return storeName;
  }

  public void setStoreName(String storeName) {
    this.storeName = storeName;
  }

  public Date getOrderDate() {
    return orderDate;
  }

  public void setOrderDate(Date orderDate) {
    this.orderDate = orderDate;
  }

  public List<Integer> getOrderQuantities() {
    return orderQuantities;
  }

  public void setOrderQuantities(List<Integer> orderQuantities) {
    this.orderQuantities = orderQuantities;
  }
}
