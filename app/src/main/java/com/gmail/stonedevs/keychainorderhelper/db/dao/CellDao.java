package com.gmail.stonedevs.keychainorderhelper.db.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.gmail.stonedevs.keychainorderhelper.db.entity.CellEntity;
import java.util.List;

/**
 * Created by Shane Stone on 2/11/2018.
 *
 * Email: stonedevs@gmail.com
 */

@Dao
public interface CellDao {

  @Query("select * from cell "
      + "where mId = :id "
      + "limit 1")
  CellEntity get(String id);

  @Query("select * from cell")
  List<CellEntity> getAll();

  @Insert
  void insert(CellEntity cellEntity);

  @Insert
  void insert(CellEntity... cellEntities);

  @Insert
  void insert(List<CellEntity> cellEntities);
}
