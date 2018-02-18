package com.gmail.stonedevs.keychainorderhelper.adapter;

import android.content.Context;
import android.support.v7.util.DiffUtil;
import android.support.v7.util.DiffUtil.Callback;
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
import org.apache.poi.ss.util.CellAddress;

public class KeychainAdapter extends RecyclerView.Adapter<KeychainViewHolder> {

  private final List<Keychain> mItems;

  public static class KeychainViewHolder extends RecyclerView.ViewHolder {

    private final TextView mNameText;
    private final TextView mQuantityText;

    KeychainViewHolder(View v) {
      super(v);

      mNameText = v.findViewById(R.id.keychainNameTextView);
      mQuantityText = v.findViewById(R.id.itemQuantityTextView);
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

    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new Callback() {
      @Override
      public int getOldListSize() {
        return 0;
      }

      @Override
      public int getNewListSize() {
        return 0;
      }

      @Override
      public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
      }

      @Override
      public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return false;
      }
    });

    diffResult.dispatchUpdatesTo(this);
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

  public Integer getItemQuantityTotal() {
    int total = 0;
    for (Keychain keychain : getItems()) {
      total += keychain.getQuantity();
    }
    return total;
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

  public List<Integer> resetItemQuantities() {
    List<Integer> quantities = new ArrayList<>(0);

    for (Keychain keychain : getItems()) {
      keychain.setQuantity(0);
      quantities.add(0);
    }

    notifyDataSetChanged();
    return quantities;
  }

  public void populateItems(Context c, List<Integer> orderQuantities) {
    //  Populate names by string-array
    mItems.clear();
    String[] names = c.getResources().getStringArray(R.array.excel_cell_values_names);
    String[] addresses = c.getResources().getStringArray(R.array.excel_cell_locations_quantities);

    //  if quantity cell values persisted through saveInstanceState, fill quantities
    //  else, fill with default 0
    if (orderQuantities != null && orderQuantities.size() == names.length) {
      for (int i = 0; i < names.length; i++) {
        int quantity = orderQuantities.get(i);
        mItems.add(new Keychain(names[i], quantity, new CellAddress(addresses[i])));
      }
    } else {
      for (int i = 0; i < names.length; i++) {
        mItems.add(new Keychain(names[i], 0, new CellAddress(addresses[i])));
      }
    }

    notifyDataSetChanged();
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
