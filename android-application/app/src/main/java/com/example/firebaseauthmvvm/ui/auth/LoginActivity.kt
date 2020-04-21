package com.example.firebaseauthmvvm.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.ActivityLoginBinding
import com.example.firebaseauthmvvm.utils.startHomeActivity
import com.example.firebaseauthmvvm.utils.startSplashActivity
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.util.*


class LoginActivity : AppCompatActivity(), AuthListener, KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()
    private lateinit var binding: ActivityLoginBinding

    private lateinit var viewModel: AuthViewModel

    // Variables for Google sign in
    val RC_SIGN_IN: Int = 1
    lateinit var googleSignInClient: GoogleSignInClient
    lateinit var googleSignInOptions: GoogleSignInOptions

    private var firebaseAuth = FirebaseAuth.getInstance()

    private var callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        viewModel.authListener = this

        // Login Google
        configureGoogleSignIn()
        buttonGoogle()

        // Login Facebook
        val button_facebook = findViewById<Button>(R.id.log_with_facebook)

        button_facebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email",
                "public_profile", "user_photos"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        handleFacebookAccessToken(loginResult.accessToken)
                    }
                    override fun onCancel() {
                        Toast.makeText(it.context, "Ups, something went wront...", Toast.LENGTH_SHORT).show()
                    }
                    override fun onError(error: FacebookException?) {
                        Toast.makeText(it.context, "Ups, something went wront...", Toast.LENGTH_SHORT).show()
                    }
                })
        }

    }

    // Prevent to switch between activities
    override fun onBackPressed() {
        // super.onBackPressed()
        moveTaskToBack(true)
    }

    override fun onStarted() {
        progressbar.visibility = View.VISIBLE
        viewModel.user?.let {
            // startHomeActivity()
            startSplashActivity()
        }
    }

    override fun onSuccess() {
        progressbar.visibility = View.GONE
        // startHomeActivity()
        startSplashActivity()
    }

    override fun onFailure(message: String) {
        progressbar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        viewModel.user?.let {
            // startHomeActivity()
            startSplashActivity()
        }
    }

    // LOGIN WITH GOOGLE
    private fun configureGoogleSignIn(){
        googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun buttonGoogle() {
        log_with_google.setOnClickListener{
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // GOOGLE
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                Log.d("Google account", account.toString())
                if (account != null) {
                    firebaseAuthWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed $e", Toast.LENGTH_LONG).show()
                Log.e("Google error", e.toString())
            }
        }

        // FACEBOOK
        callbackManager.onActivityResult(requestCode, resultCode, data)

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        Log.e("Google Sign in", "Test $acct")
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task->
            if (task.isSuccessful) {
                val uid = task.result?.user?.uid
                val username = task.result?.user?.displayName
                val imageUrl = "${task.result?.user?.photoUrl}?height=500"
                val email = task.result?.user?.email
                viewModel.saveUserToFirebaseDatabase(uid!!, username!!, email!!, imageUrl, binding.root)

            } else {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("LoginActivity", "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("--LoginActivity", "signInWithCredential:success")
                    Log.d("LoginActivity", "displayName: ${task.result?.user?.displayName}, ${task.result?.user?.photoUrl}")
                    val uid = task.result?.user?.uid
                    val username = task.result?.user?.displayName
                    val imageUrl = "${task.result?.user?.photoUrl}?height=500"
                    val email = task.result?.user?.email

                    viewModel.saveUserToFirebaseDatabase(uid!!, username!!, email!!, imageUrl, binding.root)

                } else {
                    // If sign in fails, display a message to the user.
                    Log.d("--LoginActivity", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
