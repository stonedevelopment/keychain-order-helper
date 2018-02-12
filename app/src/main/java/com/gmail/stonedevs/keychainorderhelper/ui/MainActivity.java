package com.gmail.stonedevs.keychainorderhelper.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.ui.neworder.NewOrderActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class MainActivity extends AppCompatActivity implements MainActivityNavigation {

  public static final String TAG = MainActivity.class.getSimpleName();

  private MainActivityViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupActionBar();

    setupViewFragment();

    setupViewModel();

    subscribeToViewModelEvents();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mViewModel.handleActivityResult(requestCode, resultCode);
  }

  @Override
  public void addNewOrder() {
    Intent intent = new Intent(this, NewOrderActivity.class);
    startActivityForResult(intent, NewOrderActivity.REQUEST_CODE);
  }

  @Override
  public void viewOrders() {
    mViewModel.getSnackbarMessage().setValue(R.string.snackbar_message_coming_soon);
  }

  public static MainActivityViewModel obtainViewModel(FragmentActivity activity) {
    //  Use a Factory to inject dependencies into the ViewModel.
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    MainActivityViewModel viewModel = ViewModelProviders.of(activity, factory)
        .get(MainActivityViewModel.class);

    return viewModel;
  }

  void setupActionBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }
  }

  void setupViewFragment() {
    MainActivityFragment fragment = (MainActivityFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      //  Create the fragment
      fragment = MainActivityFragment.createInstance();
      ActivityUtils
          .addFragmentInActivity(getSupportFragmentManager(), fragment, R.id.fragment_container);
    }
  }

  void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  void subscribeToViewModelEvents() {
    mViewModel.getNewOrderEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void _) {
        addNewOrder();
      }
    });

    mViewModel.getViewOrdersEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void _) {
        viewOrders();
      }
    });
  }


//  public void replaceFragmentWithPopAnimation(Fragment fragment) {
//    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//    transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left,
//        R.animator.slide_out_right, R.animator.slide_in_right);
//    transaction.replace(R.id.fragment_container, fragment, fragment.getTag());
//    transaction.addToBackStack(null);
//    transaction.commit();
//  }
}
