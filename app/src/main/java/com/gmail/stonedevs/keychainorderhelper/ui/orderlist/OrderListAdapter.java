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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListAdapter.OrderListViewHolder;
import com.gmail.stonedevs.keychainorderhelper.util.StringUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderListAdapter extends RecyclerView.Adapter<OrderListViewHolder> implements
    OnRecyclerViewItemClickListener, ActionMode.Callback {

  private static final String TAG = OrderListAdapter.class.getSimpleName();

  private final Context mContext;

  private OrderListViewModel mViewModel;

  private List<Order> mOrders;

  //  Action mode object used for multi select deleting
  private ActionMode mActionMode;

  private List<Order> mSelectedOrders;

  OrderListAdapter(Context context, OrderListViewModel viewModel) {
    mContext = context;
    mViewModel = viewModel;

    setData(new ArrayList<Order>(0));
  }

  @Override
  public OrderListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_order, parent, false);

    return new OrderListViewHolder(view);
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

  private boolean isActionMode() {
    return mActionMode != null;
  }

  private void setActionMode(ActionMode actionMode) {
    mActionMode = actionMode;
  }

  private boolean isSelected(Order order) {
    return isActionMode() && mSelectedOrders.contains(order);
  }

  private void selectItem(int position) {
    Order order = getItem(position);

    if (isSelected(order)) {
      mSelectedOrders.remove(order);
    } else {
      mSelectedOrders.add(order);
    }

    notifyItemChanged(position);
  }

  private void removeSelectedItems() {
    mViewModel.deleteOrders(mSelectedOrders);
    mOrders.removeAll(mSelectedOrders);
  }

  @Override
  public void onItemClick(int position) {
    if (isActionMode()) {
      selectItem(position);
    } else {
      Order order = getItem(position);
      mViewModel.getOrderDetailCommand().setValue(order.getId());
    }
  }

  @Override
  public boolean onItemLongClick(int position) {
    if (isActionMode()) {
      return false;
    }

    setActionMode(((OrderListActivity) mContext).startSupportActionMode(this));
    selectItem(position);
    return true;
  }

  @Override
  public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    MenuInflater inflater = mode.getMenuInflater();
    inflater.inflate(R.menu.menu_order_list_action_mode, menu);

    mActionMode = mode;
    mSelectedOrders = new ArrayList<>(0);

    notifyDataSetChanged();
    return true;
  }

  @Override
  public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
    return false;
  }

  @Override
  public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_mode_delete:
        removeSelectedItems();
        mode.finish();
        return true;
    }

    return false;
  }

  @Override
  public void onDestroyActionMode(ActionMode mode) {
    mActionMode = null;
    mSelectedOrders = null;

    notifyDataSetChanged();
  }

  class OrderListViewHolder extends RecyclerView.ViewHolder implements OnClickListener,
      OnLongClickListener {

    private final CheckBox mSelectCheckBox;
    private final TextView mStoreNameTextView;
    private final TextView mOrderDateTextView;
    private final TextView mOrderQuantityTextView;
    private final TextView mOrderTerritoryTextView;

    OrderListViewHolder(View itemView) {
      super(itemView);

      mSelectCheckBox = itemView.findViewById(R.id.selectCheckBox);
      mStoreNameTextView = itemView.findViewById(R.id.storeNameTextView);
      mOrderDateTextView = itemView.findViewById(R.id.orderDateTextView);
      mOrderQuantityTextView = itemView.findViewById(R.id.orderQuantityTextView);
      mOrderTerritoryTextView = itemView.findViewById(R.id.orderTerritoryTextView);

      itemView.setOnClickListener(this);
      itemView.setOnLongClickListener(this);
    }

    void bindItem(Context c, @NonNull Order order) {
      mSelectCheckBox.setVisibility(isActionMode() ? View.VISIBLE : View.GONE);
      mSelectCheckBox.setChecked(isSelected(order));

      String storeName = order.getStoreName();
      mStoreNameTextView.setText(storeName);

      Date orderDate = order.getOrderDate();
      mOrderDateTextView.setText(StringUtils.formatSentOrderDate(c, orderDate));

      int orderQuantity = order.getOrderQuantity();
      mOrderQuantityTextView.setText(StringUtils.formatOrderQuantity(c, orderQuantity));

      String orderTerritory = order.getOrderTerritory();
      mOrderTerritoryTextView.setText(orderTerritory);
    }

    @Override
    public void onClick(View v) {
      onItemClick(getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View v) {
      return onItemLongClick(getAdapterPosition());
    }
  }
}