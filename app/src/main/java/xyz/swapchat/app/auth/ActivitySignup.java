package xyz.teamcatalyst.breedr.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import xyz.teamcatalyst.breedr.FirebaseUtils;
import xyz.teamcatalyst.breedr.R;

public class ActivitySignup extends AppCompatActivity {


    private String TAG = "";
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @BindView(R.id.signupback) ImageView mSignupback;
    @BindView(R.id.fullname) EditText mFullname;
    @BindView(R.id.email) EditText mEmail;
    @BindView(R.id.password) EditText mPassword;
    @BindView(R.id.signin) TextView mSignin;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, ActivitySignup.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        // Write a message to the database
        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                user.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(mFullname.getText().toString()).build()).addOnCompleteListener(aVoid -> {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    FirebaseUtils.sendVerificationEmail(user, task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ActivitySignup.this, "Signup successful. Verification email sent", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                });
            } else {
                // User is signed out
                Log.d(TAG, "onAuthStateChanged:signed_out");
            }
        };

        mSignupback.setOnClickListener(v -> {
            finish();
        });

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

    Pattern uppercase = Pattern.compile("(?=.*[A-Z])");
    Pattern digit = Pattern.compile("[0-9]");
    Pattern special = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~-]");

    public final boolean isValidPassword(String password) {
        Matcher hasLetter = uppercase.matcher(password);
        Matcher hasDigit = digit.matcher(password);
        Matcher hasSpecial = special.matcher(password);
        return hasDigit.find() && password.length() >= 6;
    }

    @OnClick(R.id.signin)
    public void onClick() {
        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            new AlertDialog.Builder(this)
                    .setMessage("You should supply a password")
                    .show();
            return;
        }

        if (!isValidPassword(password)) {
            new AlertDialog.Builder(this)
                    .setMessage("Password needs at least 8 characters, a number, a special character, and an uppercase letter")
                    .show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), password)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                    Prefs.putString("email", mEmail.getText().toString());
                    Prefs.putString("fullname", mFullname.getText().toString());

                    if (!task.isSuccessful()) {
                        new AlertDialog.Builder(this)
                                .setMessage(task.getException().getMessage())
                                .show();
                        Toast.makeText(ActivitySignup.this, "Authentication Failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
