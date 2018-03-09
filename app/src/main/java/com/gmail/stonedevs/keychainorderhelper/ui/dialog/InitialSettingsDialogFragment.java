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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.gmail.stonedevs.keychainorderhelper.R;

public class InitialSettingsDialogFragment extends DialogFragment implements OnClickListener,
    TextWatcher {

  private static final String TAG = InitialSettingsDialogFragment.class.getSimpleName();

  private TextInputLayout mRepNameTextInputLayout;
  private TextInputEditText mRepNameEditText;
  private Button mSaveButton;

  private EditText mRepTerritoryEditText;

  private OnSaveListener mListener;

  public interface OnSaveListener {

    void onSave();
  }

  public InitialSettingsDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  public static InitialSettingsDialogFragment createInstance(Bundle args) {
    InitialSettingsDialogFragment dialogFragment = new InitialSettingsDialogFragment();
    dialogFragment.setArguments(args);
    return dialogFragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle bundle = getArguments();

    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_initial_settings);

    @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater()
        .inflate(R.layout.dialog_initial_settings, null);

    mSaveButton = view.findViewById(R.id.saveButton);
    mSaveButton.setOnClickListener(this);

    mRepNameTextInputLayout = view.findViewById(R.id.repNameTextInputLayout);
    mRepNameEditText = view.findViewById(R.id.repNameEditText);
    mRepNameEditText.addTextChangedListener(this);

    String repName = bundle.getString(getString(R.string.pref_key_rep_name));
    mRepNameEditText.setText(repName);

    String repTerritory = bundle.getString(getString(R.string.pref_key_rep_territory));
    mRepTerritoryEditText = view.findViewById(R.id.repTerritoryEditText);
    mRepTerritoryEditText.setText(repTerritory);

    builder.setView(view);

    return builder.create();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    try {
      mListener = (OnSaveListener) context;
    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnSaveListener");
    }
  }

  private void saveInitialSettings(String repName, String repTerritory) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    prefs.edit()
        .putString(getContext().getString(R.string.pref_key_rep_name), repName)
        .putString(getContext().getString(R.string.pref_key_rep_territory), repTerritory)
        .apply();
  }

  @Override
  public void onClick(View v) {
    String nameText = mRepNameEditText.getText().toString();
    String territoryText = mRepTerritoryEditText.getText().toString();

    if (!TextUtils.isEmpty(nameText)) {
      saveInitialSettings(nameText, territoryText);
      mListener.onSave();
      dismiss();
    }
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    //  do nothing
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    //  do nothing
  }

  @Override
  public void afterTextChanged(Editable s) {
    if (TextUtils.isEmpty(s)) {
      mRepNameTextInputLayout.setErrorEnabled(true);
      mRepNameTextInputLayout
          .setError(getString(R.string.dialog_field_error_initial_settings_rep_name_text));
      mSaveButton.setEnabled(false);
    } else {
      mRepNameTextInputLayout.setErrorEnabled(false);
      mSaveButton.setEnabled(true);
    }
  }
}