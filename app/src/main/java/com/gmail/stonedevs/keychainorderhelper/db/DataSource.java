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

import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import java.util.List;

public interface DataSource {

  interface DataNotAvailableCallback {

    /**
     * Data was not successfully returned from database.
     */
    void onDataNotAvailable();
  }

  interface LoadCallback extends DataNotAvailableCallback {

    /**
     * Data was successfully returned from database.
     *
     * @param order Object queried.
     */
    void onDataLoaded(CompleteOrder order);
  }

  interface LoadAllCallback extends DataNotAvailableCallback {

    /**
     * Data was successfully loaded from database.
     *
     * @param orders List of objects queried.
     */
    void onDataLoaded(List<Order> orders);
  }

  interface InsertCallback {

    /**
     * Data was inserted successfully.
     */
    void onDataInserted();
  }

  interface DeleteCallback {

    /**
     * Data was deleted successfully.
     *
     * @param rowsDeleted The amount of rows that were deleted.
     */
    void onDataDeleted(int rowsDeleted);
  }

  /**
   * Query database for object by its rowId.
   *
   * @param orderId RowID of object.
   * @param callback Listener to notify of query result.
   */
  void getOrder(@NonNull String orderId, @NonNull LoadCallback callback);

  /**
   * Query database for all objects in database.
   *
   * @param callback Listener to notify of query results.
   */
  void getAllOrders(int orderCategory, @NonNull LoadAllCallback callback);

  /**
   * Insert or replace object in database.
   *
   * @param order Object to insert or replace.
   * @param callback Listener to notify of insertion or replacement.
   */
  void saveOrder(@NonNull CompleteOrder order, @NonNull InsertCallback callback);

  /**
   * Insert or replace list of objects in database.
   *
   * @param orders List of objects to insert or replace.
   * @param callback Listener to notify of insertion or replacement.
   */
  void saveOrders(@NonNull List<CompleteOrder> orders, @NonNull InsertCallback callback);

  /**
   * Delete object from database.
   *
   * @param order Object to delete.
   * @param callback Listener to notify of deletion.
   */
  void deleteOrder(@NonNull Order order, @NonNull DeleteCallback callback);

  /**
   * Delete a list of objects from database.
   *
   * @param orders List of objects to delete.
   * @param callback Listener to notify of deletions.
   */
  void deleteOrders(@NonNull List<Order> orders, @NonNull DeleteCallback callback);

  /**
   * Delete all objects in table.
   *
   * @param callback Listener to notify of deletions.
   */
  void deleteAllOrders(@NonNull DeleteCallback callback);
}