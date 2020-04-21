package com.example.firebaseauthmvvm.ui.auth

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.example.firebaseauthmvvm.R
import com.example.firebaseauthmvvm.databinding.ActivitySignupBinding
import com.example.firebaseauthmvvm.utils.startLoginActivity
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class SignupActivity : AppCompatActivity(), KodeinAware {

    override val kodein by kodein()
    private val factory : AuthViewModelFactory by instance()

    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_signup)

        val binding: ActivitySignupBinding = DataBindingUtil.setContentView(this, R.layout.activity_signup)
        viewModel = ViewModelProviders.of(this, factory).get(AuthViewModel::class.java)
        binding.viewmodel = viewModel

        binding.buttonSignUp.setOnClickListener {
            // Get useful info
            val username = binding.textUsernameSignup.text.toString()
            val password = binding.editTextPassword.text.toString()
            val email = binding.textEmailSignup.text.toString()

            // Setting the visibility of the progress bar
            binding.progressbar.visibility = View.VISIBLE
            // Perform the signup
            viewModel.signup(username,email,password,binding.root)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startLoginActivity()
    }
}
