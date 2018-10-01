package com.ciuc.andrii.firebase_1;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity_2 extends AppCompatActivity implements OnConnectionFailedListener {
    Button btn_log_out;
    GoogleApiClient googleApiClient;
    CircleImageView image_provider;
    CircleImageView image_user_p;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    TextView text_displayName;
    TextView text_email;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_2);
        text_displayName = findViewById(R.id.text_displayName);
        text_email = findViewById(R.id.text_email);
        btn_log_out = findViewById(R.id.btn_log_out);
        image_user_p = findViewById(R.id.image_user_p);
        image_provider = findViewById(R.id.image_provider);
        mAuth = FirebaseAuth.getInstance();
        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if (mUser != null) {
            switch (mUser.getProviders().toString()) {
                case "[google.com]":
                    googleApiClient = new Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build()).build();
                    text_displayName.setText(mUser.getDisplayName());
                    text_email.setText(mUser.getEmail());
                    if (mUser.getPhotoUrl() != null) {
                        SetUserPhoto(mUser.getPhotoUrl(), image_user_p);
                    } else {
                        image_user_p.setImageResource(R.drawable.img_empty_user);
                    }
                    image_provider.setImageResource(R.drawable.img_google);
                    break;
                case "[facebook.com]":
                    text_email.setText(mUser.getEmail());
                    if (mUser.getPhotoUrl() != null) {
                        SetUserPhoto(mUser.getPhotoUrl(), image_user_p);
                    } else {
                        image_user_p.setImageResource(R.drawable.img_empty_user);
                    }
                    image_provider.setImageResource(R.drawable.img_facebook);
                    break;
                case "[twitter.com]":
                    text_displayName.setText("@" + mUser.getDisplayName());
                    if (mUser.getPhotoUrl() != null) {
                        if (mUser.getPhotoUrl() != null) {
                            SetUserPhoto(mUser.getPhotoUrl(), image_user_p);
                        } else {
                            image_user_p.setImageResource(R.drawable.img_empty_user);
                        }
                    }
                    image_provider.setImageResource(R.drawable.img_twitter);
                    text_email.setVisibility(View.INVISIBLE);
                    break;
                case "[password]":
                    image_user_p.setImageResource(R.drawable.img_empty_user);
                    text_displayName.setText(mUser.getEmail());
                    image_provider.setImageResource(R.drawable.img_login);
                    text_email.setVisibility(View.INVISIBLE);
                    break;
                case "[github.com]":

                    break;
                case "[phone]":
                    image_user_p.setImageResource(R.drawable.img_empty_user);
                    text_displayName.setText(mUser.getPhoneNumber());
                    image_provider.setImageResource(R.drawable.img_keypad);
                    text_email.setVisibility(View.INVISIBLE);
                    break;
                case "[]":
                    image_user_p.setImageResource(R.drawable.img_empty_user);
                    text_displayName.setText("Анонімний користувач");
                    image_provider.setImageResource(R.drawable.img_user);
                    text_email.setVisibility(View.INVISIBLE);
                    break;
            }
        }

        btn_log_out.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (mUser.getProviders().toString()) {
                    case "[google.com]":
                        FirebaseAuth.getInstance().signOut();
                        Auth.GoogleSignInApi.signOut(MainActivity_2.this.googleApiClient).setResultCallback(new ResultCallback<Status>() {
                            public void onResult(@NonNull Status status) {
                            }
                        });
                        break;
                    case "[facebook.com]":
                        FirebaseAuth.getInstance().signOut();
                        LoginManager.getInstance().logOut();
                        break;
                    case "[twitter.com]":
                        FirebaseAuth.getInstance().signOut();
                        break;
                    case "[password]":
                        FirebaseAuth.getInstance().signOut();
                        break;
                    case "[github.com]":
                        FirebaseAuth.getInstance().signOut();
                        break;
                    case "[phone]":
                        FirebaseAuth.getInstance().signOut();
                        break;
                    case "[]":
                        MainActivity_2.this.mAuth.signOut();
                        break;
                }
                startActivity(new Intent(MainActivity_2.this, MainActivity.class));
                finish();
            }
        });
    }
    private void SetUserPhoto(Uri uri, ImageView circleImageView) {
        Picasso.with(getBaseContext()).load(uri).into((ImageView) circleImageView);
    }

    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    public void onPointerCaptureChanged(boolean hasCapture) {
    }

}