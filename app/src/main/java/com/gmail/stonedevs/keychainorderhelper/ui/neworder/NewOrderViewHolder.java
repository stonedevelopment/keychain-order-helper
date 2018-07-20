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

package com.gmail.stonedevs.keychainorderhelper.ui.neworder;

import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;

/**
 * ViewHolder for {@link NewOrderAdapter}. Displays order item specific data to the recycler view,
 * and listens to the click actions made by User.
 */

public class NewOrderViewHolder extends RecyclerView.ViewHolder implements OnClickListener,
    OnLongClickListener {

  private final ConstraintLayout mLayout;
  private final TextView mKeychainNameTextView;
  private final TextView mItemQuantityTextView;

  private OnRecyclerViewItemClickListener mListener;

  NewOrderViewHolder(View itemView, OnRecyclerViewItemClickListener listener) {
    super(itemView);

    mListener = listener;

    mLayout = itemView.findViewById(R.id.list_item);
    mKeychainNameTextView = itemView.findViewById(R.id.orderItemNameTextView);
    mItemQuantityTextView = itemView.findViewById(R.id.orderItemQuantityTextView);

    itemView.setOnClickListener(this);
    itemView.setOnLongClickListener(this);
  }

  void bindItem(@NonNull OrderItem item) {
    String name = item.getName();

//  TODO  Distinguish whether item is in Best Seller list (WIP)
//    if (name.startsWith("*")) {
//      name = name.replace("*", "");
//      name += " (Top Seller)";
//    }

    mKeychainNameTextView.setText(name);

    if (item.getQuantity() > 0) {
      mLayout
          .setBackgroundColor(mLayout.getContext().getResources().getColor(R.color.accent_material_dark));
      mItemQuantityTextView.setText(String.valueOf(item.getQuantity()));
    } else {
      mLayout
          .setBackgroundColor(mLayout.getContext().getResources().getColor(android.R.color.background_light));
      mItemQuantityTextView.setText("");
    }
  }

  @Override
  public void onClick(View v) {
    mListener.onItemClick(getAdapterPosition());
  }

  @Override
  public boolean onLongClick(View v) {
    return mListener.onItemLongClick(getAdapterPosition());
  }
}
