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

package com.gmail.stonedevs.keychainorderhelper.util;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import com.gmail.stonedevs.keychainorderhelper.BuildConfig;
import com.gmail.stonedevs.keychainorderhelper.R;
import com.gmail.stonedevs.keychainorderhelper.model.CompleteOrder;

/**
 * Helper class that provides methods pertaining to send action intents.
 */
public class EmailUtils {

  public static Intent createSendOrderEmailIntent(@NonNull Context context,
      @NonNull CompleteOrder order, @Nullable Uri uri) {
    String storeName = order.getStoreName();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String repTerritory = order.hasOrderTerritory() ? order.getOrderTerritory()
        : prefs.getString(context.getString(R.string.pref_key_rep_territory),
            context.getString(R.string.pref_error_default_value_rep_territory));

    Intent intent = new Intent(Intent.ACTION_SEND);

    // set the type to 'email'
    intent.setType("vnd.android.cursor.dir/email");

    //  set email address from preferences
    String sendtoEmail =
        BuildConfig.DEBUG ? context.getString(R.string.intent_extra_email_default_value_debug)
            : context.getString(R.string.intent_extra_email_default_value);
    String to[] = {sendtoEmail};
    intent.putExtra(Intent.EXTRA_EMAIL, to);

    //  the mail subject/body
    String subject, body;
    switch (order.getOrderType()) {
      case ORDER:
        subject = String.format(context.getString(
            R.string.intent_extra_subject_send_order_by_email), repTerritory, storeName);
        body = String
            .format(context.getString(R.string.intent_extra_text_body_send_order_by_email),
                storeName);
        break;
      case ACKNOWLEDGEMENT_WITH_ORDER:
        subject = String.format(context.getString(
            R.string.intent_extra_subject_send_order_acknowledgement_by_email),
            repTerritory, storeName);
        body = String.format(context.getString(
            R.string.intent_extra_text_body_send_order_acknowledgement_by_email_with_order_details),
            storeName, DateUtils.getRelativeDateTimeString(context, order.getOrderDate().getTime(),
                DateUtils.MINUTE_IN_MILLIS, DateUtils.WEEK_IN_MILLIS,
                DateUtils.FORMAT_NUMERIC_DATE));
        break;
      case ACKNOWLEDGEMENT:
        subject = String.format(context.getString(
            R.string.intent_extra_subject_send_order_acknowledgement_by_email),
            repTerritory, storeName);
        body = String.format(context.getString(
            R.string.intent_extra_text_body_send_order_acknowledgement_by_email), storeName);
        break;
      default:
        throw new RuntimeException("Invalid OrderType: " + order.getOrderType());
    }
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(Intent.EXTRA_TEXT, body);

    // the attachment
    intent.putExtra(Intent.EXTRA_STREAM, uri);

    return intent;
  }
}