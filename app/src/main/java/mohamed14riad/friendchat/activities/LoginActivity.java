package mohamed14riad.friendchat.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Arrays;
import java.util.List;

import mohamed14riad.friendchat.R;
import mohamed14riad.friendchat.utils.AppConstants;

public class LoginActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    private FirebaseAuth firebaseAuth = null;
    private FirebaseAuth.AuthStateListener authStateListener = null;
    private FirebaseUser firebaseUser = null;
    private static final int RC_SIGN_IN = 1000;

    private boolean result_ok = false;
    private boolean activity_opened = false;

    private SharedPreferences sharedPreferences = null;

    private TextInputLayout loginEmailEdtTxt = null, loginPasswordEdtTxt = null;
    private Button loginButton = null, signUpButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getIntent().getExtras() != null) {
            String offerId = getIntent().getStringExtra("OfferId");
            if (offerId != null) {
                Toast.makeText(this, "OfferId = " + offerId, Toast.LENGTH_SHORT).show();
            }
        }

        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic("All")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "Subscribed";
                        if (!task.isSuccessful()) {
                            msg = "Subscribe Failed";
                        }
                        Log.d(TAG, msg);
                    }
                });
        // [END subscribe_topics]


        loginEmailEdtTxt = (TextInputLayout) findViewById(R.id.loginEmailEdtTxt);
        loginPasswordEdtTxt = (TextInputLayout) findViewById(R.id.loginPasswordEdtTxt);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        signUpButton = (Button) findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (getIntent() != null) {
            if (getIntent().hasExtra(AppConstants.KEY_EMAIL)) {
                loginEmailEdtTxt.getEditText().setText(getIntent().getStringExtra(AppConstants.KEY_EMAIL));
            } else {
                loginEmailEdtTxt.getEditText().setText(null);
            }
        }

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(AppConstants.KEY_EMAIL)) {
                loginEmailEdtTxt.getEditText().setText(savedInstanceState.getString(AppConstants.KEY_EMAIL));
            } else {
                loginEmailEdtTxt.getEditText().setText(null);
            }
        }

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                firebaseUser = firebaseAuth.getCurrentUser();

                if (result_ok && firebaseUser != null) {
                    if (firebaseUser.isEmailVerified()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        activity_opened = true;
                        finish();
                    } else {
                        if (!sharedPreferences.getBoolean(AppConstants.KEY_EMAIL_SENT, false)) {
                            // one time code runs here
                            sendVerificationEmail(firebaseUser);

                            // first time has run.
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(AppConstants.KEY_EMAIL_SENT, true);
                            editor.apply();
                        }
                    }

                    loginEmailEdtTxt.getEditText().setText(firebaseUser.getEmail());
                    loginPasswordEdtTxt.getEditText().setText(null);
                    loginEmailEdtTxt.setError(null); // hide error
                    loginPasswordEdtTxt.setError(null); // hide error
                } else if (firebaseUser != null) {
                    if (firebaseUser.isEmailVerified()) {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        activity_opened = true;
                        finish();
                    } else {
                        if (!sharedPreferences.getBoolean(AppConstants.KEY_EMAIL_SENT, false)) {
                            // one time code runs here
                            sendVerificationEmail(firebaseUser);

                            // first time has run.
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean(AppConstants.KEY_EMAIL_SENT, true);
                            editor.apply();
                        }
                    }

                    loginEmailEdtTxt.getEditText().setText(firebaseUser.getEmail());
                    loginPasswordEdtTxt.getEditText().setText(null);
                    loginEmailEdtTxt.setError(null); // hide error
                    loginPasswordEdtTxt.setError(null); // hide error
                } else if (firebaseUser == null) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean(AppConstants.KEY_EMAIL_SENT, false);
                    editor.apply();
                }
            }
        };
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(AppConstants.KEY_EMAIL, loginEmailEdtTxt.getEditText().getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseAuth.removeAuthStateListener(authStateListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully authentication flow has finished
                result_ok = true;
            } else {
                // Sign in failed

                if (response == null) {
                    // User pressed back button
                    Log.e(TAG, getString(R.string.sign_in_cancelled));
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(LoginActivity.this, R.string.no_connection, Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.e(TAG, getString(R.string.sign_in_error), response.getError());
            }
        }
    }

    private void sendVerificationEmail(FirebaseUser user) {
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email is sent...
                        } else {
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            Toast.makeText(LoginActivity.this, R.string.fail_to_send_email, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.signUpButton:
                signUpUser();
                break;
            case R.id.loginButton:
                signInUser();
                break;
        }
    }

    private void signUpUser() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    private void signInUser() {
        String loginEmail = loginEmailEdtTxt.getEditText().getText().toString();
        String loginPassword = loginPasswordEdtTxt.getEditText().getText().toString();

        if (loginEmail.isEmpty() && loginPassword.isEmpty()) {
            loginEmailEdtTxt.setError(getString(R.string.email_error)); // show error
            loginPasswordEdtTxt.setError(getString(R.string.password_error)); // show error
        } else if (loginEmail.isEmpty() && !loginPassword.isEmpty()) {
            loginEmailEdtTxt.setError(getString(R.string.email_error)); // show error
            loginPasswordEdtTxt.setError(null); // hide error
        } else if (loginPassword.isEmpty() && !loginEmail.isEmpty()) {
            loginPasswordEdtTxt.setError(getString(R.string.password_error)); // show error
            loginEmailEdtTxt.setError(null); // hide error
        } else {
            loginEmailEdtTxt.setError(null); // hide error
            loginPasswordEdtTxt.setError(null); // hide error

            if (firebaseUser != null && !loginEmail.equalsIgnoreCase(firebaseUser.getEmail())) {
                signOutUser();

                firebaseAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkIsEmailVerified(firebaseUser);
                                } else {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                firebaseAuth.signInWithEmailAndPassword(loginEmail, loginPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkIsEmailVerified(firebaseUser);
                                } else {
                                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }
    }

    private void checkIsEmailVerified(FirebaseUser user) {
        if (user.isEmailVerified()) {
            // user is verified, so you can finish this activity or send user to activity which you want.
            if (!activity_opened) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        } else {
            // email is not verified, so just prompt the message to the user and restart this activity.
            Toast.makeText(LoginActivity.this, R.string.email_not_verified, Toast.LENGTH_SHORT).show();

            // restart this activity
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent().putExtra(AppConstants.KEY_EMAIL, loginEmailEdtTxt.getEditText().getText().toString()));
        }
    }

    private void signOutUser() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }
}
