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

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */

public class NewOrderAdapter extends RecyclerView.Adapter<NewOrderViewHolder> implements
    OnRecyclerViewItemClickListener {

  private List<NewOrderAdapterItem> mItems;

  public NewOrderAdapter() {
    setData(new ArrayList<NewOrderAdapterItem>(0));
  }

  @Override
  public NewOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_order, parent, false);

    return new NewOrderViewHolder(view, this);
  }

  @Override
  public void onBindViewHolder(NewOrderViewHolder holder, int position) {
    NewOrderAdapterItem item = getItem(position);
  }

  @Override
  public int getItemCount() {
    return mItems != null ? mItems.size() : 0;
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  private NewOrderAdapterItem getItem(int position) {
    return mItems.get(position);
  }

  void replaceData(List<NewOrderAdapterItem> items) {
    setData(items);
  }

  void setData(List<NewOrderAdapterItem> items) {
    mItems = items;
    notifyDataSetChanged();
  }

  @Override
  public void onItemClick(int position) {

  }

  @Override
  public boolean onItemLongClick(int position) {
    return false;
  }
}
