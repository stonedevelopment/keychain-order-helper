package com.gmail.stonedevs.keychainorderhelper.view;

import static com.gmail.stonedevs.keychainorderhelper.view.ViewOrderFragment.TAG;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.OrderAdapter;
import com.gmail.stonedevs.keychainorderhelper.model.Order;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrderEntry;
import com.gmail.stonedevs.keychainorderhelper.model.json.JSONOrderEntryList;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import com.gmail.stonedevs.keychainorderhelper.util.JSONUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PreviousOrderFragment extends BackHandledFragment implements
    OnRecyclerViewItemClickListener {

  private TextView mNoOrdersTextView;
  private RecyclerView mRecyclerView;
  private OrderAdapter mAdapter;
  private LinearLayoutManager mLayoutManager;

  public PreviousOrderFragment() {
    // Required empty public constructor
  }

  public static PreviousOrderFragment newInstance() {
    PreviousOrderFragment fragment = new PreviousOrderFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    //  set variable values here
    ((MainActivity) getActivity())
        .setActionBarTitle(getString(R.string.action_bar_title_previousOrderFragment));
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_view_order_list, container, false);

    mRecyclerView = view.findViewById(R.id.listRecyclerView);

    mLayoutManager = new LinearLayoutManager(getActivity());
    mRecyclerView.setLayoutManager(mLayoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        mLayoutManager.getOrientation());
    mRecyclerView.addItemDecoration(dividerItemDecoration);

    //  Populate orders by fetching json data
    List<Order> items = new ArrayList<>(0);
    try {
      JSONOrderEntryList entryList = JSONUtil.getOrderEntryList(getActivity());
      List<JSONOrderEntry> entries = entryList.getOrderEntries();

      for (int i = entries.size() - 1; i >= 0; i--) {
        JSONOrderEntry entry = entryList.getEntry(i);
        String storeName = entry.getStoreName();
        String orderDate = entry.getOrderDate();
        ArrayList<Integer> orderQuantities = entry.getOrderQuantities();
        Integer orderTotal = entry.getOrderTotal();

        items.add(new Order(storeName, orderDate, orderQuantities, orderTotal));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    mAdapter = new OrderAdapter(getActivity(), this);
    mAdapter.bindItems(items);

    mRecyclerView.setAdapter(mAdapter);

    mNoOrdersTextView = view.findViewById(R.id.textNoOrders);

    if (mAdapter.getItemCount() > 0) {
      mNoOrdersTextView.setVisibility(View.GONE);
    } else {
      mRecyclerView.setVisibility(View.GONE);
    }

    return view;
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    //  update outState here

    super.onSaveInstanceState(outState);
  }

  @Override
  public boolean onBackPressed() {
    return false;
  }

  @Override
  public void onClick(View view, int position) {
    //  create ViewOrderFragment
    Order order = mAdapter.getItem(position);

    Log.d(TAG, "onClick: " + order.getOrderQuantities().toString());
    replaceFragmentWithPopAnimation(ViewOrderFragment.newInstance(getActivity(), order));
  }

  @Override
  public boolean onLongClick(View view, int position) {
    return false;
  }

  void replaceFragmentWithPopAnimation(Fragment fragment) {
    ((MainActivity) getActivity()).replaceFragmentWithPopAnimation(fragment);
  }
}
