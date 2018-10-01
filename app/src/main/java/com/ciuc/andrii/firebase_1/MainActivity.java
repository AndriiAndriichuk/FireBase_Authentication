package com.ciuc.andrii.firebase_1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.CallbackManager.Factory;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken;
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks;
import com.google.firebase.auth.TwitterAuthProvider;
import com.shaishavgandhi.loginbuttons.FacebookButton;
import com.shaishavgandhi.loginbuttons.GoogleButton;
import com.shaishavgandhi.loginbuttons.TwitterButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterConfig.Builder;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements OnConnectionFailedListener {
    private static final String EMAIL = "email";
    private static final String KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress";
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "SignInActivity";
    private static final String TAG1 = "PhoneAuthActivity";
    private Dialog ThisDialog;
    private GoogleSignInAccount account;
    private ImageButton btn_anonymous;
    private Button btn_autor;
    private ImageButton btn_gitHub_login;
    private ImageButton btn_keypad;
    private FacebookButton btn_login_facebook;
    private LoginButton btn_login_facebook1;
    private GoogleButton btn_login_google;
    private Button btn_regis;
    private SignInButton btn_sign_in_google;
    private Button btn_sign_out;
    private TwitterLoginButton btn_twitter;
    private TwitterButton btn_twitter_login;
    private TextInputEditText edit_login;
    private TextInputEditText edit_passw;
    private GoogleApiClient googleApiClient;
    private FirebaseAuth mAuth;
    private CallbackManager mCallbackManager = Factory.create();
    private OnVerificationStateChangedCallbacks mCallbacks;
    private ForceResendingToken mResendToken;
    private String mVerificationId;
    private boolean mVerificationInProgress = false;
    private FirebaseUser user;
    private String SUCCESSFUL_SIGN_IN = "Успішний вхід";
    private String UNSUCCESSFUL_SIGN_IN = "Неуспішний вхід";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Twitter.initialize(new Builder(this).logger(new DefaultLogger(3)).twitterAuthConfig(new TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret))).debug(true).build());
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()).build();

        edit_login = findViewById(R.id.edit_login);
        edit_passw = findViewById(R.id.edit_password);
        btn_autor = findViewById(R.id.btn_autor);
        btn_regis = findViewById(R.id.btn_regis);
        btn_sign_in_google = findViewById(R.id.btn_sign_in_b);
        btn_gitHub_login = findViewById(R.id.btn_github);
        btn_twitter_login = findViewById(R.id.btn_twitter_login);
        btn_login_facebook = findViewById(R.id.btn_facebook_login);
        btn_login_facebook1 = findViewById(R.id.btn_facebook_login1);
        btn_login_google = findViewById(R.id.btn_google_login);
        btn_keypad = findViewById(R.id.btn_keypad);
        btn_anonymous = findViewById(R.id.btn_anonymous);
        btn_twitter = findViewById(R.id.btn_twitter);

        btn_login_facebook1.setReadPermissions(EMAIL, "public_profile");


        mCallbacks = new OnVerificationStateChangedCallbacks() {
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                mVerificationInProgress = false;
                signInWithPhoneAuthCredential(credential);
            }

            public void onVerificationFailed(FirebaseException e) {
                mVerificationInProgress = false;
                if (!(e instanceof FirebaseAuthInvalidCredentialsException) && (e instanceof FirebaseTooManyRequestsException)) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.", -1).show();
                }
            }

            public void onCodeSent(String verificationId, ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        LoginManager.getInstance().registerCallback(this.mCallbackManager, new FacebookCallback<LoginResult>() {
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            public void onCancel() {
            }

            public void onError(FacebookException exception) {
            }
        });

        btn_login_google.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignInGoogle();
            }
        });

        btn_regis.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Registration(edit_login.getText().toString(), edit_passw.getText().toString());
            }
        });

        btn_autor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Authentication(edit_login.getText().toString(), MainActivity.this.edit_passw.getText().toString());
            }
        });

        btn_sign_in_google.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SignInGoogle();
            }
        });

        btn_login_facebook.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                btn_login_facebook1.performClick();
            }
        });

        btn_twitter_login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                btn_twitter.performClick();
            }
        });

        btn_twitter.setCallback(new Callback<TwitterSession>() {
            public void success(Result<TwitterSession> result) {
                handleTwitterSession((TwitterSession) result.data);
            }

            public void failure(TwitterException exception) {
            }
        });

        btn_gitHub_login.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                signInWithGitHub();
            }
        });

        btn_keypad.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ShowNumberDialog();
            }
        });

        btn_anonymous.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                signInAnonymously();
            }
        });
    }

    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity_2.class));
        }
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(edit_login.getText().toString());
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        btn_twitter.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            handleSignResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
        }
    }

    //todo Registration with email and password
    public void Registration(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    ShowToast(SUCCESSFUL_SIGN_IN);
                    MainActivity.this.updateUI(mAuth.getCurrentUser());
                    return;
                }else {
                    ShowToast(SUCCESSFUL_SIGN_IN);
                    MainActivity.this.updateUI(null);
                }

            }
        });
    }

    //todo Authentication with email and password
    public void Authentication(String email, String password) {
        this.mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    ShowToast(SUCCESSFUL_SIGN_IN);
                    MainActivity.this.startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                    return;
                }else{
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                }
            }
        });
    }

    //todo Sign in with Google
    private void SignInGoogle() {
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), RC_SIGN_IN);
    }

    private void handleSignResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()) {
            account = googleSignInResult.getSignInAccount();
            firebaseAuthWithGoogle(account);
            return;
        }else {
            ShowToast(UNSUCCESSFUL_SIGN_IN);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(acct.getIdToken(), null)).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                }else{
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                }
            }
        });
    }


    //todo Sign in with Facebook
    private void handleFacebookAccessToken(AccessToken token) {
        mAuth.signInWithCredential(FacebookAuthProvider.getCredential(token.getToken())).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    user = MainActivity.this.mAuth.getCurrentUser();
                    startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                }else{
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                    updateUI(null);
                }
            }
        });
    }


    //todo Sign in with Twitter
    private void handleTwitterSession(TwitterSession session) {
        mAuth.signInWithCredential(TwitterAuthProvider.getCredential(((TwitterAuthToken) session.getAuthToken()).token, ((TwitterAuthToken) session.getAuthToken()).secret)).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user =mAuth.getCurrentUser();
                    startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                    return;
                }else {
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                    updateUI(null);
                }

            }
        });
    }


    //todo Sign in with GitHub
    public void signInWithGitHub() {
        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithCredential(GithubAuthProvider.getCredential(getResources().getString(R.string.GitHubAccessToken))).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    mAuth.getCurrentUser();
                } else {
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                }
            }
        });
    }

    //do Sign in with Phone number
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = ((AuthResult) task.getResult()).getUser();
                    startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                } else if (!(task.getException() instanceof FirebaseAuthInvalidCredentialsException)) {
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                }
            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, (Activity) this, mCallbacks);
        mVerificationInProgress = true;
    }

    private void verifyPhoneNumberWithCode(String verificationId, String code) {
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId, code));
    }

    private void resendVerificationCode(String phoneNumber, ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, (Activity) this, mCallbacks, token);
    }

    private boolean validatePhoneNumber() {
        if (TextUtils.isEmpty(edit_login.getText().toString())) {
            return false;
        }
        return true;
    }

    private void ShowNumberDialog() {
        ThisDialog = new Dialog(this);
        ThisDialog.setTitle("Перевірка номеру");
        ThisDialog.setContentView(R.layout.dialog_template);
        final EditText Write = ThisDialog.findViewById(R.id.write);
        Button SaveMyName = ThisDialog.findViewById(R.id.SaveNow);
        Write.setEnabled(true);
        SaveMyName.setEnabled(true);
        @SuppressLint("WrongConstant") TelephonyManager telephonyManager = (TelephonyManager) getSystemService(PhoneAuthProvider.PROVIDER_ID);
        if (ActivityCompat.checkSelfPermission(this, "android.permission.READ_SMS") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.READ_PHONE_STATE") == 0) {
            Write.setText(telephonyManager.getLine1Number().toString());
            SaveMyName.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    startPhoneNumberVerification(Write.getText().toString());
                    ThisDialog.cancel();
                }
            });
            ThisDialog.show();
        }
    }


    //todo Sign in Anonymously
    private void signInAnonymously() {
        mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                    return;
                }else{
                    ShowToast(UNSUCCESSFUL_SIGN_IN);
                }
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, user.getUid() + "," + user.getEmail(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ShowToast(String message){
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}