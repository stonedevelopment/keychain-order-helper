/*
 * Copyright 2018, Jared Shane Stone
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrder;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrderEntry;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrderEntryList;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated Was used by a previous version to save and retrieve order data via json file.
 */

@Deprecated
public class JSONUtils {

  public static List<JSONOrder> getJSONOrders(Context c) {
    //	Create new Order objects from the JSON entry list.
    JSONOrderEntryList entryList = getOrderEntryList(c);

    if (entryList == null) {
      return null;
    }

    List<JSONOrder> items = new ArrayList<>(0);
    List<JSONOrderEntry> entries = entryList.getOrderEntries();

    for (int i = entries.size() - 1; i >= 0; i--) {
      JSONOrderEntry entry = entryList.getEntry(i);
      String storeName = entry.getStoreName();
      String orderDate = entry.getOrderDate();
      ArrayList<Integer> orderQuantities = entry.getOrderQuantities();
      Integer orderTotal = entry.getOrderTotal();

      items.add(new JSONOrder(storeName, orderDate, orderQuantities, orderTotal));
    }

    return items;
  }

  //	Read JSON into JSONObject
  private static JSONOrderEntryList getOrderEntryList(Context c) {
    ObjectMapper mapper = new ObjectMapper();

    File file = new File(c.getExternalFilesDir(null),
        c.getString(R.string.json_filename_previous_orders));

    try {
      return mapper.readValue(file, JSONOrderEntryList.class);
    } catch (IOException e) {
      return null;
    }
  }

  public static boolean removeOrderJSONFile(Context c) {
    File file = new File(c.getExternalFilesDir(null),
        c.getString(R.string.json_filename_previous_orders));

    return file.delete();
  }
}
