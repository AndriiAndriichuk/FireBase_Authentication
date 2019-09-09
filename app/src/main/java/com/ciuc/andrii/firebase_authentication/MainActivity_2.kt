package com.ciuc.andrii.firebase_authentication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.GoogleApiClient.Builder
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.squareup.picasso.Picasso

import de.hdodenhof.circleimageview.CircleImageView

class MainActivity_2 : AppCompatActivity(), OnConnectionFailedListener {
    internal lateinit var btn_log_out: Button
    internal lateinit var googleApiClient: GoogleApiClient
    internal lateinit var image_provider: CircleImageView
    internal lateinit var image_user_p: CircleImageView
    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    internal lateinit var text_displayName: TextView
    internal lateinit var text_email: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_2)
        text_displayName = findViewById(R.id.text_displayName)
        text_email = findViewById(R.id.text_email)
        btn_log_out = findViewById(R.id.btn_log_out)
        image_user_p = findViewById(R.id.image_user_p)
        image_provider = findViewById(R.id.image_provider)
        mAuth = FirebaseAuth.getInstance()
        mUser = FirebaseAuth.getInstance().currentUser

        if (mUser != null) {
            when (mUser!!.providers!!.toString()) {
                "[google.com]" -> {
                    googleApiClient = Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(0x7f0e004a.toString()).requestEmail().build()).build()
                    text_displayName.text = mUser!!.displayName
                    text_email.text = mUser!!.email
                    if (mUser!!.photoUrl != null) {
                        SetUserPhoto(mUser!!.photoUrl, image_user_p)
                    } else {
                        image_user_p.setImageResource(R.drawable.img_empty_user)
                    }
                    image_provider.setImageResource(R.drawable.img_google)
                }
                "[facebook.com]" -> {
                    text_email.text = mUser!!.email
                    if (mUser!!.photoUrl != null) {
                        SetUserPhoto(mUser!!.photoUrl, image_user_p)
                    } else {
                        image_user_p.setImageResource(R.drawable.img_empty_user)
                    }
                    image_provider.setImageResource(R.drawable.img_facebook)
                }
                "[twitter.com]" -> {
                    text_displayName.text = "@" + mUser!!.displayName!!
                    if (mUser!!.photoUrl != null) {
                        if (mUser!!.photoUrl != null) {
                            SetUserPhoto(mUser!!.photoUrl, image_user_p)
                        } else {
                            image_user_p.setImageResource(R.drawable.img_empty_user)
                        }
                    }
                    image_provider.setImageResource(R.drawable.img_twitter)
                    text_email.visibility = View.INVISIBLE
                }
                "[password]" -> {
                    image_user_p.setImageResource(R.drawable.img_empty_user)
                    text_displayName.text = mUser!!.email
                    image_provider.setImageResource(R.drawable.img_login)
                    text_email.visibility = View.INVISIBLE
                }
                "[github.com]" -> {
                }
                "[phone]" -> {
                    image_user_p.setImageResource(R.drawable.img_empty_user)
                    text_displayName.text = mUser!!.phoneNumber
                    image_provider.setImageResource(R.drawable.img_keypad)
                    text_email.visibility = View.INVISIBLE
                }
                "[]" -> {
                    image_user_p.setImageResource(R.drawable.img_empty_user)
                    text_displayName.text = "Анонімний користувач"
                    image_provider.setImageResource(R.drawable.img_user)
                    text_email.visibility = View.INVISIBLE
                }
            }
        }

        btn_log_out.setOnClickListener {
            when (mUser!!.providers!!.toString()) {
                "[google.com]" -> {
                    FirebaseAuth.getInstance().signOut()
                    Auth.GoogleSignInApi.signOut(this@MainActivity_2.googleApiClient).setResultCallback { }
                }
                "[facebook.com]" -> {
                    FirebaseAuth.getInstance().signOut()
                    LoginManager.getInstance().logOut()
                }
                "[twitter.com]" -> FirebaseAuth.getInstance().signOut()
                "[password]" -> FirebaseAuth.getInstance().signOut()
                "[github.com]" -> FirebaseAuth.getInstance().signOut()
                "[phone]" -> FirebaseAuth.getInstance().signOut()
                "[]" -> this@MainActivity_2.mAuth!!.signOut()
            }
            startActivity(Intent(this@MainActivity_2, MainActivity::class.java))
            finish()
        }
    }

    private fun SetUserPhoto(uri: Uri?, circleImageView: ImageView) {
        Picasso.with(baseContext).load(uri).into(circleImageView)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

}