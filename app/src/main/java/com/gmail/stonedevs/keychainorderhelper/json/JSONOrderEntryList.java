package com.gmail.stonedevs.keychainorderhelper.json;

import java.util.ArrayList;
import java.util.List;

public class JSONOrderEntryList {

  private List<JSONOrderEntry> orderEntries = new ArrayList<>(0);

  public JSONOrderEntryList() {
    //  blank constructor required
  }

  public JSONOrderEntryList(List<JSONOrderEntry> orderEntries) {
    this.orderEntries.clear();
    this.orderEntries.addAll(orderEntries);
  }

  public List<JSONOrderEntry> getOrderEntries() {
    return orderEntries;
  }

  public void setOrderEntries(
      List<JSONOrderEntry> orderEntries) {
    this.orderEntries.clear();
    this.orderEntries.addAll(orderEntries);
  }

  public void addEntry(JSONOrderEntry entry) {
    if (!entryExists(entry)) {
      orderEntries.add(entry);
    }
  }

  private boolean entryExists(JSONOrderEntry testEntry) {
    for (JSONOrderEntry entry : orderEntries) {
      if (testEntry.getStoreName().equals(entry.getStoreName()) &&
          testEntry.getOrderQuantities().equals(entry.getOrderQuantities()) &&
          testEntry.getOrderDate().equals(entry.getOrderDate())) {
        return true;
      }
    }
    return false;
  }
}
