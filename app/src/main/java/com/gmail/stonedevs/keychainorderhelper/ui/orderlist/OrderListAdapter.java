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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.databinding.ListItemOrderBinding;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class OrderListAdapter extends RecyclerView.Adapter<OrderListViewHolder> {

  private OrderListViewModel mViewModel;

  private List<Order> mOrders;

  public OrderListAdapter(OrderListViewModel viewModel, ArrayList<Order> orders) {
    mViewModel = viewModel;

    setData(orders);
  }

  @Override
  public OrderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
    ListItemOrderBinding binding = ListItemOrderBinding.inflate(inflater, parent, false);

    return new OrderListViewHolder(binding);
  }

  @Override
  public void onBindViewHolder(OrderListViewHolder holder, int position) {
    Order order = getItem(position);
    holder.bind(order, new OrderListItemClickListener() {
      @Override
      public void onItemClick(Order order) {
        mViewModel.getOpenOrderEvent().setValue(order.getId());
      }
    });
  }

  @Override
  public int getItemCount() {
    return mOrders != null ? mOrders.size() : 0;
  }

  public Order getItem(int position) {
    return mOrders.get(position);
  }

  public List<Order> getItems() {
    return mOrders;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  public void replaceData(List<Order> orders) {
    setData(orders);
  }

  private void setData(List<Order> orders) {
    mOrders = orders;
    notifyDataSetChanged();
  }
}
