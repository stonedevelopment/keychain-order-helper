package com.gmail.stonedevs.keychainorderhelper.model;

import android.support.annotation.NonNull;
import org.apache.poi.ss.util.CellAddress;

public class Keychain {

  @NonNull
  private final String name;

  private int quantity;

  @NonNull
  private final CellAddress quantityLocation;

  public Keychain(@NonNull String name, int quantity, @NonNull CellAddress quantityLocation) {
    this.name = name;
    this.quantity = quantity;
    this.quantityLocation = quantityLocation;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setQuantity(int quantity) {
    this.quantity = quantity;
  }

  @NonNull
  public CellAddress getQuantityLocation() {
    return quantityLocation;
  }
}
