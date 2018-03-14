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

package com.gmail.stonedevs.keychainorderhelper.db.converter;

import android.arch.persistence.room.TypeConverter;
import com.gmail.stonedevs.keychainorderhelper.db.AppDatabase;
import java.util.Date;

/**
 * Helper method to convert a {@link Date} object into a {@link Long}.
 *
 * @see AppDatabase
 */
public class DateConverter {

  /**
   * Convert a {@link Long} value object, representing milliseconds, into a {@link Date} object.
   *
   * @param time The {@link Long} value object to convert, represented in milliseconds.
   * @return The converted {@link Date} object.
   */
  @TypeConverter
  public static Date toDate(Long time) {
    return time == null ? null : new Date(time);
  }

  /**
   * Convert a {@link Date} object into a {@link Long} value object, representing milliseconds.
   *
   * @param date The {@link Date} object to convert.
   * @return The converted {@link Long} value object.
   */
  @TypeConverter
  public static Long toTime(Date date) {
    return date == null ? null : date.getTime();
  }
}
