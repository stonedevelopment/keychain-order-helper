package com.gmail.stonedevs.keychainorderhelper.db.converter;

import android.arch.persistence.room.TypeConverter;
import java.util.Date;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class DateConverter {

  @TypeConverter
  public static Date toDate(Long time) {
    return time == null ? null : new Date(time);
  }

  @TypeConverter
  public static Long toTime(Date date) {
    return date == null ? null : date.getTime();
  }
}
