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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class OrderListAdapter extends RecyclerView.Adapter<OrderListViewHolder> implements
    OnRecyclerViewItemClickListener {

  private final Context mContext;

  private OrderListViewModel mViewModel;

  private List<Order> mOrders;

  OrderListAdapter(Context context, OrderListViewModel viewModel) {
    mContext = context;
    mViewModel = viewModel;

    setData(new ArrayList<Order>(0));
  }

  @Override
  public OrderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_order, parent, false);

    return new OrderListViewHolder(view, this);
  }

  @Override
  public void onBindViewHolder(OrderListViewHolder holder, int position) {
    Order order = getItem(position);
    holder.bindItem(mContext, order);
  }

  @Override
  public int getItemCount() {
    return mOrders != null ? mOrders.size() : 0;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  private Order getItem(int position) {
    return mOrders.get(position);
  }

  void replaceData(List<Order> orders) {
    setData(orders);
  }

  private void setData(List<Order> orders) {
    mOrders = orders;
    notifyDataSetChanged();
  }

  @Override
  public void onItemClick(int position) {
    Order order = getItem(position);
    mViewModel.getOrderDetailCommand().setValue(order.getId());
  }

  @Override
  public boolean onItemLongClick(int position) {
    return false;
  }
}
