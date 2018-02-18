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

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;

/**
 * TODO: Add a class header comment!
 */

public class NewOrderViewHolder extends RecyclerView.ViewHolder implements OnClickListener,
    OnLongClickListener {

  private final TextView mKeychainNameTextView;
  private final TextView mItemQuantityTextView;

  private OnRecyclerViewItemClickListener mListener;

  NewOrderViewHolder(View itemView, OnRecyclerViewItemClickListener listener) {
    super(itemView);

    mListener = listener;

    mKeychainNameTextView = itemView.findViewById(R.id.keychainNameTextView);
    mItemQuantityTextView = itemView.findViewById(R.id.itemQuantityTextView);
  }

  void bindItem(@NonNull NewOrderAdapterItem item) {
    mKeychainNameTextView.setText(item.getKeychainName());
    mItemQuantityTextView.setText(item.getItemQuantity());
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
