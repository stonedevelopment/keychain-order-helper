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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class NewOrderAdapter extends RecyclerView.Adapter<NewOrderViewHolder> implements
    OnRecyclerViewItemClickListener {

  private static final String TAG = NewOrderAdapter.class.getSimpleName();

  private static int mMinOrderQuantity;
  private static int mMaxOrderQuantity;

  private NewOrderViewModel mViewModel;

  private List<OrderItem> mItems;

  NewOrderAdapter(Context c, NewOrderViewModel viewModel) {

    //  set min/max quantity attributes
    mMinOrderQuantity = c.getResources().getInteger(R.integer.order_item_quantity_lower_value);
    mMaxOrderQuantity = c.getResources().getInteger(R.integer.order_item_quantity_higher_value);

    mViewModel = viewModel;

    setData(new ArrayList<OrderItem>(0));
  }

  @Override
  public NewOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_keychain, parent, false);

    return new NewOrderViewHolder(view, this);
  }

  @Override
  public void onBindViewHolder(NewOrderViewHolder holder, int position) {
    OrderItem item = getItem(position);
    holder.bindItem(item);
  }

  @Override
  public int getItemCount() {
    return mItems != null ? mItems.size() : 0;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  private OrderItem getItem(int position) {
    return mItems.get(position);
  }

  void replaceData(List<OrderItem> items) {
    setData(items);
  }

  void setData(List<OrderItem> items) {
    mItems = items;
    notifyDataSetChanged();
  }

  @Override
  public void onItemClick(int position) {
    OrderItem item = getItem(position);

    int quantity = item.getQuantity();

    if (quantity < mMinOrderQuantity) {
      item.setQuantity(mMinOrderQuantity);
    } else if (quantity < mMaxOrderQuantity) {
      item.setQuantity(mMaxOrderQuantity);
    } else {
      item.setQuantity(0);
    }

    int newQuantity = item.getQuantity();

    int change = newQuantity - quantity;

    Log.d(TAG, "onItemClick: " + newQuantity + " - " + quantity + " = " + change);

    mViewModel.updateOrderQuantityBy(change);

    notifyItemChanged(position);
  }

  @Override
  public boolean onItemLongClick(int position) {
    OrderItem item = getItem(position);

    int quantity = item.getQuantity();

    item.setQuantity(0);

    int newQuantity = item.getQuantity();

    int change = newQuantity - quantity;

    Log.d(TAG, "onItemLongClick: " + newQuantity + " - " + quantity + " = " + change);

    mViewModel.updateOrderQuantityBy(change);

    notifyItemChanged(position);
    return true;
  }
}
