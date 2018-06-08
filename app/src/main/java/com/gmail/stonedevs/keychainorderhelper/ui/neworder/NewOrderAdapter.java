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

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import com.gmail.stonedevs.keychainorderhelper.util.OrderUtils;
import java.util.ArrayList;
import java.util.List;

public class NewOrderAdapter extends RecyclerView.Adapter<NewOrderViewHolder> implements
    OnRecyclerViewItemClickListener {

  private static final String TAG = NewOrderAdapter.class.getSimpleName();

  private static int mMinOrderQuantity;
  private static int mMaxOrderQuantity;

  private NewOrderViewModel mViewModel;

  private List<OrderItem> mItems;

  private int[] mItemQuantities;

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
        .inflate(R.layout.list_item_order_item, parent, false);

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

  void updateItemQuantities(Context c, int orderCategory) {
    mItemQuantities = OrderUtils.getItemQuantities(c, orderCategory);
  }

  private int findItemQuantityPosition(int quantity) {
    for (int i = 0; i < mItemQuantities.length; i++) {
      if (mItemQuantities[i] == quantity) {
        return i;
      }
    }
    return 0;
  }

  private int findNextItemQuantityPosition(int quantity) {
    if (quantity == 0) {
      return 1;
    } else {
      int pos = findItemQuantityPosition(quantity);
      if (pos == mItemQuantities.length - 1) {
        return 0;
      }
      return ++pos;
    }
  }

  @Override
  public void onItemClick(int position) {
    OrderItem item = getItem(position);

    int quantity = item.getQuantity();
    int quantityPosition = findNextItemQuantityPosition(quantity);
    int newQuantity = mItemQuantities[quantityPosition];
    item.setQuantity(newQuantity);

    int change = newQuantity - quantity;

    mViewModel.updateOrderQuantityBy(change);
    mViewModel.updateUI();

    notifyItemChanged(position);
  }

  @Override
  public boolean onItemLongClick(int position) {
    OrderItem item = getItem(position);

    int quantity = item.getQuantity();
    int quantityPosition = findItemQuantityPosition(quantity);
    int lastPosition = mItemQuantities.length - 1;

    int newQuantity;
    if (quantityPosition == 0) {
      newQuantity = mItemQuantities[lastPosition];
    } else {
      newQuantity = 0;
    }
    item.setQuantity(newQuantity);

    int change = newQuantity - quantity;

    mViewModel.updateOrderQuantityBy(change);
    mViewModel.updateUI();

    notifyItemChanged(position);
    return true;
  }
}
