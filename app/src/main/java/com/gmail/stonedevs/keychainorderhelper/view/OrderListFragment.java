package com.gmail.stonedevs.keychainorderhelper.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.adapter.OrderAdapter;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.model.listener.OnRecyclerViewItemClickListener;
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.viewmodel.OrderListViewModel;
import java.util.List;

public class OrderListFragment extends BackHandledFragment implements
    OnRecyclerViewItemClickListener {

  private OrderListViewModel mViewModel;
  private OrderAdapter mAdapter;

  public OrderListFragment() {
    // Required empty public constructor
  }

  public static OrderListFragment newInstance() {
    OrderListFragment fragment = new OrderListFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mAdapter = new OrderAdapter(getActivity(), this);

    //  set variable values here
    mViewModel = ViewModelProviders.of(getActivity()).get(OrderListViewModel.class);
    mViewModel.getOrderList().observe(getActivity(), new Observer<List<Order>>() {
      @Override
      public void onChanged(@Nullable List<Order> orderEntities) {
        mAdapter.bindItems(orderEntities);
      }
    });
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {

    //  set title of action bar
    ((MainActivity) getActivity())
        .setActionBarTitle(getString(R.string.action_bar_title_orderListFragment));

    // Inflate the layout for this fragment
    View view = inflater.inflate(R.layout.fragment_view_order_list, container, false);

    RecyclerView recyclerView = view.findViewById(R.id.listRecyclerView);

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    recyclerView.setLayoutManager(layoutManager);

    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
        layoutManager.getOrientation());
    recyclerView.addItemDecoration(dividerItemDecoration);

    recyclerView.setAdapter(mAdapter);

    TextView textNoOrders = view.findViewById(R.id.textNoOrders);

    if (mAdapter.getItemCount() > 0) {
      textNoOrders.setVisibility(View.GONE);
    } else {
      recyclerView.setVisibility(View.GONE);
    }

    return view;
  }

  @Override
  public boolean onBackPressed() {
    return false;
  }

  @Override
  public void onClick(View view, int position) {
    //  create ViewOrderFragment
    Order order = mAdapter.getItem(position);
    replaceFragmentWithPopAnimation(ViewOrderFragment.newInstance(order.getId()));
  }

  @Override
  public boolean onLongClick(View view, int position) {
    return false;
  }

  void replaceFragmentWithPopAnimation(Fragment fragment) {
    ((MainActivity) getActivity()).replaceFragmentWithPopAnimation(fragment);
  }
}
