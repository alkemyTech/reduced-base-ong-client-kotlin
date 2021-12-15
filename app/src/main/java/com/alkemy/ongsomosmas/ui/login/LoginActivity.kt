package com.alkemy.ongsomosmas.ui.login

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.alkemy.ongsomosmas.R
import com.alkemy.ongsomosmas.databinding.ActivityLoginBinding
import com.alkemy.ongsomosmas.ui.home.HomeActivity
import com.alkemy.ongsomosmas.utils.EventConstants
import com.alkemy.ongsomosmas.utils.sendLog
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        // facebook login setup
        binding.ibFacebook.setOnClickListener {
            loginWithFacebook()
            sendLog(EventConstants.FB_PRESSED, "User has pressed the facebook button")
        }

        binding.btnLogin.setOnClickListener {
            startActivity(Intent(applicationContext, HomeActivity::class.java))
            finish()
        }
    }

    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null)
        auth.currentUser
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    // facebook login transaction
    private fun loginWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, setOf("email"))
        LoginManager.getInstance().registerCallback(
            callbackManager, object : FacebookCallback<com.facebook.login.LoginResult> {
                override fun onSuccess(loginResult: com.facebook.login.LoginResult?) {
                    Log.d(TAG, "facebook: onSuccess: $loginResult")
                    if (loginResult != null) {
                        handleFacebookAccessToken(loginResult.accessToken)
                    }
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    finish()
                }

                override fun onCancel() {
                    Log.d(TAG, "facebook: onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d(TAG, "facebook: onError", error)
                    Toast.makeText(
                        baseContext, R.string.facebook_auth_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d(TAG, "handleFacebookAccessToken: $token")

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d(TAG, "handleFacebookAccessToken: success")
                    auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "handleFacebookAccessToken: failure", task.exception)
                    Toast.makeText(
                        baseContext, R.string.facebook_auth_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}
