package com.gmail.stonedevs.keychainorderhelper.util;

import android.net.Uri;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class Util {

  public static String getFormattedDateForLayout() {
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyy", Locale.US);

    return format.format(calendar.getTime());
  }

  public static boolean deleteTempFile(Uri path) {
    return deleteTempFile(new File(path.toString()));
  }

  private static boolean deleteTempFile(File file) {
    return file.delete();
  }
}
