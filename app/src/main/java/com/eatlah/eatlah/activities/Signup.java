package com.eatlah.eatlah.activities;

import com.eatlah.eatlah.R;
import com.eatlah.eatlah.helpers.OnTaskCompletedListener;
import com.eatlah.eatlah.helpers.fetchLatLongFromService;
import com.eatlah.eatlah.models.User;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class Signup extends AppCompatActivity implements LoaderCallbacks<Cursor>, OnTaskCompletedListener {

    /**
     * UI design
     */
    private AnimationDrawable anim;
    private AssetManager assetManager;
    private Typeface typefaceRaleway, typefaceRaleway_light, typefaceLobster;

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * com.eatlah.eatlah.models.User authentication system
     */
    private FirebaseAuth mAuth;

    /**
     * Database instance.
     */
    private FirebaseDatabase mDb;
    private DatabaseReference mDbRef;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserSignupTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mPhoneView;
    private EditText mHawkerIdView;
    private AutoCompleteTextView mHawkerCentreIdView;
    private Spinner mProfileView;
    private EditText mCustomerAddressView;
    private View mProgressView;
    private View mSignupView;

    /**
     * For the autocomplete view
     */
    private ArrayList<String> mHawkerCentres;
    private String[] mHawkerCentresArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity_signup);

        setTitle(getResources().getString(R.string.title_activity_signup));

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mDbRef = mDb.getReference(getResources().getString(R.string.user_ref));

        // Login page animation
        LinearLayout container = (LinearLayout) findViewById(R.id.container);
        anim = (AnimationDrawable) container.getBackground();
        anim.setEnterFadeDuration(100);
        anim.setEnterFadeDuration(1000);

        // Initialize fonts
        typefaceRaleway = Typeface.createFromAsset(getAssets(), "Raleway-Medium.ttf");
        typefaceLobster = Typeface.createFromAsset(getAssets(), "lobster.otf");
        typefaceRaleway_light = Typeface.createFromAsset(getAssets(), "Raleway-Light.ttf");

        // Set up the login form.
        TextView mHeader = (TextView) findViewById(R.id.signup_header);
        mHeader.setTypeface(typefaceLobster);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPhoneView = (EditText) findViewById(R.id.phone_editText);
        mPhoneView.setTypeface(typefaceRaleway);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptSignup();
                    return true;
                }
                return false;
            }
        });

        // Hawker centre related
        mHawkerIdView = findViewById(R.id.hawkerid_editText);
        mHawkerCentreIdView = findViewById(R.id.hawkercentreid_editText);
        mHawkerCentres = new ArrayList<>();
        System.out.println("LOOKIE HERERE ASLFJDLFJDSLKF\nADSLFJDLFDS\nADSFHDSFKJHDSFKJ:");
        DatabaseReference stallsRef = mDb.getReference("HawkerStalls");
        stallsRef.addValueEventListener(new ValueEventListener() { // get hawkers
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                System.out.print("znxc.,vmnzxc.vnzcx,.vncxzvm,zcxnvcmx,.zvnx,vx");
                for (DataSnapshot ds : dataSnapshot.getChildren()) { // get all hawker centre ids
                    System.out.println("MADE IT INSDIE");
                    saveHawkerCentre(ds.getKey());

                }
                fixHawkerAdapter();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
        System.out.println("YOLO finished");

        mProfileView = (Spinner) findViewById(R.id.profile_spinner);
        mProfileView.setPrompt(getResources().getString(R.string.prompt_profile));
        mProfileView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // retrieve profileId selected by user
                /* to be used for changing the visibility of hawkerId, customerAddress views */
                //todo hawkerId view
                displayRelevantFields(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button mSignupButton = (Button) findViewById(R.id.signup_button);
        mSignupButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptSignup();
            }
        });

        mCustomerAddressView = findViewById(R.id.customerAddr);

        mSignupView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * For Autosuggestion of hawker centre keys
     */
    private void saveHawkerCentre(String hawkerCentre) {
        mHawkerCentres.add(hawkerCentre);
    }
    /**
     * Callback to set the autocomplete up
     */
    private void fixHawkerAdapter() {
        mHawkerCentresArray = mHawkerCentres.toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, mHawkerCentresArray);
        mHawkerCentreIdView.setAdapter(adapter);
        System.out.println("FISXD AADDAPTER!");
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the signup form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual signup attempt is made.
     */
    private void attemptSignup() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mHawkerIdView.setError(null);
        mCustomerAddressView.setError(null);

        // Store values at the time of the signup attempt.
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

        // profile selected is a hawker and no hawkerId given
        if (mProfileView.getSelectedItemPosition() == 2 && TextUtils.isEmpty(mHawkerIdView.getText())) {
            mHawkerIdView.setError(getResources().getString(R.string.error_field_required));
            focusView = mHawkerIdView;
            cancel = true;
        }

        if (mCustomerAddressView.getError() != null) {
            focusView = mCustomerAddressView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt signup and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user signup attempt.
            showProgress(true);
            createSignupTask(email, password);
        }
    }

    /**
     * creates and executes the signup task depending on profile selected
     * @param email
     * @param password
     */
    private void createSignupTask(String email, String password) {
        System.out.println("creating signup task for " + mProfileView.getSelectedItem().toString());
        if (mProfileView.getSelectedItemPosition() == 0) {  // customer
            String customerAddress = mCustomerAddressView.getText().toString();

            new UserSignupTask(email, password, customerAddress)
                .execute();
        } else if (mProfileView.getSelectedItemPosition() == 2) {   // hawker
            String hawkerId = mHawkerIdView.getText().toString();
            String hawkerCentreId = mHawkerCentreIdView.getText().toString();
            new UserSignupTask(email, password, hawkerId, hawkerCentreId)
                    .execute();
        } else {
            new UserSignupTask(email, password, null, null, null)
                    .execute();
        }
    }

    /**
     * changes the visibility of relevant fields.
     * Precondition: associated fields are invisible
     * Postcondition: depending on profile_idx selected, relevant fields set to visible
     * @param profile_idx
     */
    private void displayRelevantFields(int profile_idx) {
        System.out.println("profile selected: " + profile_idx);
        if (profile_idx == 0) { // customer
            // display customerAddress view
            findViewById(R.id.customerAddr_inputLayout).setVisibility(View.VISIBLE);
            mCustomerAddressView.setVisibility(View.VISIBLE);
            mCustomerAddressView.setHint(getResources().getString(R.string.customer_address));
            checkAddressValidity(mCustomerAddressView.getText().toString());
        } else if (profile_idx == 1) {  // courier

        } else {    // hawker

        }
    }

    private void checkAddressValidity(String address) {
        if (address.isEmpty()) {
            mCustomerAddressView.setError(getResources().getString(R.string.customer_address_empty));
            return;
        }

//        new fetchLatLongFromService(address, this)
//                .execute();

    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return true;
    }

    /**
     * Shows the progress UI and hides the signup form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mSignupView.setVisibility(show ? View.GONE : View.VISIBLE);
            mSignupView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mSignupView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mSignupView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

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

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(Signup.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @Override
    public void onTaskCompleted(List<String> list) {
        // null
    }

    /**
     * if customer address field is non-empty,
     * validate address and set error corresponding to editText if any.
     * @param address
     */
    @Override
    public void onTaskCompleted(String address) {
        if (TextUtils.isEmpty(address)) {
            mCustomerAddressView.setError(getResources().getString(R.string.customer_address_invalid));
        }
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous signup/registration task used to authenticate
     * the user.
     */
    public class UserSignupTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;
        private final String mHawkerId;
        private final String mCustomerAddress;
        private final String mHawkerCentreId;

        UserSignupTask(String email, String password, String hawkerId, String hawkerCentreId, String customerAddress) {
            mEmail = email;
            mPassword = password;
            mHawkerId = hawkerId;
            mHawkerCentreId = hawkerCentreId;
            mCustomerAddress = customerAddress;

        }

        UserSignupTask(String email, String password, String customerAddress) {
            this(email, password, null, null, customerAddress);
        }

        UserSignupTask(String email, String password, String hawkerId, String hawkerCentreId) {
            this(email, password, hawkerId, hawkerCentreId, null);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            System.out.println("signing up user in background");
            return mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {  // successfully signed up valid user
                                System.out.println("mauth created user successfully");
                                saveUserToDb(mPhoneView.getText().toString(), mEmail);  // add user to db
                                Log.d("signup", "created new user signup successfully");
                                redirectToLogin();  // redirect newly created user to login page
                            } else {
                                System.out.println("background task err");
                                Log.e("signup", task.getException().getMessage());
                                Toast.makeText(Signup.this, "Unsuccessful signup attempt.", Toast.LENGTH_SHORT)
                                        .show();
                                showProgress(false);
                                mEmailView.requestFocus();
                                cancel(true);
                                onCancelled();
                            }
                        }
                    }).isSuccessful();
        }

        /**
         * Saves user to database.
         * @param phone_number _id of user object
         * @param email email of user
         * @return true if user is successfully saved to db, false otherwise.
         */
        private void saveUserToDb(String phone_number, String email) {
            System.out.println("saving user");
            mDbRef
                .child(mAuth.getUid())  // index by mAuth generated uid
                .setValue(new User(phone_number, email, mHawkerId, mHawkerCentreId, mCustomerAddress))
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("saved user to db successfully");
                            Log.d("signup", "saved user signup to db successfully.");
                            Toast.makeText(Signup.this, "Signed up successfully!", Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Log.e("signup", task.getException().getMessage());
                            mEmailView.requestFocus();
                            cancel(true);
                            onCancelled();
                        }
                    }
                });
        }

        /**
         * Signup was successful, redirect user to login page.
         */
        protected void redirectToLogin() {
            mAuthTask = null;
            showProgress(false);
            finish();
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

