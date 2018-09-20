package com.zn.expirytracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.zn.expirytracker.R;
import com.zn.expirytracker.utils.AuthToolbox;
import com.zn.expirytracker.utils.Toolbox;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN_GOOGLE = 1110;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @BindView(R.id.layout_sign_in_root)
    View mRootView;
    @BindView(R.id.overlay_sign_in_no_click)
    View mNoClickOverlay;
    @BindView(R.id.til_sign_in_email)
    TextInputLayout mTilEmail;
    @BindView(R.id.tiEt_sign_in_email)
    TextInputEditText mEtEmail;
    @BindView(R.id.til_sign_in_password)
    TextInputLayout mTilPassword;
    @BindView(R.id.tiEt_sign_in_password)
    TextInputEditText mEtPassword;
    @BindView(R.id.btn_sign_in_login)
    Button mBtnSignIn;
    @BindView(R.id.pb_sign_in_login)
    ProgressBar mPbSignIn;
    @BindView(R.id.btn_sign_in_google)
    SignInButton mBtnGoogle;
    @BindView(R.id.pb_sign_in_google)
    ProgressBar mPbGoogle;
    @BindView(R.id.btn_sign_in_create_account)
    Button mBtnCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.tag(SignInActivity.class.getSimpleName());
        setContentView(R.layout.activity_sign_in);
        ButterKnife.bind(this);

        mBtnSignIn.setOnClickListener(this);
        mBtnGoogle.setOnClickListener(this);
        mBtnCreateAccount.setOnClickListener(this);
        mNoClickOverlay.setOnClickListener(this);

        mEtEmail.addTextChangedListener(new SignInTextInputWatcher(mTilEmail));
        mEtPassword.addTextChangedListener(new SignInTextInputWatcher(mTilPassword));

        mAuth = FirebaseAuth.getInstance();

        // Configure Google sign-in
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (AuthToolbox.isSignedIn()) {
            startMainActivity();
        } else {
            // Show UI
        }
    }

    /**
     * Helper to start the main activity
     */
    private void startMainActivity() {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in_login:
                String email = mEtEmail.getText().toString();
                String password = mEtPassword.getText().toString();
                if (areInputsValid(email, password)) {
                    signInWithEmail(email, password);
                }
                break;
            case R.id.btn_sign_in_google:
                signInWithGoogle();
                break;
            case R.id.btn_sign_in_create_account:
                break;
            case R.id.overlay_sign_in_no_click:
                // Prevent click-handling for root view
                break;
        }
    }

    /**
     * Validates e-mail and password fields and shows an error if not valid
     * <p>
     * 1) E-mail address must be in a valid form: ***@***.***
     * <p>
     * 2) Password must have at least 8 characters and must not contain any spaces
     *
     * @return {@code true} if all inputs are valid
     */
    private boolean areInputsValid(String email, String password) {
        boolean valid = true;
        // check e-mail
        if (email.trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mTilEmail.setError("Please enter a valid e-mail address");
            valid = false;
        } else {
            mTilEmail.setError(null);
        }
        // check password
        if (password.trim().isEmpty() ||
                password.trim().length() < AuthToolbox.DEFAULT_MIN_PASSWORD_LENGTH ||
                password.contains(" ")) {
            mTilPassword.setError("Please enter a valid password of at least 8 characters and no spaces");
            valid = false;
        } else {
            mTilPassword.setError(null);
        }
        return valid;
    }

    /**
     * Signs a user in
     *
     * @param email
     * @param password
     */
    private void signInWithEmail(String email, String password) {
        Toolbox.showView(mNoClickOverlay, true, true);
        Toolbox.showView(mPbSignIn, true, false);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            Timber.d("Sign in with email: success!");
                            AuthToolbox.syncSignInWithDevice(getApplicationContext(),
                                    mAuth.getCurrentUser());
                            startMainActivity();
                        } else {
                            // Sign in failed
                            String error = ((FirebaseAuthException) task.getException()).getErrorCode();
                            handleSignInWithEmailFailure(error);
                            Toolbox.showSnackbarMessage(mRootView,
                                    "There was a problem signing in.");
                        }
                        Toolbox.showView(mNoClickOverlay, false, true);
                        Toolbox.showView(mPbSignIn, false, false);
                    }
                });
    }

    /**
     * Creates a new account
     *
     * @param email
     * @param password
     */
    private void signUpWithEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // New account is created successfully, also sign the user in
                            Timber.d("Create user with email: success!");
                            AuthToolbox.syncSignInWithDevice(getApplicationContext(),
                                    mAuth.getCurrentUser());
                            startMainActivity();
                        } else {
                            // Sign up failed
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                mTilPassword.setError(getString(R.string.auth_error_weak_password));
                                mTilPassword.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                mTilEmail.setError(getString(R.string.auth_error_invalid_email));
                                mTilEmail.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mTilEmail.setError(getString(R.string.auth_error_user_exists));
                                mTilEmail.requestFocus();
                            } catch (Exception e) {
                                Timber.w(e, "Sign up with email: failed");
                            }
                            Toolbox.showSnackbarMessage(mRootView,
                                    "There was a problem creating your account.");
                        }
                    }
                });
    }

    /**
     * Launches the Google Sign-in prompt, and disables view clicks and shows the loading indicators
     */
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN_GOOGLE);
        Toolbox.showView(mNoClickOverlay, true, true);
        Toolbox.showView(mPbGoogle, true, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN_GOOGLE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Link the Google Sign-In result with Firebase Auth, and then start Main Activity.
     * <p>
     * By this point, the loading indicators must already be showing, with disabled rootview clicks.
     * Once result has been handled, this hides loading indicators and re-enables rootview clicks.
     *
     * @param completedTask
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully. Link with Firebase by exchanging the Google ID token for a
            // Firebase credential
            Timber.d("FirebaseAuthWithGoogle: %s", account.getId());
            AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
            mAuth.signInWithCredential(credential)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success. Link with SP and then start main activity
                                Timber.d("Firebase auth sign in with Google credential success!");
                                AuthToolbox.syncSignInWithDevice(getApplicationContext(),
                                        mAuth.getCurrentUser());
                                startMainActivity();
                            } else {
                                // Sign in failed
                                Timber.w(task.getException(), "Firebase auth sign in with" +
                                        " Google credential failed");
                                Toolbox.showSnackbarMessage(mRootView, "Authentication failed.");
                            }
                            Toolbox.showView(mNoClickOverlay, false, true);
                            Toolbox.showView(mPbGoogle, false, false);
                        }
                    });
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            int statusCode = e.getStatusCode();
            Timber.w(e, "Google Sign-in failed. Result code: %s", e.getStatusCode());
            handleGoogleSignInFailure(statusCode);
            Toolbox.showView(mNoClickOverlay, false, true);
            Toolbox.showView(mPbGoogle, false, false);
        }
    }


    /**
     * Custom {@link TextWatcher} that hides errors for {@link TextInputLayout}s after the calling
     * EditText has changed text
     */
    private class SignInTextInputWatcher implements TextWatcher {
        private TextInputLayout mTil;

        SignInTextInputWatcher(TextInputLayout til) {
            mTil = til;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mTil.setError(null);
        }

        @Override
        public void afterTextChanged(Editable s) {
        }

    }


    /**
     * Helper to handle errors with sign in using e-mail
     * <p>
     * https://stackoverflow.com/questions/37859582/how-to-catch-a-firebase-auth-specific-exceptions
     *
     * @param error
     */
    private void handleSignInWithEmailFailure(String error) {
        switch (error) {
            case "ERROR_INVALID_CUSTOM_TOKEN":
                Timber.e("The custom token format is incorrect. Please check the documentation.");
                break;

            case "ERROR_CUSTOM_TOKEN_MISMATCH":
                Timber.e("The custom token corresponds to a different audience.");
                break;

            case "ERROR_INVALID_CREDENTIAL":
                Timber.e("The supplied auth credential is malformed or has expired.");
                break;

            case "ERROR_INVALID_EMAIL":
                Timber.e("The email address is badly formatted.");
                mTilEmail.setError(getString(R.string.auth_error_invalid_email));
                mTilEmail.requestFocus();
                break;

            case "ERROR_WRONG_PASSWORD":
                Timber.e("The password is invalid or the user does not have a password.");
                mTilPassword.setError(getString(R.string.auth_error_wrong_password));
                mTilPassword.requestFocus();
                mEtPassword.setText("");
                break;

            case "ERROR_USER_MISMATCH":
                Timber.e("The supplied credentials do not correspond to the previously signed in user.");
                break;

            case "ERROR_REQUIRES_RECENT_LOGIN":
                Timber.e("This operation is sensitive and requires recent authentication. Log in again before retrying this request.");
                break;

            case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                Timber.e("An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.");
                break;

            case "ERROR_EMAIL_ALREADY_IN_USE":
                Timber.e("The email address is already in use by another account.");
                mTilEmail.setError(getString(R.string.auth_error_email_already_in_use));
                mTilEmail.requestFocus();
                break;

            case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                Timber.e("This credential is already associated with a different user account.");
                break;

            case "ERROR_USER_DISABLED":
                Timber.e("The user account has been disabled by an administrator.");
                break;

            case "ERROR_USER_TOKEN_EXPIRED":
                Timber.e("The user\\'s credential is no longer valid. The user must sign in again.");
                break;

            case "ERROR_USER_NOT_FOUND":
                Timber.e("There is no user record corresponding to this identifier. The user may have been deleted.");
                mTilEmail.setError(getString(R.string.auth_error_user_not_found));
                mTilEmail.requestFocus();
                break;

            case "ERROR_INVALID_USER_TOKEN":
                Timber.e("The user\\'s credential is no longer valid. The user must sign in again.");
                break;

            case "ERROR_OPERATION_NOT_ALLOWED":
                Timber.e("This operation is not allowed. You must enable this service in the console.");
                break;

            case "ERROR_WEAK_PASSWORD":
                Timber.e("The given password is invalid.");
                mTilPassword.setError(getString(R.string.auth_error_weak_password));
                mTilPassword.requestFocus();
                break;
        }
    }

    /**
     * Helper to handle errors with sign in using Google
     *
     * @param statusCode
     */
    private void handleGoogleSignInFailure(int statusCode) {
        switch (statusCode) {
            case GoogleSignInStatusCodes.CANCELED:
            case GoogleSignInStatusCodes.SIGN_IN_CANCELLED:
                // Don't say anything, the user wanted to cancel
                break;
            case GoogleSignInStatusCodes.TIMEOUT:
                Toolbox.showSnackbarMessage(mRootView, getString(R.string.auth_error_timeout));
                break;
            case GoogleSignInStatusCodes.INTERRUPTED:
                Toolbox.showSnackbarMessage(mRootView, getString(R.string.auth_error_interrupted));
                break;
            case GoogleSignInStatusCodes.NETWORK_ERROR:
                Toolbox.showSnackbarMessage(mRootView, getString(R.string.auth_error_network));
                break;
            case GoogleSignInStatusCodes.INVALID_ACCOUNT:
                Toolbox.showSnackbarMessage(mRootView, getString(R.string.auth_error_invalid_account));
                break;
            default:
                Toolbox.showSnackbarMessage(mRootView,
                        getString(R.string.auth_error_sign_in_failed));
        }
    }
}
