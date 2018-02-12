package com.gmail.stonedevs.keychainorderhelper.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import com.gmail.stonedevs.keychainorderhelper.R;


/**
 * Created by Shane Stone on 2/10/2018.
 *
 * Email: stonedevs@gmail.com
 */

public class EditStoreNameDialogFragment extends DialogFragment {

  private static final String KEY_STORE_NAME = "store_name";

  private Listener mListener;

  public interface Listener {

    void onSave(String storeName);
  }

  public EditStoreNameDialogFragment() {
    //  required empty constructor
  }

  public static EditStoreNameDialogFragment createInstance(Bundle args, Listener listener) {
    EditStoreNameDialogFragment fragment = new EditStoreNameDialogFragment();
    fragment.setArguments(args);
    fragment.setListener(listener);
    return fragment;
  }

  private void setListener(Listener listener) {
    mListener = listener;
  }

  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new Builder(getActivity());
    builder.setTitle(R.string.dialog_title_edit_store_name);

    LayoutInflater inflater = getActivity().getLayoutInflater();

    @SuppressLint("InflateParams") final View inflateView = inflater
        .inflate(R.layout.dialog_edit_store_name, null);

    final EditText editStoreName = inflateView.findViewById(R.id.editStoreName);
    String storeName = getArguments().getString(getString(R.string.dialog_key_edit_store_name));
    editStoreName.setText(storeName);

    builder.setView(inflateView);

    builder.setPositiveButton(R.string.dialog_positive_button_edit_store_name,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            mListener.onSave(editStoreName.getText().toString());
          }
        });
    builder.setNegativeButton(R.string.dialog_negative_button_edit_store_name,
        new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
          }
        });

    return builder.create();
  }
}
