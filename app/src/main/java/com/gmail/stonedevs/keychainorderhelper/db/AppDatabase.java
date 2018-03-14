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

@Database(version = 1,
    entities = {
        Order.class,
        OrderItem.class},
    exportSchema = false)
@TypeConverters(value = {DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

  private static final String DB_NAME = "database.db";

  private static volatile AppDatabase sInstance;

  public abstract OrderDao orderDao();

  public abstract OrderItemDao orderItemDao();

  private static AppDatabase createInstance(final Context context) {
    return Room.databaseBuilder(context.getApplicationContext(),
        AppDatabase.class, DB_NAME)
        .build();
  }

  public static synchronized AppDatabase getInstance(Context context) {
    if (sInstance == null) {
      sInstance = createInstance(context);
    }

    return sInstance;
  }
}