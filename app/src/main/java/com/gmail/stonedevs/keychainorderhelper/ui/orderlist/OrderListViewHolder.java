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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.CheckBox;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import com.gmail.stonedevs.keychainorderhelper.util.StringUtils;

/**
 * ViewHolder for {@link OrderListAdapter} with binding to its item layout.
 */

public class OrderListViewHolder extends RecyclerView.ViewHolder implements OnClickListener,
    OnLongClickListener {

  private static final String TAG = OrderListViewHolder.class.getSimpleName();

  private final CheckBox mSelectCheckBox;
  private final TextView mStoreNameTextView;
  private final TextView mOrderDateTextView;
  private final TextView mOrderQuantityTextView;
  private final TextView mOrderTerritoryTextView;

  private OnRecyclerViewItemClickListener mListener;

  private boolean mIsMultiSelect;
  private boolean mIsSelected;

  OrderListViewHolder(View itemView, OnRecyclerViewItemClickListener listener) {
    super(itemView);
    mListener = listener;

    mSelectCheckBox = itemView.findViewById(R.id.selectCheckBox);
    mStoreNameTextView = itemView.findViewById(R.id.storeNameTextView);
    mOrderDateTextView = itemView.findViewById(R.id.orderDateTextView);
    mOrderQuantityTextView = itemView.findViewById(R.id.orderQuantityTextView);
    mOrderTerritoryTextView = itemView.findViewById(R.id.orderTerritoryTextView);

    itemView.setOnClickListener(this);
    itemView.setOnLongClickListener(this);
  }

  void bindItem(Context c, @NonNull Order order, boolean isMultiSelect, boolean isSelected) {

    mIsMultiSelect = isMultiSelect;

    mSelectCheckBox.setChecked(isSelected);
    mSelectCheckBox.setVisibility(isMultiSelect ? View.VISIBLE : View.GONE);

    String storeName = order.getStoreName();
    if (TextUtils.isEmpty(storeName)) {
      mStoreNameTextView.setText(c.getString(R.string.layout_list_item_text_store_name_empty));
    } else {
      mStoreNameTextView.setText(storeName);
    }

    long orderDate = order.getOrderDate().getTime();
    mOrderDateTextView.setText(StringUtils.formatSentOrderDate(c, orderDate));

    int quantity = order.getOrderQuantity();
    mOrderQuantityTextView.setText(StringUtils.formatOrderQuantity(c, quantity));

    String orderTerritory = order.getOrderTerritory();
    mOrderTerritoryTextView.setText(orderTerritory);
  }

  private void selectItem() {
    mSelectCheckBox.setChecked(!mSelectCheckBox.isChecked());
  }

  @Override
  public void onClick(View v) {
    if (mIsMultiSelect) {
      selectItem();
    }

    Log.w(TAG, "onClick: " + getAdapterPosition());

    mListener.onItemClick(getAdapterPosition());
  }

  @Override
  public boolean onLongClick(View v) {
    selectItem();

    Log.w(TAG, "onLongClick: " + getAdapterPosition());

    return mListener.onItemLongClick(getAdapterPosition());
  }
}
