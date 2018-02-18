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

package com.gmail.stonedevs.keychainorderhelper.ui.dialog.requiredfields;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.R;

public class RequiredFieldsDialogFragment extends DialogFragment implements OnClickListener {

  private static final String TAG = RequiredFieldsDialogFragment.class.getSimpleName();

  private static final String KEY_REP_NAME = "rep_name";
  private static final String KEY_REP_TERRITORY = "rep_territory";

  private EditText mRepNameEditText;
  private EditText mRepTerritoryEditText;

  private RequiredFieldsDialogListener mListener;

  public interface RequiredFieldsDialogListener {

    void onSuccess(int resourceId);

    void onFail(int resourceId);
  }

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
    builder.setCancelable(false);

    View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_required_fields, null);

    mRepNameEditText = view.findViewById(R.id.editName);
    String repName = bundle.getString(getString(R.string.pref_key_rep_name));
    mRepNameEditText.setText(repName);

    mRepTerritoryEditText = view.findViewById(R.id.editTerritory);
    String repTerritory = bundle.getString(getString(R.string.pref_key_rep_territory));
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
      mListener = (RequiredFieldsDialogListener) context;
    } catch (ClassCastException e) {
      // The activity doesn't implement the interface, throw exception
      throw new ClassCastException(context.toString()
          + " must implement RequiredFieldsDialogListener");
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

    saveRequiredFields(nameText, territoryText);

    if (nameText.isEmpty() || territoryText.isEmpty()) {
      if (nameText.isEmpty() && territoryText.isEmpty()) {
        mListener.onFail(R.string.snackbar_dialog_required_fields_fail);
      } else {
        if (nameText.isEmpty()) {
          Toast.makeText(getActivity(), R.string.snackbar_dialog_required_fields_rep_name_empty, Toast.LENGTH_SHORT)
              .show();
          mListener.onFail(R.string.snackbar_dialog_required_fields_rep_name_empty);
        } else {
          mListener.onFail(R.string.snackbar_dialog_required_fields_rep_territory_empty);
        }
      }
    } else {
      Toast.makeText(getActivity(), R.string.toast_dialog_required_fields_success, Toast.LENGTH_SHORT)
          .show();
      mListener.onSuccess(R.string.toast_dialog_required_fields_success);
      this.dismiss();
    }
  }
}
