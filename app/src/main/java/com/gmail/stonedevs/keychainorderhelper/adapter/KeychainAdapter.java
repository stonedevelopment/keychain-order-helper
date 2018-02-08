package com.gmail.stonedevs.keychainorderhelper.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.KeychainAdapter.KeychainViewHolder;
import com.gmail.stonedevs.keychainorderhelper.model.Keychain;
import java.util.ArrayList;
import java.util.List;

public class KeychainAdapter extends RecyclerView.Adapter<KeychainViewHolder> {

  private final List<Keychain> mItems;

  public static class KeychainViewHolder extends RecyclerView.ViewHolder {

    private final TextView mNameText;
    private final TextView mQuantityText;

    KeychainViewHolder(View v) {
      super(v);

      mNameText = v.findViewById(R.id.nameText);
      mQuantityText = v.findViewById(R.id.quantityText);
    }

    void bindItem(Keychain item) {
      mNameText.setText(item.getName());

      int quantity = item.getQuantity();
      if (quantity > 0) {
        mQuantityText.setText(String.valueOf(quantity));
      } else {
        mQuantityText.setText("");
      }
    }
  }

  public KeychainAdapter() {
    mItems = new ArrayList<>(0);
  }

  public Keychain getItem(int position) {
    return mItems.get(position);
  }

  public List<Keychain> getItems() {
    return mItems;
  }

  public void bindItems(List<Keychain> items) {
    mItems.clear();
    mItems.addAll(items);
    notifyDataSetChanged();
  }

  public ArrayList<Integer> getItemQuantities() {
    ArrayList<Integer> quantities = new ArrayList<>(0);

    for (int i = 0; i < getItemCount(); i++) {
      Keychain item = getItem(i);
      if (item != null) {
        quantities.add(item.getQuantity());
      }
    }

    return quantities;
  }

  public boolean areItemQuantitiesEmpty() {
    for (int i = 0; i < getItemCount(); i++) {
      Keychain item = getItem(i);
      if (item != null) {
        if (item.getQuantity() > 0) {
          return false;
        }
      }
    }

    return true;
  }

  @Override
  public KeychainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View v = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_keychain, parent, false);

    return new KeychainViewHolder(v);
  }

  @Override
  public void onBindViewHolder(KeychainViewHolder holder, int position) {
    Keychain item = mItems.get(position);
    holder.bindItem(item);
  }

  @Override
  public int getItemCount() {
    return mItems.size();
  }
}
