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

package com.gmail.stonedevs.keychainorderhelper.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.gmail.stonedevs.keychainorderhelper.R;

public class TerritoryDialogFragment extends DialogFragment {

  private static final String TAG = TerritoryDialogFragment.class.getSimpleName();

  private TextInputLayout mTextInputLayout;
  private TextInputEditText mEditText;
  private Button mSaveButton;
  private Button mCancelButton;

  private DialogListener mListener;

  private String mText;
  private boolean mSendOrderAfter;

  public interface DialogListener {

    void onDismissDialog(String text, boolean sendOrderAfter);
  }

  public TerritoryDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  public static TerritoryDialogFragment createInstance(Bundle args) {
    TerritoryDialogFragment dialogFragment = new TerritoryDialogFragment();
    dialogFragment.setArguments(args);
    return dialogFragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_territory);

    Bundle args = getArguments();

    mText = args.getString(getString(R.string.bundle_key_order_territory));
    mSendOrderAfter = args.getBoolean(getString(R.string.bundle_key_order_send_after));

    @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater()
        .inflate(R.layout.dialog_prompt_with_edit_text, null);

    mSaveButton = view.findViewById(R.id.saveButton);
    mSaveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String text = mEditText.getText().toString();

        if (!TextUtils.isEmpty(text)) {
          dismiss();
        }
      }
    });

    mCancelButton = view.findViewById(R.id.cancelButton);
    mCancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });

    mTextInputLayout = view.findViewById(R.id.textInputLayout);
    mEditText = view.findViewById(R.id.editText);
    mEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        mText = s.toString();

        if (TextUtils.isEmpty(s)) {
          mTextInputLayout.setErrorEnabled(true);
          mTextInputLayout
              .setError(getString(R.string.dialog_edit_text_error_territory));
          mSaveButton.setEnabled(false);
        } else {
          mTextInputLayout.setErrorEnabled(false);
          mSaveButton.setEnabled(true);
        }
      }
    });

    //  nullify edit text to lazily trigger error layout
    mEditText.setText(mText);

    builder.setView(view);

    return builder.create();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      mListener = (DialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(context.toString()
          + " must implement DialogListener");
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    mListener.onDismissDialog(mText, mSendOrderAfter);
  }
}