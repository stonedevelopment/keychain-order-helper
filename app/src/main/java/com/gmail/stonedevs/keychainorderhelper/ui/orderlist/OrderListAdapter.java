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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
    OnRecyclerViewItemClickListener, ActionMode.Callback {

  private static final String TAG = OrderListAdapter.class.getSimpleName();

  private final Context mContext;

  private OrderListViewModel mViewModel;

  private List<Order> mOrders;

  private boolean mMultiSelect;
  private List<Order> mSelectedOrders;

  OrderListAdapter(Context context, OrderListViewModel viewModel) {
    mContext = context;
    mViewModel = viewModel;

    mMultiSelect = false;
    mSelectedOrders = new ArrayList<>(0);

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
    holder.bindItem(mContext, order, mMultiSelect);
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

  void finishActionMode(ActionMode mode) {
    mOrders.removeAll(mSelectedOrders);
    mode.finish();
  }

  @Override
  public void onItemClick(int position) {
    Order order = getItem(position);

    if (mMultiSelect) {
      if (mSelectedOrders.contains(order)) {
        mSelectedOrders.remove(order);
      } else {
        mSelectedOrders.add(order);
      }
    } else {
      mViewModel.getOrderDetailCommand().setValue(order.getId());
    }
  }

  @Override
  public boolean onItemLongClick(int position) {
    Order order = getItem(position);

    if (mSelectedOrders.contains(order)) {
      mSelectedOrders.remove(order);
    } else {
      mSelectedOrders.add(order);
    }

    if (!mMultiSelect) {
      ((AppCompatActivity) mContext).startSupportActionMode(this);
    }

    return false;
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add("Delete");

    mMultiSelect = true;
    notifyDataSetChanged();
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    mViewModel.startDeleteModeProcess(mode, mSelectedOrders);
    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {
    mMultiSelect = false;
    mSelectedOrders.clear();
    notifyDataSetChanged();
  }
}