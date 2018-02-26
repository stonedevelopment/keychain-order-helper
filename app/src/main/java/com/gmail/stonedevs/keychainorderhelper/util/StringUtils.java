/*
 * Copyright (c) 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;
import android.text.format.DateUtils;
import com.gmail.stonedevs.keychainorderhelper.R;

/**
 * TODO: Add a class header comment!
 */

public class StringUtils {

  public static String formatSentOrderDate(Context c, long orderDate) {
    return String.format(c.getString(R.string.string_format_list_item_order_sent_text),
        DateUtils.getRelativeDateTimeString(c, orderDate, DateUtils.MINUTE_IN_MILLIS,
            DateUtils.WEEK_IN_MILLIS, DateUtils.FORMAT_NUMERIC_DATE));
  }

  public static String formatOrderQuantity(Context c, int quantity) {
    return String.format(c.getString(R.string.string_format_list_item_order_total_text), quantity);
  }
}
