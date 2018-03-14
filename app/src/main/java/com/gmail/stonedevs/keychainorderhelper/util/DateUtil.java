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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class that provides Date object formatting.
 */

public class DateUtil {

  public static String getFormattedDateForLayout(Date date) {
    return new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(date);
  }

  public static String getFormattedTimeForLayout(Date date) {
    return new SimpleDateFormat("hh:mma", Locale.getDefault()).format(date);
  }

  public static String getFormattedDateForFilename(Date date) {
    return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date);
  }
}
