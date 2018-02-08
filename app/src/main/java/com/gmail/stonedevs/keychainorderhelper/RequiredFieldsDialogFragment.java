package com.gmail.stonedevs.keychainorderhelper;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RequiredFieldsDialogFragment extends DialogFragment {

  public static final String TAG = RequiredFieldsDialogFragment.class.getSimpleName();

  public RequiredFieldsDialogFragment() {
    //  Empty constructor required for DialogFragment
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle bundle = getArguments();

    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle("Required Fields");

    LayoutInflater inflater = getActivity().getLayoutInflater();

    @SuppressLint("InflateParams") final View inflateView = inflater
        .inflate(R.layout.dialog_no_settings, null);

    final EditText editName = inflateView.findViewById(R.id.editName);
    String repName = bundle.getString(getString(R.string.pref_key_rep_name));
    editName.setText(repName);

    final EditText editTerritory = inflateView.findViewById(R.id.editTerritory);
    String repTerritory = bundle.getString(getString(R.string.pref_key_rep_territory));
    editTerritory.setText(repTerritory);

    builder.setView(inflateView);

    builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        String nameText = editName.getText().toString();
        String territoryText = editTerritory.getText().toString();

        if (!nameText.isEmpty() && !territoryText.isEmpty()) {
          //  save to preferences
          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
          prefs.edit()
              .putString(getContext().getString(R.string.pref_key_rep_name), nameText)
              .apply();
          prefs.edit()
              .putString(getContext().getString(R.string.pref_key_rep_territory), territoryText)
              .apply();

          Toast.makeText(getContext(), "Settings saved successfully.", Toast.LENGTH_SHORT).show();
        } else {
          Toast
              .makeText(getContext(), "Settings not saved successfully, open Settings to complete.",
                  Toast.LENGTH_LONG)
              .show();
        }
      }
    });

    return builder.create();
  }
}
