package com.ciuc.andrii.firebase_1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookSdk;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GithubAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.shaishavgandhi.loginbuttons.BaseButton;
import com.shaishavgandhi.loginbuttons.FacebookButton;
import com.shaishavgandhi.loginbuttons.GoogleButton;
import com.shaishavgandhi.loginbuttons.TwitterButton;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private FirebaseAuth mAuth;

    TextInputEditText edit_login,edit_passw;
    Button btn_autor,btn_regis;

    SignInButton btn_sign_in_google;
    Button btn_sign_out;
    TextView text1;
    ImageButton imageView;
    LoginButton btn_login_facebook1;
    FacebookButton btn_login_facebook;
    GoogleButton btn_login_google;
    TwitterButton btn_twitter_login;
    BaseButton btn_gitHub_login;

    GoogleApiClient googleApiClient;
    GoogleSignInAccount account;
    FirebaseUser user;

    ProfilePictureView profilePictureView;

    private TwitterLoginButton btn_twitter;

    private static final String EMAIL = "email";

    CallbackManager mCallbackManager = CallbackManager.Factory.create();
    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;
    String user_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        //Toast.makeText(MainActivity.this, mAuth.getCurrentUser().getPhotoUrl().toString(),
                //Toast.LENGTH_SHORT).show();


       // Uri uri = mAuth.getCurrentUser().getPhotoUrl();
        //Picasso.with(this).load(uri).into(imageView);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,gso).build();

        edit_login = findViewById(R.id.edit_login);
        edit_passw = findViewById(R.id.edit_password);

        btn_autor = findViewById(R.id.btn_autor);
        btn_regis = findViewById(R.id.btn_regis);

        btn_sign_in_google = findViewById(R.id.btn_sign_in_b);
        btn_sign_out = findViewById(R.id.btn_sing_out);

        text1 = findViewById(R.id.text1);

        btn_gitHub_login = findViewById(R.id.btn_github);

        btn_twitter_login = findViewById(R.id.btn_twitter_login);




        btn_login_facebook = findViewById(R.id.btn_facebook_login);
        //btn_login_facebook.setReadPermissions("email", "public_profile");

        btn_login_facebook1 = findViewById(R.id.btn_facebook_login1);
        btn_login_facebook1.setReadPermissions("email", "public_profile");

        btn_login_google = findViewById(R.id.btn_google_login);

        btn_login_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // btn_sign_in_google.performClick();
                SignInGoogle();
            }
        });

        btn_twitter_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });



        btn_twitter = findViewById(R.id.btn_twitter);
        /*
        btn_twitter.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Log.d(TAG, "twitterLogin:success" + result);
                handleTwitterSession(result.data);
            }

            @Override
            public void failure(TwitterException exception) {
                Log.w(TAG, "twitterLogin:failure", exception);
                updateUI(null);
            }
        });*/

        btn_login_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_login_facebook1.performClick();
            }
        });

        LoginManager.getInstance().registerCallback(mCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        handleFacebookAccessToken(loginResult.getAccessToken());

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });


        /*
        btn_login_facebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {}
        });*/


        btn_sign_in_google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignInGoogle();
            }
        });

        btn_sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SignOutGoogle();
            }
        });

      /*  FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(MainActivity.this, TasksActivityListV.class));
        }*/

        btn_autor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Authentication(edit_login.getText().toString(),edit_passw.getText().toString());
            }
        });

        btn_regis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registration(edit_login.getText().toString(),edit_passw.getText().toString());
            }
        });


        /*btn_gitHub_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signWithGitHub();
            }
        });*/
    }

    private void handleTwitterSession(TwitterSession session) {
        AuthCredential credential = TwitterAuthProvider.getCredential(session.getAuthToken().token,session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }



  /*  protected Bitmap doInBackground(String... url) {

        String urldisplay = url[0];
        Bitmap bitmap = null;
        try {
            InputStream srt = new java.net.URL(urldisplay).openStream();
            bitmap = BitmapFactory.decodeStream(srt);
        } catch (Exception e){
            e.printStackTrace();
        }
        return bitmap;
    }*/
/*
    public void sendLoginGithubData( View view ){

    /
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("github.com")
                .appendPath("login")
                .appendPath("oauth")
                .appendPath("authorize")
                .appendQueryParameter("client_id", getString(R.string.github_app_id))
                .appendQueryParameter("scope", "user,user:email")
                .build();


        WebView webView = new WebView(this);
        webView.loadUrl( uri.toString() );
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Uri uri = Uri.parse(url);
                if( uri.getQueryParameter("code") != null
                        && uri.getScheme() != null
                        && uri.getScheme().equalsIgnoreCase("https") ){

                    requestGitHubUserAccessToken( uri.getQueryParameter("code") );
                }
                return super.shouldOverrideUrlLoading(view, url);
            }


        });


        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Login GitHub");
        alert.setView(webView);
        alert.show();
    }

    private void requestGitHubUserAccessToken( String code ){
        RequestTokenClient requestTokenClient = new RequestTokenClient(
                code,
                getString(R.string.github_app_id),
                getString(R.string.github_app_secret),
                getString(R.string.github_app_url)
        );

        requestTokenClient
                .observable()
                .subscribe(new Observer<Token>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        showSnackbar( e.getMessage() );
                    }

                    @Override
                    public void onNext(Token token) {
                        if( token.access_token != null ){
                            requestGitHubUserData( token.access_token );
                        }
                    }
                });
    }

    private void requestGitHubUserData( final String accessToken ){
        GetAuthUserClient getAuthUserClient = new GetAuthUserClient( accessToken );
        getAuthUserClient
                .observable()
                .subscribe(new Observer<com.alorma.github.sdk.bean.dto.response.User>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {}

                    @Override
                    public void onNext(com.alorma.github.sdk.bean.dto.response.User user) {
                        LoginActivity.this.user.setName( user.name );
                        LoginActivity.this.user.setEmail( user.email );

                        accessGithubLoginData( accessToken );
                    }
                });
    }

    private void accessGithubLoginData(String accessToken){
        accessLoginData(
                "github",
                accessToken
        );
    }

 */
    private void handleFacebookAccessToken(AccessToken token) {
        user_id = token.getUserId();
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                           // user = mAuth.getCurrentUser();
                            Toast.makeText(MainActivity.this, user.getDisplayName(),
                                    Toast.LENGTH_SHORT).show();
                           // updateUI(user);
                            //Uri uri = user.getPhotoUrl();
                            //imageView.setImageURI(uri);
                            //Toast.makeText(MainActivity.this, uri.toString(),
                                 //   Toast.LENGTH_SHORT).show();
                            /*
                            profilePictureView.setProfileId(user.getUid());*/
                            /*Picasso.with(MainActivity.this)
                                    .load("https://graph.facebook.com/" + user.ge+ "/picture?type=large")
                                    .into(userpicture);*/
                            //imageView.setImageURI(uri);
                            /*try {
                                Bitmap bitmap = getFacebookProfilePicture(user.getUid());
                                imageView.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                    }
                });
    }

   /* public static Bitmap getFacebookProfilePicture(String userID) throws IOException {
        URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?type=large");
        Bitmap bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());

        return bitmap;
    }
*/


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        /*FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        //btn_twitter.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignResult(googleSignInResult);
        }
    }

    private void handleSignResult(GoogleSignInResult googleSignInResult) {
        if (googleSignInResult.isSuccess()){
            account = googleSignInResult.getSignInAccount();

            text1.setText("Hi, " + account.getDisplayName());
            //startActivity(new Intent(MainActivity.this, MainActivity_2.class)/*.putExtra("account",account)*/);
        }
    }


    public void Authentication(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Авторизація успішна.",
                                    Toast.LENGTH_SHORT).show();
                           /* FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);*/
                            startActivity(new Intent(MainActivity.this, MainActivity_2.class));
                        } else {
                            Toast.makeText(MainActivity.this, "Авторизація провалена.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    public void registration(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Реєстрація успішна.",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            Toast.makeText(MainActivity.this, "Реєстрація провалена.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(MainActivity.this,user.getUid() + "," + user.getEmail(),Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    private void SignInGoogle(){
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    private void SignOutGoogle(){
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                text1.setText("Signed Out");
            }
        });
    }





    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


    private void signWithGitHub() {
        String token = "efe8a24902a62aa6d6421aadd5060722473dff98";
        AuthCredential credential = GithubAuthProvider.getCredential(token);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Authentication good.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
        });
    }

/*
    public class User {
    ...
        public void setNameIfNull(String name) {
            if( this.name == null ){
                this.name = name;
            }
        }
    ...
        public void setEmailIfNull(String email) {
            if( this.email == null ){
                this.email = email;
            }

        }
    ...
    }
*/
    /*
    getHigherResProviderPhotoUrl = ({ photoURL, providerId }: any): ?string => {
        //workaround to get higer res profile picture
        Uri result = photoURL;
        if (providerId.includes('google')) {
            result = photoURL.replace('s96-c', 's400-c');
        } else if (providerId.includes('facebook')) {
            result = `${photoURL}?type=large`;
        }
        return result;
    };*/
}
