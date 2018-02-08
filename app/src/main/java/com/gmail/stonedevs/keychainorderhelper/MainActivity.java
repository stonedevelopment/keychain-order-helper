package com.gmail.stonedevs.keychainorderhelper;

import android.app.Fragment;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.gmail.stonedevs.keychainorderhelper.view.BackHandledFragment;
import com.gmail.stonedevs.keychainorderhelper.view.BackHandledFragment.BackHandlerInterface;

public class MainActivity extends AppCompatActivity implements BackHandlerInterface,
    OnBackStackChangedListener {

  public static final String TAG = MainActivity.class.getSimpleName();

  private BackHandledFragment mSelectedFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getFragmentManager().addOnBackStackChangedListener(this);
    displayHomeUpButton();

    //  Set default value if debugging
    if (BuildConfig.DEBUG) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

      String repName = prefs.getString(getString(R.string.pref_key_rep_name), "");
      String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory), "");

      if (repName.isEmpty()) {
        prefs.edit().putString(getString(R.string.pref_key_rep_name),
            getString(R.string.pref_debug_default_value_rep_name)).apply();
      }

      if (repTerritory.isEmpty()) {
        prefs.edit().putString(getString(R.string.pref_key_rep_territory),
            getString(R.string.pref_debug_default_value_rep_territory)).apply();
      }
    }

    // Check that the activity is using the layout version with
    // the fragment_container FrameLayout
    if (findViewById(R.id.fragment_container) != null) {

      // However, if we're being restored from a previous state,
      // then we don't need to do anything and should return or else
      // we could end up with overlapping fragments.
      if (savedInstanceState != null) {
        return;
      }

      // Create a new Fragment to be placed in the activity layout
      MainActivityFragment fragment = new MainActivityFragment();

      // In case this activity was started with special instructions from an
      // Intent, pass the Intent's extras to the fragment as arguments
      fragment.setArguments(getIntent().getExtras());

      // Add the fragment to the 'fragment_container' FrameLayout
      getFragmentManager().beginTransaction()
          .add(R.id.fragment_container, fragment).commit();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
//    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
//    if (id == R.id.action_settings) {
//      return true;
//    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (mSelectedFragment == null || !mSelectedFragment.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  public void setSelectedFragment(BackHandledFragment backHandledFragment) {
    mSelectedFragment = backHandledFragment;
  }

  @Override
  public void unsetSelectedFragment() {
    mSelectedFragment = null;
  }

  @Override
  public void onBackStackChanged() {
    displayHomeUpButton();
  }

  @Override
  public boolean onSupportNavigateUp() {
    if (mSelectedFragment == null || !mSelectedFragment.onBackPressed()) {
      super.onBackPressed();
      return true;
    }
    return false;
  }

  void displayHomeUpButton() {
    boolean canGoBack = getFragmentManager().getBackStackEntryCount() > 0;
    getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
  }

  public void setActionBarTitle(String title) {
    getSupportActionBar().setTitle(title);
  }

  public void replaceFragmentWithPopAnimation(Fragment fragment) {
    FragmentTransaction transaction = getFragmentManager().beginTransaction();
    transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left,
        R.animator.slide_out_right, R.animator.slide_in_right);
    transaction.replace(R.id.fragment_container, fragment, fragment.getTag());
    transaction.addToBackStack(null);
    transaction.commit();
  }
}
