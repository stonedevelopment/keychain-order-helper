package com.gmail.stonedevs.keychainorderhelper.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import com.gmail.stonedevs.keychainorderhelper.db.converter.DateConverter;
import com.gmail.stonedevs.keychainorderhelper.db.dao.OrderDao;
import com.gmail.stonedevs.keychainorderhelper.db.dao.OrderItemDao;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

@Database(version = 1,
    entities = {
        Order.class,
        OrderItem.class},
    exportSchema = false)
@TypeConverters(value = {DateConverter.class})

public abstract class AppDatabase extends RoomDatabase {

  private static final String DB_NAME = "database.db";

  private static volatile AppDatabase sInstance;

  //  DAO list
  public abstract OrderDao orderDao();

  public abstract OrderItemDao orderItemDao();

  public static synchronized AppDatabase getInstance(Context context) {
    if (sInstance == null) {
      sInstance = createInstance(context);
    }

    return sInstance;
  }

  private static AppDatabase createInstance(final Context context) {
    return Room.databaseBuilder(context.getApplicationContext(),
        AppDatabase.class, DB_NAME)
        .build();
  }
}
