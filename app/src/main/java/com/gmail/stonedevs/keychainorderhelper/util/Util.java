package com.gmail.stonedevs.keychainorderhelper.util;

import static com.gmail.stonedevs.keychainorderhelper.MainActivity.TAG;

import android.net.Uri;
import android.util.Log;
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

  public static void deleteTempFile(Uri path) {
    deleteTempFile(new File(path.toString()));
  }

  public static void deleteTempFile(File file) {
    if (file.delete()) {
      Log.d(TAG, "deleteTempFile: file deleted successfully.");
    } else {
      Log.d(TAG,
          "deleteTempFile: file was not deleted successfully. Uri Path: "
              + file.getPath());
    }
  }
}
