package com.gmail.stonedevs.keychainorderhelper.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.Keychain;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;

public class ClickableKeychainAdapter extends KeychainAdapter {

  private static int mMinOrderQuantity;
  private static int mMaxOrderQuantity;

  private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener = new OnRecyclerViewItemClickListener() {
    @Override
    public void onClick(View view, int position) {
      Keychain item = getItem(position);

      int quantity = item.getQuantity();

      if (quantity < mMinOrderQuantity) {
        item.setQuantity(mMinOrderQuantity);
      } else if (quantity < mMaxOrderQuantity) {
        item.setQuantity(mMaxOrderQuantity);
      } else {
        item.setQuantity(0);
      }

      notifyItemChanged(position);
    }

    @Override
    public boolean onLongClick(View view, int position) {
      Keychain item = getItem(position);

      int quantity = item.getQuantity();

      if (quantity < mMaxOrderQuantity) {
        item.setQuantity(mMaxOrderQuantity);
      } else {
        item.setQuantity(0);
      }

      notifyItemChanged(position);
      return true;
    }
  };

  public static class ClickableKeychainViewHolder extends KeychainViewHolder implements
      OnClickListener,
      OnLongClickListener {

    private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;

    ClickableKeychainViewHolder(View v, OnRecyclerViewItemClickListener listener) {
      super(v);

      v.setOnClickListener(this);
      v.setOnLongClickListener(this);

      mOnRecyclerViewItemClickListener = listener;
    }

    @Override
    public void onClick(View v) {
      mOnRecyclerViewItemClickListener.onClick(v, getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View v) {
      return mOnRecyclerViewItemClickListener.onLongClick(v, getAdapterPosition());
    }
  }

  public ClickableKeychainAdapter(Context c) {
    super();

    //  set min/max quantity attributes
    mMinOrderQuantity = c.getResources().getInteger(R.integer.excel_min_order_quantity_value);
    mMaxOrderQuantity = c.getResources().getInteger(R.integer.excel_max_order_quantity_value);
  }

  @Override
  public ClickableKeychainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_keychain, parent, false);

    return new ClickableKeychainViewHolder(v, mOnRecyclerViewItemClickListener);
  }
}
