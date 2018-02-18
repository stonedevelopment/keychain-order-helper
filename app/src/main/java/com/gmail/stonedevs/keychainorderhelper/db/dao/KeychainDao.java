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

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Keychain;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

@Dao
public interface KeychainDao {

  @Query("select * from keychain "
      + "where id = :keychainId "
      + "limit 1")
  Keychain get(String keychainId);

  @Query("select * from keychain")
  List<Keychain> getAll();

  @Insert
  void insert(Keychain keychain);

  @Insert
  void insertAll(List<Keychain> keychains);

  @Delete
  void delete(Keychain keychain);
}
