package xyz.teamcatalyst.breedr.auth;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.functions.Consumer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import xyz.teamcatalyst.breedr.FirebaseUtils;
import xyz.teamcatalyst.breedr.GlideApp;
import xyz.teamcatalyst.breedr.MyDashboard;
import xyz.teamcatalyst.breedr.R;
import xyz.teamcatalyst.breedr.data.Profile;
import xyz.teamcatalyst.breedr.profile.ProfileActivity;

public class ActivitySignin extends AppCompatActivity {

    int RC_SIGN_IN = 9000;
    private String TAG = "";
    private FirebaseAuth mAuth;
    private boolean emailAndPass = true;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.username)
    EditText mUsername;
    @BindView(R.id.password)
    EditText mPassword;
    @BindView(R.id.signin)
    TextView mSignin;
    @BindView(R.id.filter_language)
    Spinner mLanguage;
    private int tries;
    private CallbackManager mCallbackManager;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference myProfile;
    private FirebaseUser user;
    private Profile profile;
    private boolean isSameFace;

    boolean isFirstLoad = true;
    private AlertDialog alertDialog;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin);
        ButterKnife.bind(this);

        if (isTagalog()) {
            mLanguage.setSelection(1);
        }
        mLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isFirstLoad) {
                    isFirstLoad = false;
                    return;
                }
                if (position == 0) {
                    if (Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("en") ||
                            Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("english") ||
                            Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("eng")) {

                    } else {
                        setLocale("en");
                    }

                } else {
                    if (!isTagalog()) {
                        setLocale("tl");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        isSameFace = getIntent().getBooleanExtra("is_same_face", false);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
            dismissLoginPicker();
        });

        tries = 0;
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                this.user = user;
                // User is signed in
                Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                myProfile = database.getReference("user_profile").child(user.getUid());

                myProfile.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        profile = dataSnapshot.getValue(Profile.class);
                        if (profile == null || TextUtils.isEmpty(profile.getProfileUrl())) {
                            myProfile.removeEventListener(this);
                            finish();
                            startActivityForResult(ProfileActivity.getRegistrationIntent(ActivitySignin.this), 123);
                        } else {
                            checkIfVerified();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        };

        // Initialize Facebook Login button
        mCallbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions("email", "public_profile");
        loginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                dismissLoginPicker();
                new AlertDialog.Builder(ActivitySignin.this)
                        .setMessage(R.string.disclaimer)
                        .setPositiveButton(R.string.agree, (dialogInterface, i) -> {
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        })
                        .setNegativeButton(R.string.disagree, ((dialogInterface, i) -> dialogInterface.dismiss()))
                        .show();

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                dismissLoginPicker();
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                dismissLoginPicker();
            }
        });

        ReactiveNetwork.observeNetworkConnectivity(this)
                .subscribeOn(io.reactivex.schedulers.Schedulers.io())
                .observeOn(io.reactivex.android.schedulers.AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    if (connectivity.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                        new AlertDialog.Builder(ActivitySignin.this)
                                .setMessage(R.string.no_network_connection)
                                .setPositiveButton("Ok", (dialogInterface, i) -> {
                                    dialogInterface.dismiss();
                                })
                                .show();
                    }

                });

    }

    public boolean isTagalog() {
        return Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("tl") ||
                Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("tagalog") ||
                Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("fl") ||
                Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("fil") ||
                Locale.getDefault().getDisplayLanguage().equalsIgnoreCase("filipino");
    }

    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Intent refresh = new Intent(this, ActivitySignin.class);
        startActivity(refresh);
        finish();
    }

    private void checkIfVerified() {
        String providerId = user.getProviderId();
        emailAndPass = providerId.equals("email");
        if (user.isEmailVerified() || !emailAndPass) {
            verifyFace();
        } else {
            new AlertDialog.Builder(ActivitySignin.this)
                    .setMessage("Your email is not verified yet please check your email. " +
                            "Do you want to resend verification email?")
                    .setPositiveButton("Resend", (dialogInterface, i) -> {
                        FirebaseUtils.sendVerificationEmail(user, task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ActivitySignin.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .setNegativeButton("No", ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();
        }
    }

    private void verifyFace() {
        isSameFace = Prefs.getBoolean("isSameFace", isSameFace);
        if (isSameFace) {
            startActivity(new Intent(ActivitySignin.this, MyDashboard.class));
        } else {
            finish();
            Intent intent = new Intent(this, FaceVerificationActivity.class);
            intent.putExtra("profile_image", profile.getProfileUrl());
            startActivityForResult(intent, 124);
        }

    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        emailAndPass = false;

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(ActivitySignin.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        emailAndPass = false;

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }

                        // ...
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            new AlertDialog.Builder(ActivitySignin.this)
                    .setMessage(getString(R.string.disclaimer))
                    .setPositiveButton(getString(R.string.agree), (dialogInterface, i) -> {
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w(TAG, "Google sign in failed", e);
                            // ...
                        }
                    })
                    .setNegativeButton(getString(R.string.disagree), ((dialogInterface, i) -> dialogInterface.dismiss()))
                    .show();

        } else if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                mAuth.addAuthStateListener(mAuthListener);
                checkIfVerified();
            } else {
                FirebaseAuth.getInstance().signOut();
            }
        } else if (requestCode == 124) {
            if (resultCode == RESULT_OK) {
                startActivity(new Intent(ActivitySignin.this, MyDashboard.class));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @OnClick(R.id.email_button)
    public void emalSignIn() {
        dismissLoginPicker();
        new AlertDialog.Builder(ActivitySignin.this)
                .setMessage(getString(R.string.disclaimer))
                .setPositiveButton(getString(R.string.agree), (dialogInterface, i) -> {
                    emailAndPass = true;
                    Prefs.putLong("login_attempt", System.currentTimeMillis());
                    if (TextUtils.isEmpty(mUsername.getText())) return;
                    if (TextUtils.isEmpty(mPassword.getText())) return;
                    mAuth.signInWithEmailAndPassword(mUsername.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(this, task -> {
                                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                    tries += 1;
                                    if (tries == 3) {
                                        finish();
                                        return;
                                    }
                                    Toast.makeText(ActivitySignin.this, getString(R.string.login_failed) + " " + (3 - tries) + " " + getString(R.string.login_attempts_left), Toast.LENGTH_SHORT).show();
                                    new AlertDialog.Builder(this)
                                            .setMessage(getString(R.string.login_failed) + " " + (3 - tries) + " " + getString(R.string.login_attempts_left))
                                            .setPositiveButton("Ok", (di, i2) -> {
                                                di.dismiss();
                                            })
                                            .show();
                                }

                            });
                })
                .setNegativeButton(getString(R.string.disagree), ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();
    }

    @OnClick(R.id.signin)
    public void onSignIn() {
        dismissLoginPicker();

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        View email = findViewById(R.id.email_button);
        View fb = findViewById(R.id.login_button);
        View google = findViewById(R.id.sign_in_button);
        email.setVisibility(View.VISIBLE);
        fb.setVisibility(View.VISIBLE);
        google.setVisibility(View.VISIBLE);
        ViewGroup parent = ((ViewGroup)email.getParent());
        ((ViewGroup)email.getParent()).removeView(email);
        ((ViewGroup)fb.getParent()).removeView(fb);
        ((ViewGroup)google.getParent()).removeView(google);
        layout.addView(email);
        layout.addView(fb);
        layout.addView(google);

        alertDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setOnDismissListener(dialog -> {
                    ((ViewGroup)email.getParent()).removeView(email);
                    ((ViewGroup)fb.getParent()).removeView(fb);
                    ((ViewGroup)google.getParent()).removeView(google);
                    parent.addView(email);
                    parent.addView(fb);
                    parent.addView(google);
                    email.setVisibility(View.GONE);
                    fb.setVisibility(View.GONE);
                    google.setVisibility(View.GONE);
                })
                .show();

    }

    public void dismissLoginPicker() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @OnClick(R.id.signup)
    public void onSignUp() {
        emailAndPass = true;
        startActivity(ActivitySignup.getStartIntent(this));
    }

    @OnClick(R.id.forgot_password)
    public void onForgotPassword() {
        if (TextUtils.isEmpty(mUsername.getText())) {
            new AlertDialog.Builder(this)
                    .setMessage(R.string.type_email)
                    .show();
            return;
        }
        FirebaseAuth.getInstance().sendPasswordResetEmail(mUsername.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ActivitySignin.this, getString(R.string.reset_password_email_sent), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ActivitySignin.this, "Something went wrong. Did you enter a correct email?", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @OnClick(R.id.terms)
    public void onTermsClicked() {
        new AlertDialog.Builder(ActivitySignin.this)
                .setMessage(getString(R.string.disclaimer))
                .setPositiveButton(getString(R.string.agree), (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setNegativeButton(getString(R.string.disagree), ((dialogInterface, i) -> dialogInterface.dismiss()))
                .show();
    }

}
