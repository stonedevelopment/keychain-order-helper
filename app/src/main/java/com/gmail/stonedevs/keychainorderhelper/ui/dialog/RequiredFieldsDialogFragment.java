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
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.R;

public class RequiredFieldsDialogFragment extends DialogFragment implements OnClickListener {

  private static final String TAG = RequiredFieldsDialogFragment.class.getSimpleName();

  private TextInputLayout mRepNameTextInputLayout;
  private TextInputEditText mRepNameEditText;

  private TextInputLayout mRepTerritoryInputLayout;
  private TextInputEditText mRepTerritoryEditText;

  private OnDismissListener mListener;

  public RequiredFieldsDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  public static RequiredFieldsDialogFragment createInstance(Bundle args) {
    RequiredFieldsDialogFragment dialogFragment = new RequiredFieldsDialogFragment();
    dialogFragment.setArguments(args);
    return dialogFragment;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle bundle = getArguments();

    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_required_fields);

    @SuppressLint("InflateParams") View view = getActivity().getLayoutInflater()
        .inflate(R.layout.dialog_required_fields, null);

    String repName = bundle.getString(getString(R.string.pref_key_rep_name));
    mRepNameTextInputLayout = view.findViewById(R.id.repNameTextInputLayout);
    mRepNameEditText = view.findViewById(R.id.repNameEditText);
    mRepNameEditText.setText(repName);

    String repTerritory = bundle.getString(getString(R.string.pref_key_rep_territory));
    mRepTerritoryInputLayout = view.findViewById(R.id.repTerritoryTextInputLayout);
    mRepTerritoryEditText = view.findViewById(R.id.repTerritoryEditText);
    mRepTerritoryEditText.setText(repTerritory);

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
      mListener = (OnDismissListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(context.toString()
          + " must implement OnDismissListener");
    }
  }

  void saveRequiredFields(String repName, String repTerritory) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    prefs.edit()
        .putString(getContext().getString(R.string.pref_key_rep_name), repName)
        .apply();
    prefs.edit()
        .putString(getContext().getString(R.string.pref_key_rep_territory), repTerritory)
        .apply();
  }

  @Override
  public void onClick(View v) {
    String nameText = mRepNameEditText.getText().toString();
    String territoryText = mRepTerritoryEditText.getText().toString();

    if (nameText.isEmpty() || territoryText.isEmpty()) {
      if (nameText.isEmpty() && territoryText.isEmpty()) {
        mRepNameTextInputLayout.setError(getString(R.string.dialog_field_error_required_fields_rep_name_text));
        mRepTerritoryInputLayout
            .setError(getString(R.string.dialog_field_error_required_fields_rep_territory_text));
      } else {
        if (nameText.isEmpty()) {
          mRepNameTextInputLayout.setError(getString(R.string.dialog_field_error_required_fields_rep_name_text));
          mRepTerritoryInputLayout.setError(null);
        } else {
          mRepNameTextInputLayout.setError(null);
          mRepTerritoryInputLayout
              .setError(getString(R.string.dialog_field_error_required_fields_rep_territory_text));
        }
      }
    } else {
      saveRequiredFields(nameText, territoryText);

      Toast.makeText(getActivity(), R.string.toast_dialog_required_fields_success,
          Toast.LENGTH_SHORT).show();

      dismiss();
    }
  }

  @Override
  public void onDismiss(DialogInterface dialog) {
    mListener.onDismiss(dialog);
  }
}
