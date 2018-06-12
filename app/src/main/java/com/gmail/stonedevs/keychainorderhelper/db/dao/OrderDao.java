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
import java.util.List;

/**
 * Data Access Object for {@link Order}.
 *
 * @see AppDatabase
 */
@Dao
public interface OrderDao {

  /**
   * Get a single {@link Order} object by its rowId.
   *
   * @param id rowId found in the table
   * @return The Order object.
   */
  @Query("select * from orders "
      + "where id = :id "
      + "limit 1")
  Order get(String id);

  /**
   * Get a list of {@link Order} objects, descending order by its date, with a limit of 30 records.
   *
   * @return The list of Order objects.
   */
  @Query("select * from orders "
      + "where order_category = :orderCategory "
      + "order by order_date desc "
      + "limit 30")
  List<Order> getAll(int orderCategory);

  /**
   * Insert a single {@link Order} object.
   *
   * @param order The Order object to insert.
   */
  @Insert(onConflict = REPLACE)
  void insert(Order order);

  /**
   * Insert a list of {@link Order} objects.
   *
   * @param orders The list of Order objects to insert.
   */
  @Insert(onConflict = REPLACE)
  void insert(List<Order> orders);

  /**
   * Delete a single {@link Order} object.
   *
   * @param order The Order object to delete.
   * @return The number of rows deleted.
   */
  @Delete
  int delete(Order order);

  /**
   * Delete a list of {@link Order} objects.
   *
   * @param orders The list of Order objects to delete.
   * @return The number of rows deleted.
   */
  @Delete
  int delete(List<Order> orders);

  /**
   * Delete all {@link Order} objects in the table.
   *
   * @return The number of rows deleted.
   */
  @Query("delete from orders")
  int deleteAll();
}