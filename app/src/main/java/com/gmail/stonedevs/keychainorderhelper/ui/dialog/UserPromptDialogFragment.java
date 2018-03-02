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
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;

public class UserPromptDialogFragment extends DialogFragment {

  private static final String TAG = UserPromptDialogFragment.class.getSimpleName();

  private static final String BUNDLE_KEY_TITLE = "title";
  private static final String BUNDLE_KEY_MESSAGE = "message";
  private static final String BUNDLE_KEY_HINT = "hint";
  private static final String BUNDLE_KEY_INPUT_TEXT = "input_text";
  private static final String BUNDLE_KEY_INPUT_TYPE = "input_type";

  private TextInputLayout mTextInputLayout;
  private TextInputEditText mEditText;
  private Button mSaveButton;
  private Button mCancelButton;

  private DialogListener mListener;

  private String mInputText;

  private boolean mCanceled;

  public interface DialogListener {

    void onContinue(@NonNull String inputText);

    void onCancel();
  }

  public UserPromptDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  public static UserPromptDialogFragment createInstance(int title, int message, int hint,
      int inputType,
      String inputText) {
    Bundle args = new Bundle();
    args.putInt(BUNDLE_KEY_TITLE, title);
    args.putInt(BUNDLE_KEY_MESSAGE, message);
    args.putInt(BUNDLE_KEY_HINT, hint);
    args.putInt(BUNDLE_KEY_INPUT_TYPE, inputType);
    args.putString(BUNDLE_KEY_INPUT_TEXT, inputText);

    UserPromptDialogFragment dialogFragment = new UserPromptDialogFragment();
    dialogFragment.setArguments(args);
    dialogFragment.setCancelable(false);
    return dialogFragment;
  }

  boolean didUserCancel() {
    return mCanceled;
  }

  String getInputText() {
    return mInputText;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    if (getDialog() != null) {
      dismiss();
    }

    Bundle args = getArguments();

    Builder builder = new Builder(getActivity());

    int title = args.getInt(BUNDLE_KEY_TITLE);
    builder.setTitle(title);

    @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater()
        .inflate(R.layout.dialog_prompt_with_edit_text, null);

    mSaveButton = view.findViewById(R.id.saveButton);
    mSaveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mCanceled = false;
        dismiss();
      }
    });

    mCancelButton = view.findViewById(R.id.cancelButton);
    mCancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mCanceled = true;
        dismiss();
      }
    });

    int message = args.getInt(BUNDLE_KEY_MESSAGE);
    TextView messageTextView = view.findViewById(R.id.messageTextView);
    messageTextView.setText(message);

    mTextInputLayout = view.findViewById(R.id.textInputLayout);
    mEditText = view.findViewById(R.id.editText);

    int hint = args.getInt(BUNDLE_KEY_HINT);
    mEditText.setHint(hint);

    int inputType = args.getInt(BUNDLE_KEY_INPUT_TYPE);
    mEditText.setInputType(inputType);

    mEditText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        mInputText = s.toString();

        if (TextUtils.isEmpty(s)) {
          mTextInputLayout.setErrorEnabled(true);
          mTextInputLayout
              .setError(getString(R.string.dialog_edit_text_error_user_prompt));
          mSaveButton.setEnabled(false);
        } else {
          mTextInputLayout.setErrorEnabled(false);
          mSaveButton.setEnabled(true);
        }
      }
    });

    //  nullify edit text to lazily trigger error layout
    //  or  set text to given arguments value.
    String inputText = args.getString(BUNDLE_KEY_INPUT_TEXT);
    mEditText.setText(inputText);

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
    if (didUserCancel()) {
      mListener.onCancel();
    } else {
      mListener.onContinue(mInputText);
    }
  }
}