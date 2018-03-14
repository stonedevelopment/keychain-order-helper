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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.db.entity.OrderItem;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailAdapter extends RecyclerView.Adapter<OrderDetailViewHolder> {

  private List<OrderItem> mItems;

  OrderDetailAdapter() {
    setData(new ArrayList<OrderItem>(0));
  }

  @Override
  public OrderDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_keychain, parent, false);

    return new OrderDetailViewHolder(view);
  }

  @Override
  public void onBindViewHolder(OrderDetailViewHolder holder, int position) {
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

  void setData(List<OrderItem> items) {
    mItems = items;
    notifyDataSetChanged();
  }
}
