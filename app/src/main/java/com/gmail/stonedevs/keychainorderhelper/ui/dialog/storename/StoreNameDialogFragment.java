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

package com.gmail.stonedevs.keychainorderhelper.ui.dialog.storename;

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
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.gmail.stonedevs.keychainorderhelper.R;

public class StoreNameDialogFragment extends DialogFragment implements OnClickListener {

  private static final String TAG = StoreNameDialogFragment.class.getSimpleName();

  private TextInputLayout mStoreNameTextInputLayout;
  private TextInputEditText mStoreNameEditText;

  private StoreNameDialogListener mListener;

  public StoreNameDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  public static StoreNameDialogFragment createInstance(Bundle args) {
    StoreNameDialogFragment dialogFragment = new StoreNameDialogFragment();
    dialogFragment.setArguments(args);
    return dialogFragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle bundle = getArguments();

    Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_store_name);

    @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater()
        .inflate(R.layout.dialog_store_name, null);

    String storeName = bundle.getString(getString(R.string.bundle_key_store_name));
    mStoreNameTextInputLayout = view.findViewById(R.id.storeNameTextInputLayout);
    mStoreNameEditText = view.findViewById(R.id.storeNameEditText);
    mStoreNameEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty()) {
          mStoreNameTextInputLayout.setError(getString(R.string.dialog_edit_text_error_store_name));
        }
      }
    });
    mStoreNameEditText.setText(storeName);

    Button saveButton = view.findViewById(R.id.saveButton);
    saveButton.setOnClickListener(this);

    builder.setView(view);

    return builder.create();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    // Verify that the host activity implements the callback interface
    try {
      // Instantiate the NoticeDialogListener so we can send events to the host
      mListener = (StoreNameDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(context.toString()
          + " must implement StoreNameDialogListener");
    }
  }

  @Override
  public void onClick(View v) {
    String nameText = mStoreNameEditText.getText().toString();

    if (nameText.isEmpty()) {
      mStoreNameTextInputLayout
          .setError(getString(R.string.dialog_edit_text_error_store_name));
    } else {
      mListener.onSave(nameText);
      dismiss();
    }
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    mListener.onCancel();
  }
}
