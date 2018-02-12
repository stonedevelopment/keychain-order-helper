package com.gmail.stonedevs.keychainorderhelper.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class DateUtil {

  public static String getFormattedDateForLayout(Date date) {
    return new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(date);
  }

  public static String getFormattedDateForFilename(Date date) {
    return new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date);
  }
}
