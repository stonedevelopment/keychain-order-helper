package com.gmail.stonedevs.keychainorderhelper;

import android.app.Fragment;
import android.content.Context;

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

  public interface BackHandlerInterface {

    void setSelectedFragment(BackHandledFragment backHandledFragment);

    void unsetSelectedFragment();
  }
}
