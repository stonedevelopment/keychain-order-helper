/*
 * Copyright (c) 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.gmail.stonedevs.keychainorderhelper.ui.neworder;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.SettingsActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.TerritoryDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.TerritoryDialogFragment.DialogListener;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class NewOrderActivity extends AppCompatActivity implements NewOrderNavigator,
    OnFocusChangeListener, DialogListener {

  private static final String TAG = NewOrderActivity.class.getSimpleName();

  public static final int REQUEST_CODE = MainActivity.REQUEST_CODE + 1;
  public static final int RESULT_SENT_ERROR_NO_APPS = RESULT_FIRST_USER + 1;

  private static final int REQUEST_CODE_ACTION_SEND = REQUEST_CODE + 1;

  private TextInputLayout mTextInputLayout;
  private TextInputEditText mStoreNameEditText;
//  private TextView mOrderQuantityTextView;

  private NewOrderViewModel mViewModel;

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_order);

    setupActionBar();

    setupViewFragment();

    setupViewModel();

    subscribeToViewModelEvents();

    subscribeToNavigationChanges();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_new_order, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_reset_order:
        mViewModel.getResetOrderCommand().call();
        return true;
      case R.id.action_send:
        mViewModel.getSendOrderCommand().call();
        return true;
      case R.id.action_settings:
        startActivity(new Intent(this, SettingsActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_ACTION_SEND) {
      finishWithResult(RESULT_OK);
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void onBackPressed() {
    mViewModel.getCancelOrderCommand().call();
  }

  private void setupActionBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }

    mTextInputLayout = findViewById(R.id.storeNameTextInputLayout);
    mStoreNameEditText = findViewById(R.id.storeNameEditText);
    mStoreNameEditText.setOnFocusChangeListener(this);
    mStoreNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        mViewModel.setStoreName(s.toString());

        validateEditText(s);
      }
    });

//    mOrderQuantityTextView = findViewById(R.id.orderQuantityTextView);
  }

  private void setupViewFragment() {
    NewOrderFragment fragment = obtainViewFragment();

    ActivityUtils
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, R.id.fragment_container);
  }

  private void setupViewModel() {
    mViewModel = obtainViewModel(this);
  }

  private void subscribeToNavigationChanges() {
    mViewModel.getCancelOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        showConfirmCancelOrderDialog();
      }
    });

    mViewModel.getResetOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        showConfirmResetOrderDialog();
      }
    });

    mViewModel.getPrepareOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        mViewModel.executeFinalPreparations(NewOrderActivity.this);
      }
    });

    mViewModel.getSendOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        if (mViewModel.isReady()) {
          if (mViewModel.hasTerritory()) {
            showConfirmSendOrderDialog();
          } else {
            showTerritoryDialog();
          }
        } else {
          if (mViewModel.isStoreNameEmpty()) {
            mStoreNameEditText.requestFocus();

            InputMethodManager imm = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mStoreNameEditText, InputMethodManager.SHOW_IMPLICIT);
          } else {
            showOrderRequirementsDialog();
          }
        }
      }
    });
  }

  private void subscribeToViewModelEvents() {
    mViewModel.getUpdateUIEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        mStoreNameEditText.setText(order.getStoreName());
      }
    });

    mViewModel.getIntentReadyEvent().observe(this, new Observer<Intent>() {
      @Override
      public void onChanged(@Nullable Intent intent) {
        Intent chooser = Intent
            .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

        if (intent.resolveActivity(getPackageManager()) != null) {
          startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
        } else {
          //  there are no apps on phone to handle this intent, cancel order
          finishWithResult(RESULT_SENT_ERROR_NO_APPS);
        }
      }
    });
  }

  private NewOrderFragment obtainViewFragment() {
    NewOrderFragment fragment = (NewOrderFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = NewOrderFragment.createInstance();
    }

    return fragment;
  }

  public static NewOrderViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(NewOrderViewModel.class);
  }

  private void validateEditText(Editable s) {
    if (TextUtils.isEmpty(s)) {
      if (!mTextInputLayout.isErrorEnabled()) {
        mTextInputLayout.setErrorEnabled(true);
      }

      mTextInputLayout.setError(getString(R.string.layout_edit_text_hint_store_name));
    } else {
      mTextInputLayout.setErrorEnabled(false);
    }
  }

  void finishWithResult(int resultCode) {
    setResult(resultCode);
    finish();
  }

  @Override
  public void showOrderRequirementsDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_incomplete_order);

    StringBuilder stringBuilder = new StringBuilder(
        getString(R.string.dialog_message_incomplete_order));

    if (mViewModel.isOrderQuantityZero()) {
      stringBuilder.append(getString(R.string.dialog_message_incomplete_order_keychains_empty));
    } else if (!mViewModel.doesOrderQuantityMeetMinimumRequirements()) {
      int minimum = getResources().getInteger(R.integer.order_quantity_minimum_requirement);
      int quantity = mViewModel.getOrderQuantity();
      int difference = minimum - quantity;

      stringBuilder.append(String.format(getString(
          R.string.dialog_message_incomplete_order_keychains_minimum_not_met),
          difference, minimum));
    }
    builder.setMessage(stringBuilder);

    builder.setNegativeButton(R.string.dialog_negative_button_incomplete_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to fix issues.
          }
        });
    builder.show();
  }

  @Override
  public void showTerritoryDialog() {
    TerritoryDialogFragment dialogFragment = TerritoryDialogFragment.createInstance(new Bundle());
    dialogFragment.show(getSupportFragmentManager(), dialogFragment.getTag());
  }

  @Override
  public void showConfirmCancelOrderDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_cancel_order);
    builder.setMessage(R.string.dialog_message_cancel_order);
    builder.setPositiveButton(R.string.dialog_positive_button_cancel_order,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            finishWithResult(RESULT_CANCELED);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_cancel_order,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to continue their order.
          }
        });
    builder.show();
  }

  @Override
  public void showConfirmResetOrderDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_reset_order);
    builder.setMessage(R.string.dialog_message_reset_order);
    builder.setPositiveButton(R.string.dialog_positive_button_reset_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.resetOrder();
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_reset_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to continue their order.
          }
        });
    builder.show();
  }

  @Override
  public void showConfirmSendOrderDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_send_order);
    builder.setMessage(R.string.dialog_message_send_order);
    builder.setPositiveButton(R.string.dialog_positive_button_send_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.prepareToSendOrder();
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_send_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to continue their order.
          }
        });
    builder.show();
  }

  /**
   * Override method to fix annoying display bug where the edit text doesn't close keyboard.
   */
  @Override
  public void onFocusChange(View v, boolean hasFocus) {
    if (!hasFocus) {
      InputMethodManager mImMan = (InputMethodManager) getSystemService(
          Context.INPUT_METHOD_SERVICE);
      if (mImMan != null) {
        mImMan.hideSoftInputFromWindow(v.getWindowToken(), 0);
      }
    }
  }

  /**
   * Callback method called from {@link TerritoryDialogFragment}.
   *
   * Called when User presses Continue, or canceling. If {@param territory} is not empty, save to
   * view model and trigger send order commmand again.
   */
  @Override
  public void onDismissDialog(String territory) {
    if (!TextUtils.isEmpty(territory)) {
      //  save Territory to view model
      mViewModel.setTerritory(territory);

      //  re-call send order command to start process again.
      mViewModel.getSendOrderCommand().call();
    }
  }
}