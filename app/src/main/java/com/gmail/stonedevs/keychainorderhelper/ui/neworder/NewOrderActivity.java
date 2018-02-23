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
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.ui.MainActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.storename.StoreNameDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.storename.StoreNameDialogListener;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class NewOrderActivity extends AppCompatActivity implements NewOrderNavigator,
    StoreNameDialogListener {

  private static final String TAG = NewOrderActivity.class.getSimpleName();

  public static final int REQUEST_CODE = MainActivity.REQUEST_CODE + 1;

  private static final int REQUEST_CODE_ACTION_SEND = REQUEST_CODE + 1;

  public static final int SENT_RESULT_OK = 1;

  private TextView mStoreNameTextView;
  private TextInputLayout mTextInputLayout;
  private TextInputEditText mStoreNameEditText;

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
      case R.id.action_edit_store_name:
        showEditStoreNameDialog();
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_ACTION_SEND) {
      finishWithResult(SENT_RESULT_OK);
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

    mStoreNameTextView = findViewById(R.id.storeNameTextView);
    mStoreNameTextView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mStoreNameTextView.setVisibility(View.GONE);
        mTextInputLayout.setVisibility(View.VISIBLE);
      }
    });

    mTextInputLayout = findViewById(R.id.storeNameTextInputLayout);
    mStoreNameEditText = findViewById(R.id.storeNameEditText);
    mStoreNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        Log.d(TAG, "onTextChanged: " + s);
      }

      @Override
      public void afterTextChanged(Editable s) {
        mStoreNameTextView.setText(s.toString());

        mTextInputLayout.setVisibility(View.GONE);
        mStoreNameTextView.setVisibility(View.VISIBLE);
      }
    });
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
          showConfirmSendOrderDialog();
        } else {
          mViewModel.getSnackBarMessenger()
              .setValue(R.string.snackbar_message_send_order_fail_incomplete);
        }
      }
    });
  }

  private void subscribeToViewModelEvents() {
    mViewModel.getEditStoreNameCommand().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
//        showEditStoreNameDialog(s);
      }
    });

    mViewModel.getUpdateUIStoreNameTextEvent().observe(this, new Observer<String>() {
      @Override
      public void onChanged(@Nullable String s) {
        updateActionBarTitle(s);
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
          mViewModel.getSnackBarMessenger()
              .setValue(R.string.snackbar_message_send_order_fail_no_supported_apps);
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

  void finishWithResult(int resultCode) {
    setResult(resultCode);
    finish();
  }

  private void updateActionBarTitle(String s) {
    mStoreNameTextView.setText(s);
    mStoreNameEditText.setText(s);

    if (s.isEmpty()) {
      mStoreNameTextView.setVisibility(View.GONE);
      mTextInputLayout.setVisibility(View.VISIBLE);
    }
  }

  public void showEditStoreNameDialog() {
//    showEditStoreNameDialog(mViewModel.getStoreName());
  }

  @Override
  public void showEditStoreNameDialog(String s) {
    Bundle args = new Bundle();

    //  Fill argument bundle with either provided, if debugging fill with default values :)
    if (s.isEmpty()) {
      args.putString(getString(R.string.bundle_key_store_name),
          BuildConfig.DEBUG ? getString(R.string.layout_edit_text_default_value_store_name)
              : s);
    } else {
      args.putString(getString(R.string.bundle_key_store_name), s);
    }

    //  Create instance of dialog fragment use to help User fill in the blanks.
    StoreNameDialogFragment dialogFragment = StoreNameDialogFragment
        .createInstance(args);

    //  Initializations complete, show that dialog!
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

  @Override
  public void onSave(String s) {
    mViewModel.setStoreName(s);
    updateActionBarTitle(s);
  }

  @Override
  public void onCancel() {
    //  do nothing, let User continue as they please.
  }
}