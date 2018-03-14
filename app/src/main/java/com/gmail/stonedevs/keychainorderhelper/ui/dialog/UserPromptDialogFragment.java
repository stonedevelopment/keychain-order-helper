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

package com.gmail.stonedevs.keychainorderhelper.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.gmail.stonedevs.keychainorderhelper.R;

/**
 * Abstract implementation of a {@link DialogFragment} that prompts User to input a value into a
 * {@link EditText} component.
 */
public class UserPromptDialogFragment extends DialogFragment {

  //  Tag used by calling activity in creating an instance of this class.
  public static final String TAG = UserPromptDialogFragment.class.getSimpleName();

  //  Bundle keys
  private static final String BUNDLE_KEY_TITLE = "title";
  private static final String BUNDLE_KEY_MESSAGE = "message";
  private static final String BUNDLE_KEY_HINT = "hint";
  private static final String BUNDLE_KEY_INPUT_TEXT = "input_text";
  private static final String BUNDLE_KEY_INPUT_TYPE = "input_type";

  //  Layout objects
  private TextInputLayout mTextInputLayout;
  private TextInputEditText mEditText;
  private Button mPositiveButton;
  private Button mNegativeButton;

  //  Listeners
  private UserPromptDialogListener mListener;

  //  The text that User inputs.
  private String mInputText;

  /**
   * Listener of non-cancelable dialog that reacts to whether User presses the continue (positive)
   * button or cancel (negative) button.
   */
  public interface UserPromptDialogListener {

    /**
     * Called after User presses the continue (positive) button.
     *
     * @param inputText The text that User has given us.
     */
    void onPositiveButtonClicked(@NonNull String inputText);

    /**
     * Called after User presses the cancel (negative) button.
     */
    void onNegativeButtonClicked();
  }

  /**
   * Required default constructor.
   */
  public UserPromptDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  /**
   * Creates an instance of the dialog fragment by its required parameters.
   *
   * @param title String resource value of the dialog's title.
   * @param message String resource value of the dialog's default message.
   * @param hint String resource value of the dialog's editText's hint message.
   * @param inputType {@link InputType} used by the dialog's {@link EditText}
   * @param inputText The text to qualify the dialog's {@link EditText} with, can be null.
   * @return The created {@link UserPromptDialogFragment} instance.
   */
  public static UserPromptDialogFragment createInstance(@StringRes int title,
      @StringRes int message, @StringRes int hint,
      int inputType, @Nullable String inputText) {
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

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {

    //  If there's a dialog to show, return it.
    if (getDialog() != null) {
      return getDialog();
    }

    Bundle args = getArguments();

    Builder builder = new Builder(getActivity());

    int title = args.getInt(BUNDLE_KEY_TITLE);
    builder.setTitle(title);

    @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater()
        .inflate(R.layout.dialog_prompt_with_edit_text, null);

    mPositiveButton = view.findViewById(R.id.positiveButton);
    mPositiveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mListener.onPositiveButtonClicked(mInputText);
        dismiss();
      }
    });

    mNegativeButton = view.findViewById(R.id.negativeButton);
    mNegativeButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        mListener.onNegativeButtonClicked();
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
          mPositiveButton.setEnabled(false);
        } else {
          mTextInputLayout.setErrorEnabled(false);
          mPositiveButton.setEnabled(true);
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
      // Instantiate the DialogListener so we can send events to the host
      mListener = (UserPromptDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(context.toString()
          + " must implement UserPromptDialogListener");
    }
  }
}