package com.gmail.stonedevs.keychainorderhelper.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.OrderAdapter.PreviousOrderHolder;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<PreviousOrderHolder> {

  private final Context mContext;

  private final OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;

  private final List<Order> mItems;

  public OrderAdapter(Context c, OnRecyclerViewItemClickListener listener) {
    mItems = new ArrayList<>(0);
    mContext = c;

    mOnRecyclerViewItemClickListener = listener;
  }

  @Override
  public PreviousOrderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_order, parent, false);

    return new PreviousOrderHolder(v, mOnRecyclerViewItemClickListener);
  }

  @Override
  public void onBindViewHolder(PreviousOrderHolder holder, int position) {
    Order item = mItems.get(position);
    holder.bindItem(getContext(), item);
  }

  @Override
  public int getItemCount() {
    return mItems.size();
  }

  private Context getContext() {
    return mContext;
  }

  public Order getItem(int position) {
    return mItems.get(position);
  }

  public void bindItems(List<Order> items) {
    mItems.clear();
    mItems.addAll(items);
    notifyDataSetChanged();
  }

  public static class PreviousOrderHolder extends RecyclerView.ViewHolder implements
      OnClickListener,
      OnLongClickListener {

    private final TextView mStoreNameText;
    private final TextView mOrderDateText;
    private final TextView mOrderTotalText;

    private OnRecyclerViewItemClickListener mOnRecyclerViewItemClickListener;

    PreviousOrderHolder(View v, OnRecyclerViewItemClickListener listener) {
      super(v);

      mStoreNameText = v.findViewById(R.id.storeNameText);
      mOrderDateText = v.findViewById(R.id.orderDateText);
      mOrderTotalText = v.findViewById(R.id.orderTotalText);

      v.setOnClickListener(this);
      v.setOnLongClickListener(this);

      mOnRecyclerViewItemClickListener = listener;
    }

    void bindItem(Context c, Order item) {
      mStoreNameText.setText(item.getStoreName());
      mOrderDateText.setText(
          item.getOrderDateText(new SimpleDateFormat(c.getString(R.string.string_date_layout),
              Locale.getDefault())));
      mOrderTotalText.setText(item.getOrderTotalText(c));
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
}
