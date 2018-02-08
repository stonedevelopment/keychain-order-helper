package com.gmail.stonedevs.keychainorderhelper;

import android.app.Fragment;
import android.app.FragmentTransaction;
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
import java.io.IOException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;

public class MainActivityFragment extends Fragment {

  public MainActivityFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    Button orderButton = view.findViewById(R.id.
        orderButton);
    orderButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        OrderFragment orderFragment = OrderFragment.newInstance();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_right, 0, 0);
        transaction.replace(R.id.fragment_container, orderFragment, OrderFragment.TAG);
        transaction.addToBackStack(null);
        transaction.commit();
      }
    });

    Button generateButton = view.findViewById(R.id.generateButton);
    if (BuildConfig.DEBUG) {
      generateButton.setVisibility(View.VISIBLE);
      generateButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          //  Generate
          try {
            ExcelUtil.GenerateStringArrayFormat(getActivity());
          } catch (InvalidFormatException | IOException e) {
            e.printStackTrace();
          }
        }
      });
    }

    Button settingsButton = view.findViewById(R.id.settingsButton);
    settingsButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(getActivity(), SettingsActivity.class));
      }
    });

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    String defaultRepName = BuildConfig.DEBUG ? "Chuck Norris" : "";
    String defaultTerritory = BuildConfig.DEBUG ? "LIT3" : "";
    String repName = prefs.getString(getString(R.string.pref_key_rep_name), defaultRepName);
    String repTerritory = prefs
        .getString(getString(R.string.pref_key_rep_territory), defaultTerritory);

    if (repName.isEmpty() || repTerritory.isEmpty()) {
      RequiredFieldsDialogFragment dialogFragment = new RequiredFieldsDialogFragment();

      Bundle bundle = new Bundle();
      bundle.putString(getString(R.string.pref_key_rep_name), repName);
      bundle.putString(getString(R.string.pref_key_rep_territory), repTerritory);
      dialogFragment.setArguments(bundle);
      dialogFragment.show(getFragmentManager(), RequiredFieldsDialogFragment.TAG);
    }

    return view;
  }
}
