package com.gmail.stonedevs.keychainorderhelper.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.gmail.stonedevs.keychainorderhelper.R;

public class RequiredFieldsDialogFragment extends DialogFragment {

  public static final String TAG = RequiredFieldsDialogFragment.class.getSimpleName();

  private static final String KEY_REP_NAME = "rep_name";
  private static final String KEY_REP_TERRITORY = "rep_territory";

  private OnRequiredFieldsCheckListener mListener;

  public interface OnRequiredFieldsCheckListener {

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

  public void setListener(OnRequiredFieldsCheckListener listener) {
    mListener = listener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Bundle bundle = getArguments();

    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_required_fields);

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

    builder.setPositiveButton(R.string.dialog_positive_button_required_fields,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            String nameText = editName.getText().toString();
            String territoryText = editTerritory.getText().toString();

            saveRequiredFields(nameText, territoryText);

            if (nameText.isEmpty() || territoryText.isEmpty()) {
              if (nameText.isEmpty() && territoryText.isEmpty()) {
                mListener.onFail(R.string.toast_dialog_required_fields_fail);
              } else {
                if (nameText.isEmpty()) {
                  mListener.onFail(R.string.toast_dialog_required_fields_rep_name_empty);
                } else {
                  mListener.onFail(R.string.toast_dialog_required_fields_rep_territory_empty);
                }
              }
            } else {
              mListener.onSuccess(R.string.toast_dialog_required_fields_success);
            }
          }
        });

    return builder.create();
  }

  @Override
  public void onCancel(DialogInterface dialog) {
    mListener.onFail(R.string.toast_dialog_required_fields_fail);
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
}
