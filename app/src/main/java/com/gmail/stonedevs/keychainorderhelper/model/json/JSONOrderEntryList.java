package com.gmail.stonedevs.keychainorderhelper.model.json;

import java.util.ArrayList;
import java.util.List;

public class JSONOrderEntryList {

  private List<JSONOrderEntry> orderEntries = new ArrayList<>(0);

  public JSONOrderEntryList() {
    //  blank constructor required
  }

  public List<JSONOrderEntry> getOrderEntries() {
    return orderEntries;
  }

  public JSONOrderEntry getEntry(int position) {
    return orderEntries.get(position);
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
