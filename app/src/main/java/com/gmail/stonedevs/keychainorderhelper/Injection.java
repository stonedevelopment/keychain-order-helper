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

package com.gmail.stonedevs.keychainorderhelper;

import android.content.Context;
import android.support.annotation.Nullable;
import com.gmail.stonedevs.keychainorderhelper.db.AppDatabase;
import com.gmail.stonedevs.keychainorderhelper.db.DataSource;
import com.gmail.stonedevs.keychainorderhelper.db.LocalDataSource;
import com.gmail.stonedevs.keychainorderhelper.db.Repository;
import com.gmail.stonedevs.keychainorderhelper.util.executor.AppExecutors;

/**
 * Enables injection of production implementations for
 * {@link DataSource} at compile time.
 */

class Injection {

  static Repository provideRepository(@Nullable Context context) {
    AppDatabase database = AppDatabase.getInstance(context);
    return Repository
        .getInstance(LocalDataSource
            .getInstance(new AppExecutors(), database.orderDao(), database.orderItemDao()));
  }
}
