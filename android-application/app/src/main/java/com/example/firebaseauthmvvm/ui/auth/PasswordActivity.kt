package com.example.firebaseauthmvvm.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.ActivityLoginBinding
import com.example.firebaseauthmvvm.databinding.ActivityPasswordBinding
import com.example.firebaseauthmvvm.utils.startLoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class PasswordActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory: AuthViewModelFactory by instance()

    private lateinit var viewModel: AuthViewModel

    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_password)

        val binding: ActivityPasswordBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_password)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

    }

    override fun onBackPressed() {
        super.onBackPressed()
        startLoginActivity()
    }
}