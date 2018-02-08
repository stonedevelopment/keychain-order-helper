package com.gmail.stonedevs.keychainorderhelper.model.listener;

import android.view.View;

public interface OnRecyclerViewItemClickListener {

  void onClick(View view, int position);

  boolean onLongClick(View view, int position);
}
