package com.gmail.stonedevs.keychainorderhelper;

import android.view.View;

public interface OnOrderItemClickListener {

  void onClick(View view, int position);

  boolean onLongClick(View view, int position);
}
