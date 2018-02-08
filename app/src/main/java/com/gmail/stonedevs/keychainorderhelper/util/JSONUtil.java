package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrderEntryList;
import java.io.File;
import java.io.IOException;

public class JSONUtil {

  public static JSONOrderEntryList getOrderEntryList(Context c) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    File file = new File(c.getExternalFilesDir(null),
        c.getString(R.string.json_filename_previous_orders));

    if (!file.exists()) {
      return new JSONOrderEntryList();
    }

    return mapper.readValue(file, JSONOrderEntryList.class);
  }

  public static void setOrderEntryList(Context c, JSONOrderEntryList entryList) throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    File file = new File(c.getExternalFilesDir(null),
        c.getString(R.string.json_filename_previous_orders));

    mapper.writeValue(file, entryList);
  }
}
