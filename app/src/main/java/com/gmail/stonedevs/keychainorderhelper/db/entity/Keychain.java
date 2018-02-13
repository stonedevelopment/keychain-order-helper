package com.gmail.stonedevs.keychainorderhelper.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import java.util.Objects;
import java.util.UUID;

/**
 * Defines order form keychain name and its quantity cell address, used in filling {@link
 * NewOrderActivity} keychain list. {@link OrderItem} links to this table entity.
 */

@Entity
public class Keychain {

  @NonNull
  @PrimaryKey
  @ColumnInfo(name = "id")
  private final String mId;

  @NonNull
  @ColumnInfo(name = "name")
  private final String mName;

  @NonNull
  @ColumnInfo(name = "cell_address")
  private final String mCellAddress;

  @Ignore
  public Keychain(String name, String cellAddress) {
    this(UUID.randomUUID().toString(), name, cellAddress);
  }

  public Keychain(@NonNull String id, @NonNull String name, @NonNull String cellAddress) {
    this.mId = id;
    this.mName = name;
    this.mCellAddress = cellAddress;
  }

  @NonNull
  public String getId() {
    return mId;
  }

  @NonNull
  public String getName() {
    return mName;
  }

  @NonNull
  public String getCellAddress() {
    return mCellAddress;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    Keychain keychain = (Keychain) obj;
    return Objects.equals(getId(), keychain.getId()) &&
        Objects.equals(getName(), keychain.getName()) &&
        Objects.equals(getCellAddress(), keychain.getCellAddress());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId(), getName(), getCellAddress());
  }

  @Override
  public String toString() {
    return "id:" + getId() + ", name:" + getName() + ", cellAddress:" + getCellAddress();
  }
}
