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

package com.gmail.stonedevs.keychainorderhelper.db.dao;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.gmail.stonedevs.keychainorderhelper.db.AppDatabase;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import java.util.List;

/**
 * Data Access Object for {@link OrderItem}.
 *
 * @see AppDatabase
 */
@Dao
public interface OrderItemDao {

  /**
   * Get a list of {@link OrderItem} objects by their corresponding row id of {@link Order}.
   *
   * @param orderId The row id that ties {@link OrderItem} to {@link Order}.
   * @return The list of {@link OrderItem} objects.
   */
  @Query("select * from orderitem "
      + "where order_id = :orderId")
  List<OrderItem> get(String orderId);

  /**
   * Insert a single {@link OrderItem} object.
   *
   * @param orderItem The {@link OrderItem} object to insert.
   */
  @Insert(onConflict = REPLACE)
  void insert(OrderItem orderItem);

  /**
   * Insert a list of {@link OrderItem} objects.
   *
   * @param orderItemList The list of {@link OrderItem} objects to insert.
   */
  @Insert(onConflict = REPLACE)
  void insert(List<OrderItem> orderItemList);

  /**
   * Delete a single {@link OrderItem} object.
   *
   * @param orderItem The {@link OrderItem} to insert.
   * @return The number of rows deleted.
   */
  @Delete
  int delete(OrderItem orderItem);

  /**
   * Delete a list of {@link OrderItem} objects.
   *
   * @param orderItemList The list of {@link OrderItem} objects to delete.
   * @return The number of rows deleted.
   */
  @Delete
  int delete(List<OrderItem> orderItemList);
}