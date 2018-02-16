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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;

/**
 * ViewHolder for {@link OrderListAdapter} with binding to its item layout.
 */

public class OrderListViewHolder extends RecyclerView.ViewHolder implements OnClickListener,
    OnLongClickListener {

  private final TextView mStoreNameTextView;
  private final TextView mOrderDateTextView;
  private final TextView mOrderTimeSinceTextView;

  private OnRecyclerViewItemClickListener mListener;

  OrderListViewHolder(View itemView, OnRecyclerViewItemClickListener listener) {
    super(itemView);

    mListener = listener;

    mStoreNameTextView = itemView.findViewById(R.id.storeNameTextView);
    mOrderDateTextView = itemView.findViewById(R.id.orderDateTextView);
    mOrderTimeSinceTextView = itemView.findViewById(R.id.orderTimeSinceTextView);
  }

  void bindItem(Context context, @NonNull Order order) {
    mStoreNameTextView.setText(order.getStoreName());

    long orderDate = order.getOrderDate().getTime();
    mOrderDateTextView.setText(DateUtils
        .formatDateTime(context, orderDate, DateUtils.FORMAT_NUMERIC_DATE));
    mOrderTimeSinceTextView.setText(DateUtils.formatElapsedTime(orderDate));
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
