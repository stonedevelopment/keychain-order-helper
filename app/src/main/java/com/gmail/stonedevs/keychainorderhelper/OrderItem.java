package com.gmail.stonedevs.keychainorderhelper;

import android.support.annotation.NonNull;
import org.apache.poi.ss.util.CellAddress;

public class OrderItem {

  @NonNull
  private final String name;

  private int quantity;

  @NonNull
  private final CellAddress quantityLocation;

  OrderItem(@NonNull String name, int quantity, @NonNull CellAddress quantityLocation) {
    this.name = name;
    this.quantity = quantity;
    this.quantityLocation = quantityLocation;
  }

  @NonNull
  public String getName() {
    return name;
  }

  int getQuantity() {
    return quantity;
  }

  void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @NonNull
  CellAddress getQuantityLocation() {
    return quantityLocation;
  }
}
