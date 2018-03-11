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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;
import com.gmail.stonedevs.keychainorderhelper.ui.SettingsActivity;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.UserPromptDialogFragment;
import com.gmail.stonedevs.keychainorderhelper.ui.dialog.UserPromptDialogFragment.UserPromptDialogListener;
import com.gmail.stonedevs.keychainorderhelper.ui.orderlist.OrderListActivity;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;

public class NewOrderActivity extends AppCompatActivity implements NewOrderNavigator,
    OnFocusChangeListener, UserPromptDialogListener {

  private static final String TAG = NewOrderActivity.class.getSimpleName();

  public static final int REQUEST_CODE = OrderListActivity.REQUEST_CODE + 1;

  public static final int REQUEST_CODE_ACTION_SEND = REQUEST_CODE + 1;

  //  RESULT_OK
  public static final int RESULT_SENT_ORDER_OK = RESULT_OK;
  public static final int RESULT_SENT_ACKNOWLEDGEMENT_OK = RESULT_SENT_ORDER_OK - 1;

  //  RESULT_CANCELED
  public static final int RESULT_SENT_CANCEL = RESULT_CANCELED;
  public static final int RESULT_SENT_ERROR_NO_APPS = RESULT_SENT_CANCEL + 1;
  public static final int RESULT_DATA_LOAD_ERROR = RESULT_SENT_ERROR_NO_APPS + 1;

  private TextInputLayout mTextInputLayout;
  private TextInputEditText mStoreNameEditText;
//  private TextView mOrderQuantityTextView;

  private NewOrderViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_order);

    setupActionBar();

    setupViewFragment();

    setupViewModel();

    subscribeToViewModelEvents();

    subscribeToViewModelCommands();
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
      case R.id.action_send_order:
        if (mViewModel.readyToSendOrder()) {
          mViewModel.initializeSendOrderPhase();

          if (mViewModel.hasTerritory()) {
            showConfirmSendOrderDialog();
          } else {
            showEditTerritoryDialog();
          }
        } else {
          showOrderRequirementsDialog();
        }
        return true;
      case R.id.action_send_acknowledgement:
        if (mViewModel.readyToSendAcknowledgment()) {
          mViewModel.initializeSendAcknowledgementPhase();

          if (mViewModel.hasTerritory()) {
            showConfirmSendAcknowledgementDialog();
          } else {
            showEditTerritoryDialog();
          }
        } else {
          showAcknowledgementRequirementsDialog();
        }
        return true;
      case R.id.action_edit_territory:
        showEditTerritoryDialog();
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
      if (mViewModel.isSendingOrder()) {
        finishWithResult(RESULT_SENT_ORDER_OK);
      } else if (mViewModel.isSendingAcknowledgement()) {
        finishWithResult(RESULT_SENT_ACKNOWLEDGEMENT_OK);
      } else {
        throw new RuntimeException("Invalid view model state.");
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public void onBackPressed() {
    showConfirmCancelOrderDialog();
  }

  @Override
  public boolean onSupportNavigateUp() {
    onBackPressed();
    return true;
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
        mViewModel.updateStoreName(s.toString());

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

  private void subscribeToViewModelCommands() {
  }

  private void subscribeToViewModelEvents() {
    mViewModel.getUpdateUIEvent().observe(this, new Observer<CompleteOrder>() {
      @Override
      public void onChanged(@Nullable CompleteOrder order) {
        mStoreNameEditText.setText(order.getStoreName());
      }
    });

    mViewModel.getErrorLoadingDataEvent().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        //  Data was not received properly.
        finishWithResult(RESULT_DATA_LOAD_ERROR);
      }
    });

    mViewModel.getIntentReadyEvent().observe(this, new Observer<Intent>() {
      @Override
      public void onChanged(@Nullable Intent intent) {
        if (mViewModel.isSendingOrder()) {
          sendOrderByEmail(intent);
        } else if (mViewModel.isSendingAcknowledgement()) {
          sendOrderAcknowledgementByEmail(intent);
        } else {
          throw new RuntimeException("View model state invalid for intent.");
        }
      }
    });
  }

  private NewOrderFragment obtainViewFragment() {
    NewOrderFragment fragment = (NewOrderFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      String orderId = getIntent().getStringExtra(getString(R.string.bundle_key_order_id));
      fragment = NewOrderFragment.createInstance(orderId);
    }

    return fragment;
  }

  static NewOrderViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(NewOrderViewModel.class);
  }

  private void validateEditText(Editable s) {
    if (TextUtils.isEmpty(s)) {
      if (!mTextInputLayout.isErrorEnabled()) {
        mTextInputLayout.setErrorEnabled(true);
      }

      mTextInputLayout.setError(getString(R.string.layout_edit_text_error_field_store_name));
    } else {
      mTextInputLayout.setErrorEnabled(false);
    }
  }

  void sendOrderByEmail(Intent intent) {
    Intent chooser = Intent
        .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
    } else {
      //  there are no apps on phone to handle this intent, cancel order
      finishWithResult(RESULT_SENT_ERROR_NO_APPS);
    }
  }

  void sendOrderAcknowledgementByEmail(Intent intent) {
    Intent chooser = Intent
        .createChooser(intent,
            getString(R.string.intent_title_send_order_acknowledgement_by_email));

    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
    } else {
      //  there are no apps on phone to handle this intent, cancel order
      finishWithResult(RESULT_SENT_ERROR_NO_APPS);
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

    StringBuilder message = new StringBuilder(
        getString(R.string.dialog_message_incomplete_order));

    if (mViewModel.isStoreNameEmpty()) {
      message.append(getString(R.string.dialog_message_incomplete_order_store_name_empty));
    }

    if (mViewModel.isOrderQuantityZero()) {
      message.append(getString(R.string.dialog_message_incomplete_order_keychains_empty));
    } else if (!mViewModel.doesOrderQuantityMeetMinimumRequirements()) {
      int minimum = getResources().getInteger(R.integer.order_quantity_minimum_requirement);
      int quantity = mViewModel.getOrderQuantity();
      int difference = minimum - quantity;

      message.append(String.format(getString(
          R.string.dialog_message_incomplete_order_keychains_minimum_not_met),
          difference, minimum));
    }
    builder.setMessage(message);

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
  public void showAcknowledgementRequirementsDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_incomplete_order_acknowledgement);

    StringBuilder message = new StringBuilder(
        getString(R.string.dialog_message_incomplete_order_acknowledgement));

    if (mViewModel.isStoreNameEmpty()) {
      message.append(
          getString(R.string.dialog_message_incomplete_order_acknowledgement_store_name_empty));
    }

    builder.setMessage(message);

    builder.setNegativeButton(R.string.dialog_negative_button_incomplete_order_acknowledgement,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            //  do nothing, allow the user to fix issues.
          }
        });
    builder.show();
  }

  @Override
  public void showEditTerritoryDialog() {
    int title = R.string.dialog_title_territory;
    int message = R.string.dialog_message_territory;
    int hint = R.string.dialog_edit_text_hint_territory;
    int inputType = InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
    String inputText = mViewModel.getTerritory();

    UserPromptDialogFragment dialogFragment = UserPromptDialogFragment
        .createInstance(title, message, hint, inputType, inputText);
    dialogFragment.show(getSupportFragmentManager(), UserPromptDialogFragment.TAG);
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
            finishWithResult(RESULT_SENT_CANCEL);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_cancel_order, null);
    builder.show();
  }

  @Override
  public void showConfirmSendOrderDialog() {
    AlertDialog.Builder builder = new Builder(this);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String repName = prefs.getString(getString(R.string.pref_key_rep_name), null);
    String repTerritory = mViewModel.getTerritory();
    String storeName = mViewModel.getStoreName();
    int orderQuantity = mViewModel.getOrderQuantity();
    String orderQuantityFormat = String
        .format(getString(R.string.string_format_list_item_order_total_text), orderQuantity);

    View view = View.inflate(this, R.layout.dialog_send_order, null);
    TextView repNameTextView = view.findViewById(R.id.repNameTextView);
    repNameTextView.setText(repName);
    TextView repTerritoryTextView = view.findViewById(R.id.repTerritoryTextView);
    repTerritoryTextView.setText(repTerritory);
    TextView storeNameTextView = view.findViewById(R.id.storeNameTextView);
    storeNameTextView.setText(storeName);
    TextView orderQuantityTextView = view.findViewById(R.id.orderQuantityTextView);
    orderQuantityTextView.setText(orderQuantityFormat);
    builder.setView(view);

    builder.setPositiveButton(R.string.dialog_positive_button_send_order,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.beginSendOrderPhase(NewOrderActivity.this);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_send_order, new OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mViewModel.endSendOrderPhase();
      }
    });
    builder.setCancelable(false);
    builder.show();
  }

  @Override
  public void showConfirmSendAcknowledgementDialog() {
    AlertDialog.Builder builder = new Builder(this);
    builder.setTitle(R.string.dialog_title_send_order_acknowledgement);
    builder.setMessage(getString(R.string.dialog_message_send_order_acknowledgement));
    builder.setPositiveButton(R.string.dialog_positive_button_send_order_acknowledgement,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.beginSendAcknowledgementPhase(NewOrderActivity.this);
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_send_order_acknowledgement,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mViewModel.endSendAcknowledgementPhase();
          }
        });
    builder.setCancelable(false);
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
   * Callback method called from {@link UserPromptDialogFragment}.
   *
   * Called when User presses Continue button. Set territory in View Model, then send if dialog was
   * started during the send process.
   */
  @Override
  public void onContinue(@NonNull String territory) {
    boolean dataChanged = mViewModel.updateTerritory(territory);

    //  If sending order state is true, show dialog to send order,
    //  Otherwise, show snackbar message to User. Only show if not already sending.
    if (mViewModel.isSendingOrder()) {
      showConfirmSendOrderDialog();
    } else if (mViewModel.isSendingAcknowledgement()) {
      showConfirmSendAcknowledgementDialog();
    } else {
      if (dataChanged) {
        mViewModel.getSnackBarMessenger().setValue(R.string.snackbar_message_changes_saved);
      } else {
        mViewModel.getSnackBarMessenger().setValue(R.string.snackbar_message_no_changes);
      }
    }
  }

  /**
   * Callback method from {@link UserPromptDialogFragment}.
   *
   * Called when User presses the Cancel button.
   */
  @Override
  public void onCancel() {
    mViewModel.getSnackBarMessenger().setValue(R.string.snackbar_message_no_changes);

    if (mViewModel.isSendingOrder()) {
      mViewModel.endSendOrderPhase();
    } else if (mViewModel.isSendingAcknowledgement()) {
      mViewModel.endSendAcknowledgementPhase();
    }
  }
}