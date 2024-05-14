package com.example.projectthree

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class Login : AppCompatActivity() {
    lateinit var editTextUsername: EditText
    lateinit var editTextPassword: EditText
    lateinit var buttonLogin: Button
    lateinit var dBhelper: DBhelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        dBhelper = DBhelper(this)
        editTextUsername = findViewById(R.id.username)
        editTextPassword = findViewById(R.id.pwd)
        buttonLogin = findViewById(R.id.logbutton)

        buttonLogin.setOnClickListener { homePage() }
    }

    private fun homePage() {
        val user: String = editTextUsername.text.toString()
        val pwd: String = editTextPassword.text.toString()

        if (user.isEmpty() || pwd.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            return
        }

        val isLogged: Boolean = dBhelper.checkUser(user, pwd)
        if (isLogged) {
            val intent = Intent(this, HomePage::class.java)
            startActivity(intent)
        } else {
            Toast.makeText(this, "Wrong username or password", Toast.LENGTH_LONG).show()
        }
    }
}