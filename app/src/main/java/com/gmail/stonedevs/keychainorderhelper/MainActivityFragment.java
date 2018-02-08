package com.gmail.stonedevs.keychainorderhelper;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.gmail.stonedevs.keychainorderhelper.util.ExcelUtil;
import com.gmail.stonedevs.keychainorderhelper.view.NewOrderFragment;
import com.gmail.stonedevs.keychainorderhelper.view.PreviousOrderFragment;
import com.gmail.stonedevs.keychainorderhelper.view.RequiredFieldsDialogFragment;
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class MainActivityFragment extends Fragment {

  public MainActivityFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.app_name));

    View view = inflater.inflate(R.layout.fragment_main, container, false);

    Button orderButton = view.findViewById(R.id.
        orderButton);
    orderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

        String repName = prefs.getString(getString(R.string.pref_key_rep_name), "");
        String repTerritory = prefs
            .getString(getString(R.string.pref_key_rep_territory), "");

        //  if required fields are empty, open dialog for easy editing
        //  else, send user to order fragment, they followed instructions.
        if (repName.isEmpty() || repTerritory.isEmpty()) {
            RequiredFieldsDialogFragment dialogFragment = new RequiredFieldsDialogFragment();

            Bundle bundle = new Bundle();
            bundle.putString(getString(R.string.pref_key_rep_name), repName);
            bundle.putString(getString(R.string.pref_key_rep_territory), repTerritory);
            dialogFragment.setArguments(bundle);

            dialogFragment.show(getFragmentManager(), dialogFragment.getTag());
        } else {
          replaceFragmentWithPopAnimation(NewOrderFragment.newInstance());
        }
      }
    });

    Button viewOrderButton = view.findViewById(R.id.
        viewOrderButton);
    viewOrderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        replaceFragmentWithPopAnimation(PreviousOrderFragment.newInstance());
      }
    });

//    Button generateButton = view.findViewById(R.id.generateButton);
//    if (BuildConfig.DEBUG) {
//      generateButton.setVisibility(View.VISIBLE);
//      generateButton.setOnClickListener(new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//          //  Generate
//          try {
//            ExcelUtil.GenerateStringArrayFormat(getActivity());
//          } catch (InvalidFormatException | IOException e) {
//            e.printStackTrace();
//          }
//        }
//      });
//    }

    Button settingsButton = view.findViewById(R.id.settingsButton);
    settingsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
      }
    });

    return view;
  }

  void replaceFragmentWithPopAnimation(Fragment fragment) {
    ((MainActivity) getActivity()).replaceFragmentWithPopAnimation(fragment);
  }
}
