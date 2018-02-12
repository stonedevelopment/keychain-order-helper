package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import java.util.UUID;

/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

@Entity(tableName = "cell")
public class CellEntity {

  @NonNull
  @PrimaryKey
  private final String id;

  @NonNull
  private final String name;

  @NonNull
  private final String cellAddress;

  @Ignore
  public CellEntity(String name, String cellAddress) {
    this(UUID.randomUUID().toString(), name, cellAddress);
  }

  public CellEntity(@NonNull String id, @NonNull String name, @NonNull String cellAddress) {
    this.id = id;
    this.name = name;
    this.cellAddress = cellAddress;
  }

  @NonNull
  public String getId() {
    return id;
  }

  @NonNull
  public String getName() {
    return name;
  }

  @NonNull
  public String getCellAddress() {
    return cellAddress;
  }
}
