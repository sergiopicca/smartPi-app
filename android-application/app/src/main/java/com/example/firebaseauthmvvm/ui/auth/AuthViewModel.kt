package com.example.firebaseauthmvvm.ui.auth

import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.firebaseauthmvvm.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import com.example.firebaseauthmvvm.data.repositories.UserRepository
import com.example.firebaseauthmvvm.database.RoomUser
import com.example.firebaseauthmvvm.models.User
import com.example.firebaseauthmvvm.utils.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_signup.view.*


class AuthViewModel(
    private val repository: UserRepository
) : ViewModel() {

    //email and password for the input
    var username: String? = null
    var email: String? = null
    var password: String? = null

    //auth listener
    var authListener: AuthListener? = null

    //disposable to dispose the Completable
    private val disposables = CompositeDisposable()

    val user by lazy {
        repository.currentUser()
    }

    //function to perform login
    fun login() {

        //validating email and password
        if (email.isNullOrEmpty() || password.isNullOrEmpty()) {
            authListener?.onFailure("Invalid email or password")
            return
        }

        //authentication started
        authListener?.onStarted()

        //calling login from repository to perform the actual authentication
        val disposable = repository.login(email!!, password!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //sending a success callback
                authListener?.onSuccess()
            }, {
                //sending a failure callback
                authListener?.onFailure(it.message!!)
            })
        disposables.add(disposable)
    }

    fun goToSignup(view: View) {
        view.context.startSignupActivity()
    }

    fun goToLogin(view: View) {
        view.context.startLoginActivity()
    }

    fun goToPassword(view: View) {
        view.context.startPasswordActivity()
    }

    fun goToExistingHouse(){
        TODO()
    }

    fun reset(view: View) {
        Log.d("ResetPassword", "Email: $email")
        if (email == null) {
            Toast.makeText(view.context, "Enter Email", Toast.LENGTH_SHORT).show()
        } else {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(view.context, "Email sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("ResetPassword", task.exception!!.message!!)
                        Toast.makeText(view.context, "No user found with this email.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    fun logout(view: View){
        repository.logout()
        view.context.startLoginActivity()
    }

    // Check if to save the user or not
    fun saveUserToFirebaseDatabase(uid: String, username: String, email: String, profileImageUrl: String, view: View) {
        val ref = FirebaseDatabase.getInstance().getReference("/users")
        ref.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.e("Error", p0.toString())
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    // Hide the progress bar
                    view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
                    view.context.startSplashActivity()
                }
                else{
                    val user = RoomUser(uid, email, username, "","", profileImageUrl)
                    ref.child(uid).setValue(user)
                    // Hide the progress bar
                    view.findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
                    Toast.makeText(view.context, "Ok, registered correctly!", Toast.LENGTH_LONG).show()
                    view.context.startSplashActivity()
                }
            }
        })
    }

    // Perform the signup USING THE FORM
    fun signup(username: String, email:String, password:String, view: View){
        if(username.isNullOrBlank() || email.isNullOrBlank() || password.isNullOrBlank()){
            Toast.makeText(view.context, "Fields missing...",Toast.LENGTH_LONG).show()
        }
        else{
            // First of all, verify if the email is written correctly
            val emailChecker = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
            Log.d("AuthViewModel", "emailChecker: $emailChecker -- $email")

            // If the email is written correctly
            if (emailChecker){
                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful){
                        val startingImgUrl = "https://image.flaticon.com/icons/png/512/149/149071.png"
                        saveUserToFirebaseDatabase(it.result?.user?.uid!!, username, email, startingImgUrl, view)
                    }
                }
            }
            // The email is written badly
            else{
                Toast.makeText(view.context, "Please write the email correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //disposing the disposables
    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}