package com.gmail.stonedevs.keychainorderhelper;

import android.app.Fragment;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.util.Util;
import com.gmail.stonedevs.keychainorderhelper.view.BackHandledFragment;
import com.gmail.stonedevs.keychainorderhelper.view.BackHandledFragment.BackHandlerInterface;
import java.io.File;

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

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (requestCode) {
      case R.string.intent_request_code_send_order_by_email:
        Toast
            .makeText(this, R.string.toast_intent_send_order_by_email_success,
                Toast.LENGTH_SHORT)
            .show();

        //  get path of sent excel file from intent bundle
        Uri path = data.getParcelableExtra(Intent.EXTRA_STREAM);

        //  attempt to delete temp file
        Util.deleteTempFile(path);

        //  order was sent, saved, and temp file was deleted: close fragment, go to main menu
        closeFragment();
        break;
    }
  }

  void displayHomeUpButton() {
    boolean canGoBack = getFragmentManager().getBackStackEntryCount() > 0;
    getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
  }

  public void setActionBarTitle(String title) {
    getSupportActionBar().setTitle(title);
  }

  public void sendOrderByEmail(File file, String storeName) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory),
        getString(R.string.pref_key_rep_territory));

    Uri path = Uri.fromFile(file);
    Intent intent = new Intent(Intent.ACTION_SEND);

    // set the type to 'email'
    intent.setType("vnd.android.cursor.dir/email");

    //  set email address from preferences
    String sendtoEmail = getString(R.string.pref_default_value_sendto_email);
    String to[] = {sendtoEmail};
    intent.putExtra(Intent.EXTRA_EMAIL, to);

    // the attachment
    intent.putExtra(Intent.EXTRA_STREAM, path);

    // the mail subject
    String subject = String
        .format(getString(R.string.string_format_email_subject), repTerritory, storeName);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    //  the mail body
    String body = String
        .format(getString(R.string.intent_extra_text_body_send_order_by_email), storeName);
    intent.putExtra(Intent.EXTRA_TEXT, body);

    //  send email!
    if (BuildConfig.DEBUG) {
      //  attempt to delete temp file from path used with intent
      Util.deleteTempFile(file);

      //  order was sent, saved, and temp file was deleted: close fragment, go to main menu
      closeFragment();
    } else {
      Intent chooser = Intent
          .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

      if (intent.resolveActivity(getPackageManager()) != null) {
        startActivityForResult(chooser, R.string.intent_request_code_send_order_by_email);
      }
    }
  }

  public void closeFragment() {
    getFragmentManager().popBackStack();
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
