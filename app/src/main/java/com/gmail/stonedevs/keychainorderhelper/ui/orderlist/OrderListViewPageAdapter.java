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

package com.gmail.stonedevs.keychainorderhelper.ui.orderlist;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Add a class header comment!
 */
public class OrderListViewPageAdapter extends FragmentPagerAdapter {

  private List<OrderListFragment> fragmentList = new ArrayList<>(0);

  private int mTabCount;

  public OrderListViewPageAdapter(FragmentManager fm, int tabCount) {
    super(fm);

    for (int i = 0; i < tabCount; i++) {
      fragmentList.add(OrderListFragment.createInstance(i));
    }

    mTabCount = tabCount;
  }

  @Override
  public OrderListFragment getItem(int position) {
    return fragmentList.get(position);
  }

  @Override
  public int getCount() {
    return mTabCount;
  }
}
