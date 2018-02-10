package com.gmail.stonedevs.keychainorderhelper.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderEntity;
import java.util.List;

@Dao
public interface OrderDao {

  @Query("select * from OrderEntity "
      + "where id = :id")
  OrderEntity get(String id);

  @Query("select * from OrderEntity "
      + "order by orderDate desc")
  List<OrderEntity> getAll();

  @Insert
  void insert(OrderEntity orderEntity);

  @Insert
  void insert(OrderEntity... orderEntities);

  @Update
  void update(OrderEntity orderEntity);
}
