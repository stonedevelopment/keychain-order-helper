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

package com.gmail.stonedevs.keychainorderhelper.ui.orderdetail;

import static android.content.Intent.EXTRA_STREAM;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.ViewModelFactory;
import com.gmail.stonedevs.keychainorderhelper.db.entity.Order;
import com.gmail.stonedevs.keychainorderhelper.util.ActivityUtils;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class OrderDetailActivity extends AppCompatActivity implements
    OrderDetailUserInteractionListener {

  public static final int REQUEST_CODE = 1;
  public static final int REQUEST_CODE_ACTION_SEND = REQUEST_CODE + 1;

  public static final int RESULT_OK = RESULT_FIRST_USER;
  public static final int DELETE_RESULT_OK = RESULT_FIRST_USER + 1;
  public static final int EDIT_RESULT_OK = RESULT_FIRST_USER + 2;

  private OrderDetailViewModel mViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_order_detail);

    setupActionBar();

    setupViewFragment();

    // TODO: 2/12/2018 setupViewModel() to save ViewModel here

    subscribeToNavigationChanges();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == REQUEST_CODE_ACTION_SEND) {

    }
  }

  private void setupActionBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
    }
  }

  private void setupViewFragment() {
    OrderDetailFragment fragment = obtainViewFragment();

    ActivityUtils
        .replaceFragmentInActivity(getSupportFragmentManager(), fragment, R.id.fragment_container);
  }

  /**
   * Detect user interactions.
   */
  private void subscribeToNavigationChanges() {
    OrderDetailViewModel viewModel = obtainViewModel(this);

    //  This event fires when User clicks Resend Order button.
    viewModel.getSendOrderCommand().observe(this, new Observer<Void>() {
      @Override
      public void onChanged(@Nullable Void aVoid) {
        OrderDetailActivity.this.onResendOrder();
      }
    });
  }

  private OrderDetailFragment obtainViewFragment() {
    //  Get the requested order id.
    String orderId = getIntent().getStringExtra(getString(R.string.bundle_key_order_id));

    OrderDetailFragment fragment = (OrderDetailFragment) getSupportFragmentManager()
        .findFragmentById(R.id.fragment_container);

    if (fragment == null) {
      fragment = OrderDetailFragment.createInstance(this, orderId);
    }

    return fragment;
  }

  public static OrderDetailViewModel obtainViewModel(FragmentActivity activity) {
    // Use a Factory to inject dependencies into the ViewModel
    ViewModelFactory factory = ViewModelFactory.getInstance(activity.getApplication());

    return ViewModelProviders.of(activity, factory).get(OrderDetailViewModel.class);
  }

  @Override
  public void onResendOrder() {
    //  resend email
    try {
      Workbook workbook = WorkbookFactory.create(getAssets().open(
          getString(R.string.excel_template_filename)));

      Order order = mViewModel.order.get();

      File file = ExcelUtil
          .generateExcelFile(this, workbook, order.getStoreName(),
              order.getOrderDate(), mViewModel.items);

      sendOrderByEmail(file, order.getStoreName());
    } catch (ParseException | InvalidFormatException | IOException e) {
      e.printStackTrace();
    }
  }

  public void sendOrderByEmail(File file, String storeName) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory),
        getString(R.string.pref_key_rep_territory));

    Intent intent = new Intent(Intent.ACTION_SEND);

    // set the type to 'email'
    intent.setType("vnd.android.cursor.dir/email");

    //  set email address from preferences
    String sendtoEmail =
        BuildConfig.DEBUG ? getString(R.string.pref_debug_default_value_sendto_email)
            : getString(R.string.pref_default_value_sendto_email);
    String to[] = {sendtoEmail};
    intent.putExtra(Intent.EXTRA_EMAIL, to);

    // the attachment
    Uri path = Uri.fromFile(file);
    intent.putExtra(EXTRA_STREAM, path);

    // the mail subject
    String subject = String
        .format(getString(R.string.string_format_email_subject), repTerritory, storeName);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    //  the mail body
    String body = String
        .format(getString(R.string.intent_extra_text_body_send_order_by_email), storeName);
    intent.putExtra(Intent.EXTRA_TEXT, body);

    //  send email!
    Intent chooser = Intent
        .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

    if (intent.resolveActivity(getPackageManager()) != null) {
      mSendOrderByEmailFile = file;
      startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
    } else {
      closeFragment();
      Toast.makeText(this, R.string.toast_intent_send_order_by_email_no_supported_apps,
          Toast.LENGTH_LONG).show();
    }
  }

}