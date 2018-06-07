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

package com.gmail.stonedevs.keychainorderhelper.ui.orderdetail;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;

/**
 * ViewHolder for {@link OrderDetailAdapter}. Displays order item specific data to the recycler
 * view.
 */

class OrderDetailViewHolder extends RecyclerView.ViewHolder {

  private final TextView mKeychainNameTextView;
  private final TextView mItemQuantityTextView;

  OrderDetailViewHolder(View itemView) {
    super(itemView);

    mKeychainNameTextView = itemView.findViewById(R.id.orderItemNameTextView);
    mItemQuantityTextView = itemView.findViewById(R.id.orderItemQuantityTextView);
  }

  void bindItem(@NonNull OrderItem item) {
    mKeychainNameTextView.setText(item.getName());
    mItemQuantityTextView.setText(String.valueOf(item.getQuantity()));
  }
}
