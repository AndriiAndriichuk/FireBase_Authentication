package com.ciuc.andrii.firebase_authentication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.TextInputEditText
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast

import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GithubAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.shaishavgandhi.loginbuttons.FacebookButton
import com.shaishavgandhi.loginbuttons.GoogleButton
import com.shaishavgandhi.loginbuttons.TwitterButton
import com.twitter.sdk.android.core.Callback
import com.twitter.sdk.android.core.DefaultLogger
import com.twitter.sdk.android.core.Result
import com.twitter.sdk.android.core.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import com.twitter.sdk.android.core.TwitterAuthToken
import com.twitter.sdk.android.core.TwitterConfig
import com.twitter.sdk.android.core.TwitterException
import com.twitter.sdk.android.core.TwitterSession
import com.twitter.sdk.android.core.identity.TwitterLoginButton
import kotlinx.android.synthetic.main.activity_main.*

import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener {
    private var ThisDialog: Dialog? = null
    private var account: GoogleSignInAccount? = null
    private var googleApiClient: GoogleApiClient? = null
    private var mAuth: FirebaseAuth? = null
    private val mCallbackManager = CallbackManager.Factory.create()
    private var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var mResendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mVerificationId: String? = null
    private var mVerificationInProgress = false
    private var user: FirebaseUser? = null
    private val SUCCESSFUL_SIGN_IN = "Успішний вхід"
    private val UNSUCCESSFUL_SIGN_IN = "Неуспішний вхід"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Twitter.initialize(TwitterConfig.Builder(this).logger(DefaultLogger(3)).twitterAuthConfig(TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret))).debug(true).build())
        FacebookSdk.sdkInitialize(applicationContext)

        mAuth = FirebaseAuth.getInstance()
        setContentView(R.layout.activity_main)


        googleApiClient = GoogleApiClient.Builder(this).enableAutoManage(this, this).addApi(Auth.GOOGLE_SIGN_IN_API, GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(0x7f0e004a.toString()).requestEmail().build()).build()

        btn_facebook_login1.setReadPermissions(EMAIL, "public_profile")


        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                mVerificationInProgress = false
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                mVerificationInProgress = false
                if (e !is FirebaseAuthInvalidCredentialsException && e is FirebaseTooManyRequestsException) {
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.", -1).show()
                }
            }

            override fun onCodeSent(verificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                mVerificationId = verificationId
                mResendToken = token
            }
        }
        LoginManager.getInstance().registerCallback(this.mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {}

            override fun onError(exception: FacebookException) {}
        })

        btnGoogle.setOnClickListener { SignInGoogle() }

        btnRegister.setOnClickListener { Registration(editLogin.text.toString(), editPassword.text.toString()) }

        btnAuthorize.setOnClickListener { Authentication(editLogin.text.toString(), editPassword.text.toString()) }

        btnGoogle.setOnClickListener { SignInGoogle() }

        btnFacebook.setOnClickListener { btn_facebook_login1.performClick() }

        btnTwitter.setOnClickListener { btn_twitter.performClick() }

        btn_twitter.callback = object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>) {
                handleTwitterSession(result.data as TwitterSession)
            }

            override fun failure(exception: TwitterException) {}
        }

        btnGithub.setOnClickListener { signInWithGitHub() }

        btnPhoneNumber.setOnClickListener { ShowNumberDialog() }

        btnAnonymous.setOnClickListener { signInAnonymously() }
    }

    public override fun onStart() {
        super.onStart()
        /* if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity_2.class));
        }*/
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(editLogin.text.toString())
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)
        mCallbackManager.onActivityResult(requestCode, resultCode, data)
        btn_twitter!!.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            handleSignResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data))
        }
    }

    //todo Registration with email and password
    fun Registration(email: String, password: String) {
        mAuth!!.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                ShowToast(SUCCESSFUL_SIGN_IN)
                this@MainActivity.updateUI(mAuth!!.currentUser)
                return@OnCompleteListener
            } else {
                ShowToast(SUCCESSFUL_SIGN_IN)
                this@MainActivity.updateUI(null)
            }
        })
    }

    //todo Authentication with email and password
    fun Authentication(email: String, password: String) {
        this.mAuth!!.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                ShowToast(SUCCESSFUL_SIGN_IN)
                this@MainActivity.startActivity(Intent(this@MainActivity, MainActivity_2::class.java))
                return@OnCompleteListener
            } else {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
            }
        })
    }

    //todo Sign in with Google
    private fun SignInGoogle() {
        startActivityForResult(Auth.GoogleSignInApi.getSignInIntent(googleApiClient), RC_SIGN_IN)
    }

    private fun handleSignResult(googleSignInResult: GoogleSignInResult) {
        if (googleSignInResult.isSuccess) {
            account = googleSignInResult.signInAccount
            firebaseAuthWithGoogle(account!!)
            return
        } else {
            ShowToast(UNSUCCESSFUL_SIGN_IN)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        mAuth!!.signInWithCredential(GoogleAuthProvider.getCredential(acct.idToken, null)).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = mAuth!!.currentUser
                startActivity(Intent(this@MainActivity, MainActivity_2::class.java))
            } else {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
            }
        }
    }


    //todo Sign in with Facebook
    private fun handleFacebookAccessToken(token: AccessToken) {
        mAuth!!.signInWithCredential(FacebookAuthProvider.getCredential(token.token)).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                user = this@MainActivity.mAuth!!.currentUser
                startActivity(Intent(this@MainActivity, MainActivity_2::class.java))
            } else {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
                updateUI(null)
            }
        }
    }


    //todo Sign in with Twitter
    private fun handleTwitterSession(session: TwitterSession) {
        mAuth!!.signInWithCredential(TwitterAuthProvider.getCredential((session.authToken as TwitterAuthToken).token, (session.authToken as TwitterAuthToken).secret)).addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = mAuth!!.currentUser
                startActivity(Intent(this@MainActivity, MainActivity_2::class.java))
                return@OnCompleteListener
            } else {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
                updateUI(null)
            }
        })
    }


    //todo Sign in with GitHub
    fun signInWithGitHub() {
        val mAuth = FirebaseAuth.getInstance()
        mAuth.signInWithCredential(GithubAuthProvider.getCredential(resources.getString(R.string.GitHubAccessToken))).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                mAuth.currentUser
            } else {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
            }
        }
    }

    //do Sign in with Phone number
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = (task.result as AuthResult).user
                startActivity(Intent(this@MainActivity, MainActivity_2::class.java))
            } else if (task.exception !is FirebaseAuthInvalidCredentialsException) {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this as Activity, mCallbacks!!)
        mVerificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String, code: String) {
        signInWithPhoneAuthCredential(PhoneAuthProvider.getCredential(verificationId, code))
    }

    private fun resendVerificationCode(phoneNumber: String, token: PhoneAuthProvider.ForceResendingToken) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber, 60, TimeUnit.SECONDS, this as Activity, mCallbacks!!, token)
    }

    private fun validatePhoneNumber(): Boolean {
        return if (TextUtils.isEmpty(editLogin.text.toString())) {
            false
        } else true
    }

    private fun ShowNumberDialog() {
        ThisDialog = Dialog(this)
        ThisDialog!!.setTitle("Перевірка номеру")
        ThisDialog!!.setContentView(R.layout.dialog_template)
        val Write = ThisDialog!!.findViewById<EditText>(R.id.write)
        val SaveMyName = ThisDialog!!.findViewById<Button>(R.id.SaveNow)
        Write.isEnabled = true
        SaveMyName.isEnabled = true
        @SuppressLint("WrongConstant") val telephonyManager = getSystemService(PhoneAuthProvider.PROVIDER_ID) as TelephonyManager
        if (ActivityCompat.checkSelfPermission(this, "android.permission.READ_SMS") == 0 || ActivityCompat.checkSelfPermission(this, "android.permission.READ_PHONE_STATE") == 0) {
            Write.setText(telephonyManager.line1Number.toString())
            SaveMyName.setOnClickListener {
                startPhoneNumberVerification(Write.text.toString())
                ThisDialog!!.cancel()
            }
            ThisDialog!!.show()
        }
    }


    //todo Sign in Anonymously
    private fun signInAnonymously() {
        mAuth!!.signInAnonymously().addOnCompleteListener(this, OnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = mAuth!!.currentUser
                startActivity(Intent(this@MainActivity, MainActivity_2::class.java))
                return@OnCompleteListener
            } else {
                ShowToast(UNSUCCESSFUL_SIGN_IN)
            }
        })
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            Toast.makeText(this, user.uid + "," + user.email, Toast.LENGTH_SHORT).show()
        }
    }

    private fun ShowToast(message: String) {
        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    companion object {
        private val EMAIL = "email"
        private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private val RC_SIGN_IN = 9001
        private val TAG = "SignInActivity"
        private val TAG1 = "PhoneAuthActivity"
    }
}
