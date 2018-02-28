package com.gmail.stonedevs.keychainorderhelper.db.dao;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.List;

@Dao
public interface OrderDao {

  @Query("select * from orders "
      + "where id = :id")
  Order get(String id);

  @Query("select * from orders "
      + "order by order_date desc")
  List<Order> getAll();

  @Insert(onConflict = REPLACE)
  void insert(Order order);

  @Insert(onConflict = REPLACE)
  void insert(List<Order> orders);

  @Update
  void update(Order order);

  @Query("delete from orders")
  int deleteAll();

  @Delete
  int delete(Order order);

  @Delete
  int delete(List<Order> orders);
}
