package co.brianberg.taiga.auth;

import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.ProgressDialog;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import co.brianberg.taiga.R;
import co.brianberg.taiga.projects.ProjectListActivity;
import co.brianberg.taiga.service.Taiga;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

  /**
   * ID to identity READ_CONTACTS permission request.
   */
  private static final int REQUEST_READ_CONTACTS = 0;

  /**
   * Keep track of the login task to ensure we can cancel it if requested.
   */
  private BasicLoginTask mAuthTask;

  /**
   * Authenticated user reference
   */
  private User mUser;

  // UI references.
  private ImageView mLogoView;
  private LinearLayout mLoginForm;
  private AutoCompleteTextView mEmailView;
  private EditText mPasswordView;
  private ProgressDialog mProgressDialog;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // Load credentials
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this.getBaseContext());
    if (prefs.contains("user")) {
      mUser  = new Gson().fromJson(prefs.getString("user", null), User.class);
    }

    mLogoView = (ImageView) findViewById(R.id.app_logo);

    // Set up the login form.
    mLoginForm = (LinearLayout) findViewById(R.id.login_form);
    mEmailView = (AutoCompleteTextView) findViewById(R.id.prompt_login_email);
    populateAutoComplete();

    mPasswordView = (EditText) findViewById(R.id.prompt_login_password);
    mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
          attemptLogin();
          return true;
        }
        return false;
      }
    });

    Button mSignInButton = (Button) findViewById(R.id.btn_sign_in);
    mSignInButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        attemptLogin();
      }
    });

    // Set up the progress dialog
    mProgressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
    mProgressDialog.setIndeterminate(true);
    mProgressDialog.setMessage(getString(R.string.auth_dialog_msg));
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mUser != null) {
      onLoginSuccess(mUser);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // Checks whether a hardware keyboard is available
    if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
      ViewGroup.LayoutParams layoutParams = mLogoView.getLayoutParams();
      layoutParams.height = layoutParams.height / 2;
      layoutParams.width = layoutParams.width / 2;
      mLogoView.setLayoutParams(layoutParams);
    } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
      ViewGroup.LayoutParams layoutParams = mLogoView.getLayoutParams();
      layoutParams.height = layoutParams.height * 2;
      layoutParams.width = layoutParams.width * 2;
      mLogoView.setLayoutParams(layoutParams);
    }
  }

  private void populateAutoComplete() {
    if (!mayRequestContacts()) {
      return;
    }
    getLoaderManager().initLoader(0, null, this);
  }

  private boolean mayRequestContacts() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
      return true;
    }
    if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
      return true;
    }
    if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
      Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
          .setAction(android.R.string.ok, new View.OnClickListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.M)
            public void onClick(View v) {
              requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
            }
          });
    } else {
      requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
    }
    return false;
  }

  /**
   * Callback received when a permissions request has been completed.
   */
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (requestCode == REQUEST_READ_CONTACTS) {
      if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        populateAutoComplete();
      }
    }
  }

  /**
   * Attempts to sign in or register the account specified by the login form.
   * If there are form errors (invalid email, missing fields, etc.), the
   * errors are presented and no actual login attempt is made.
   */
  private void attemptLogin() {
    if (mAuthTask != null) {
      return;
    }

    // Reset errors.
    mEmailView.setError(null);
    mPasswordView.setError(null);

    // Store values at the time of the login attempt.
    String email = mEmailView.getText().toString();
    String password = mPasswordView.getText().toString();

    boolean cancel = false;
    View focusView = null;

    // Check for a valid password, if the user entered one.
    if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
      mPasswordView.setError(getString(R.string.error_invalid_password));
      focusView = mPasswordView;
      cancel = true;
    }

    // Check for a valid email address.
    if (TextUtils.isEmpty(email)) {
      mEmailView.setError(getString(R.string.error_field_required));
      focusView = mEmailView;
      cancel = true;
    } else if (!isEmailValid(email)) {
      mEmailView.setError(getString(R.string.error_invalid_email));
      focusView = mEmailView;
      cancel = true;
    }

    if (cancel) {
      // There was an error; don't attempt login and focus the first
      // form field with an error.
      focusView.requestFocus();
    } else {
      // Show a progress spinner, and kick off a background task to
      // perform the user login attempt.
      showProgressDialog();
      mAuthTask = new BasicLoginTask(email, password);
      mAuthTask.execute((Void) null);
    }
  }

  private boolean isEmailValid(String email) {
    return email.contains("@");
  }

  private boolean isPasswordValid(String password) {
    return password.length() > 4;
  }

  /**
   * Shows the progress dialog.
   */
  private void showProgressDialog() {
    mProgressDialog.show();
  }

  private void dismissProgressDialog() {
    mProgressDialog.dismiss();
  }

  private void onLoginSuccess(User user) {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this.getBaseContext());
    SharedPreferences.Editor editor = prefs.edit();
    editor.putString("user", new Gson().toJson(user));
    editor.apply();
    Bundle args = new Bundle();
    args.putInt(ProjectListActivity.ARG_MEMBER_ID, user.getId());
    Intent projectListIntent = new Intent(LoginActivity.this, ProjectListActivity.class);
    projectListIntent.putExtras(args);
    startActivity(projectListIntent);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
    return new CursorLoader(this,
      // Retrieve data rows for the device user's 'profile' contact.
      Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
          ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

      // Select only email addresses.
      ContactsContract.Contacts.Data.MIMETYPE +
          " = ?", new String[]{ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE},

      // Show primary email addresses first. Note that there won't be
      // a primary email address if the user hasn't specified one.
      ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
  }

  @Override
  public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
    List<String> emails = new ArrayList<>();
    cursor.moveToFirst();
    while (!cursor.isAfterLast()) {
      emails.add(cursor.getString(ProfileQuery.ADDRESS));
      cursor.moveToNext();
    }
    addEmailsToAutoComplete(emails);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> cursorLoader) {

  }


  private interface ProfileQuery {
    String[] PROJECTION = {
        ContactsContract.CommonDataKinds.Email.ADDRESS,
        ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
    };
    int ADDRESS = 0;
  }

  private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
    //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
    ArrayAdapter<String> adapter =
        new ArrayAdapter<>(LoginActivity.this,
            android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

    mEmailView.setAdapter(adapter);
  }

  /**
   * Represents an asynchronous login/registration task used to authenticate
   * the user.
   */
  public class BasicLoginTask extends AsyncTask<Void, Void, User> {

    private final String TAG = BasicLoginTask.class.getSimpleName();
    private final String mUsername;
    private final String mPassword;

    BasicLoginTask(String username, String password) {
      mUsername = username;
      mPassword = password;
    }

    @Override
    protected User doInBackground(Void... params) {
      return Taiga.signIn(LoginActivity.this.getBaseContext(), mUsername, mPassword,
          new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
              Log.d(TAG, error.toString());
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  Snackbar.make(mLoginForm, R.string.auth_invalid_credentials, Snackbar.LENGTH_LONG);
                }
              });
            }
          }
      );
    }

    @Override
    protected void onPostExecute(final User user) {
      mAuthTask = null;
      dismissProgressDialog();
      if (user != null) {
          onLoginSuccess(user);
      }
    }

    @Override
    protected void onCancelled() {
      mAuthTask = null;
      dismissProgressDialog();
    }
  }
}

