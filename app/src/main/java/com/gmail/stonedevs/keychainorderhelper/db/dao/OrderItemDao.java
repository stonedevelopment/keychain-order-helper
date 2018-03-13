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

package com.gmail.stonedevs.keychainorderhelper.db.dao;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import java.util.List;

@Dao
public interface OrderItemDao {

  @Query("select * from orderitem "
      + "where order_id = :orderId")
  List<OrderItem> get(String orderId);

  @Insert(onConflict = REPLACE)
  void insert(OrderItem orderItem);

  @Insert(onConflict = REPLACE)
  void insert(List<OrderItem> orderItemList);

  @Delete
  void delete(OrderItem orderItem);

  @Delete
  void delete(List<OrderItem> orderItemList);
}