package com.gmail.stonedevs.keychainorderhelper;

import static android.content.Intent.EXTRA_STREAM;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.gmail.stonedevs.keychainorderhelper.view.BackHandledFragment;
import com.gmail.stonedevs.keychainorderhelper.view.BackHandledFragment.BackHandlerInterface;
import java.io.File;

public class MainActivity extends AppCompatActivity implements BackHandlerInterface,
    OnBackStackChangedListener {

  public static final String TAG = MainActivity.class.getSimpleName();

  private static final int REQUEST_CODE_ACTION_SEND = 100;
  private File mSendOrderByEmailFile;

  private BackHandledFragment mSelectedFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    getSupportFragmentManager().addOnBackStackChangedListener(this);
    displayHomeUpButton();

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
      getSupportFragmentManager().beginTransaction()
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
      case REQUEST_CODE_ACTION_SEND:
        Toast.makeText(this, R.string.toast_intent_send_order_by_email_success, Toast.LENGTH_SHORT)
            .show();

        //  attempt to delete temp file
        if (mSendOrderByEmailFile != null) {
//          Util.deleteTempFile(mSendOrderByEmailFile);
          mSendOrderByEmailFile = null;
        }

        //  order was sent, saved, and temp file was deleted: close fragment, go to main menu
        closeFragment();
        break;
    }
  }

  void displayHomeUpButton() {
    boolean canGoBack = getSupportFragmentManager().getBackStackEntryCount() > 0;
    getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
  }

  public void setActionBarTitle(String title) {
    getSupportActionBar().setTitle(title);
  }

  public void sendOrderByEmail(File file, String storeName) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    String repTerritory = prefs.getString(getString(R.string.pref_key_rep_territory),
        getString(R.string.pref_key_rep_territory));

    Intent intent = new Intent(Intent.ACTION_SEND);

    // set the type to 'email'
    intent.setType("vnd.android.cursor.dir/email");

    //  set email address from preferences
    String sendtoEmail =
        BuildConfig.DEBUG ? getString(R.string.intent_extra_email_default_value_debug)
            : getString(R.string.intent_extra_email_default_value);
    String to[] = {sendtoEmail};
    intent.putExtra(Intent.EXTRA_EMAIL, to);

    // the attachment
    Uri path = Uri.fromFile(file);
    intent.putExtra(EXTRA_STREAM, path);

    // the mail subject
    String subject = String
        .format(getString(R.string.string_format_email_subject), repTerritory, storeName);
    intent.putExtra(Intent.EXTRA_SUBJECT, subject);

    //  the mail body
    String body = String
        .format(getString(R.string.intent_extra_text_body_send_order_by_email), storeName);
    intent.putExtra(Intent.EXTRA_TEXT, body);

    //  send email!
    Intent chooser = Intent
        .createChooser(intent, getString(R.string.intent_title_send_order_by_email));

    if (intent.resolveActivity(getPackageManager()) != null) {
      mSendOrderByEmailFile = file;
      startActivityForResult(chooser, REQUEST_CODE_ACTION_SEND);
    } else {
      closeFragment();
      Toast.makeText(this, R.string.toast_intent_send_order_by_email_no_supported_apps,
          Toast.LENGTH_LONG).show();
    }
  }

  public void closeFragment() {
    getSupportFragmentManager().popBackStack();
  }

  public void replaceFragmentWithPopAnimation(Fragment fragment) {
    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
    transaction.setCustomAnimations(R.animator.slide_in_left, R.animator.slide_out_left,
        R.animator.slide_out_right, R.animator.slide_in_right);
    transaction.replace(R.id.fragment_container, fragment, fragment.getTag());
    transaction.addToBackStack(null);
    transaction.commit();
  }
}
