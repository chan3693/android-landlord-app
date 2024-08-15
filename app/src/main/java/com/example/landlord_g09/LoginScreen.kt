package com.example.landlord_g09

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.landlord_g09.databinding.ActivityLoginScreenBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginScreen : AppCompatActivity() {
    lateinit var binding: ActivityLoginScreenBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.myToolbar)

        //initialize Firebase auth
        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            val emailFromUI = binding.userEmail.text.toString()
            val passwordFromUI = binding.etPassword.text.toString()
            loginUser(emailFromUI, passwordFromUI)
            binding.tvResults.isVisible = true
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser!=null){
            val intent = Intent(this@LoginScreen, CreateListingScreen::class.java)
            intent.putExtra("KEY_IS_CREATE", true)
            startActivity(intent)
        }
    }

    // login handler
    fun loginUser(email:String, password:String) {
        if (auth.currentUser != null) {
            binding.tvResults.text = "User already logged in"
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this){
                    task ->
                if (task.isSuccessful){
                    Log.d("TESTING", "singInWithEmail:success")
                    binding.tvResults.text = "${email} logged in successfully"
                    val intent = Intent(this@LoginScreen, CreateListingScreen::class.java)
                    startActivity(intent)
                } else {
                    Log.w("TESTING", "signInWithEmail:failure", task.exception)
                    binding.tvResults.text = "Login failed, please check your Email or Password"
                }
            }
    }
}