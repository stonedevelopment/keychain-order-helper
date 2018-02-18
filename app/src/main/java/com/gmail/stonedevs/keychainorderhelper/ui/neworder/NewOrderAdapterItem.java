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

package com.gmail.stonedevs.keychainorderhelper.ui.neworder;

/**
 * TODO: Add a class header comment!
 */

public class NewOrderAdapterItem {

  private final String mName;

  private Integer mItemQuantity;

  public NewOrderAdapterItem(String name, Integer itemQuantity) {
    this.mName = name;
    this.mItemQuantity = itemQuantity;
  }

  public String getName() {
    return mName;
  }

  public Integer getItemQuantity() {
    return mItemQuantity;
  }

  public void setItemQuantity(Integer itemQuantity) {
    this.mItemQuantity = itemQuantity;
  }
}
