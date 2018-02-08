package com.gmail.stonedevs.keychainorderhelper;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.OrderAdapter.OrderHolder;
import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderHolder> {

  //  private static int mDefaultQuantity;
  private static int mMinOrderQuantity;
  private static int mMaxOrderQuantity;

  private OnOrderItemClickListener mOnOrderItemClickListener;

  private List<OrderItem> mItems = new ArrayList<>();

  public static class OrderHolder extends RecyclerView.ViewHolder implements OnClickListener,
      OnLongClickListener {

    private final TextView mName;
    private final TextView mQuantity;

    private OnOrderItemClickListener mOnOrderItemClickListener;

    OrderHolder(View v, OnOrderItemClickListener listener) {
      super(v);

      mName = v.findViewById(R.id.nameText);
      mQuantity = v.findViewById(R.id.quantityText);

      v.setOnClickListener(this);
      v.setOnLongClickListener(this);

      mOnOrderItemClickListener = listener;
    }

    void bindItem(OrderItem item) {
      mName.setText(item.getName());

      int quantity = item.getQuantity();
      if (quantity > 0) {
        mQuantity.setText(String.valueOf(quantity));
      } else {
        mQuantity.setText("");
      }
    }

    @Override
    public void onClick(View v) {
      mOnOrderItemClickListener.onClick(v, getAdapterPosition());
    }

    @Override
    public boolean onLongClick(View v) {
      return mOnOrderItemClickListener.onLongClick(v, getAdapterPosition());
    }
  }

  OrderAdapter(Context c) {
    //  set min/max quantity attributes
    mMinOrderQuantity = c.getResources().getInteger(R.integer.excel_min_order_quantity_value);
    mMaxOrderQuantity = c.getResources().getInteger(R.integer.excel_max_order_quantity_value);

    //  set default quantity for later use
//    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);
//    mDefaultQuantity = Integer
//        .parseInt(prefs.getString(c.getString(R.string.pref_key_default_order_quantity),
//            c.getResources().getString(R.string.pref_default_value_default_order_quantity)));

    mOnOrderItemClickListener = new OnOrderItemClickListener() {
      @Override
      public void onClick(View view, int position) {
        OrderItem item = getItem(position);

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
        OrderItem item = getItem(position);

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
  }

  void bindItems(List<OrderItem> items) {
    mItems.clear();
    mItems.addAll(items);
    notifyDataSetChanged();
  }

  @Override
  public OrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_order, parent, false);

    return new OrderHolder(v, mOnOrderItemClickListener);
  }

  @Override
  public void onBindViewHolder(OrderHolder holder, int position) {
    OrderItem item = mItems.get(position);
    holder.bindItem(item);
  }

  @Override
  public int getItemCount() {
    return mItems.size();
  }

  OrderItem getItem(int position) {
    return mItems.get(position);
  }
}
