package com.gmail.stonedevs.keychainorderhelper.view;

import android.content.Context;
import android.support.v4.app.Fragment;
import com.gmail.stonedevs.keychainorderhelper.MainActivity;

public abstract class BackHandledFragment extends Fragment {

  protected BackHandlerInterface mListener;

  public abstract boolean onBackPressed();

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);

    if (context instanceof BackHandlerInterface) {
      mListener = (BackHandlerInterface) context;
      mListener.setSelectedFragment(this);
    } else {
      throw new RuntimeException(context.toString()
          + " must implement BackHandlerInterface");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();

    mListener.unsetSelectedFragment();
    mListener = null;
  }

  public void closeFragment() {
    ((MainActivity) getActivity()).closeFragment();
  }

  public interface BackHandlerInterface {

    void setSelectedFragment(BackHandledFragment backHandledFragment);

    void unsetSelectedFragment();
  }
}