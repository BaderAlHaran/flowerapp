package com.example.projectthree

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class SignUp : AppCompatActivity() {
    lateinit var editTextUsername: EditText
    lateinit var editTextPassword: EditText
    lateinit var editTextRePassword: EditText
    lateinit var buttonRegister: Button
    lateinit var dphelper: DBhelper

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        editTextUsername = findViewById(R.id.reget1)
        editTextPassword = findViewById(R.id.reget2)
        editTextRePassword = findViewById(R.id.reget3)
        buttonRegister = findViewById(R.id.regbutton)
        dphelper = DBhelper(this)

        buttonRegister.setOnClickListener { onReg() }
    }

    private fun onReg() {
        val user: String = editTextUsername.text.toString()
        val pwd: String = editTextPassword.text.toString()
        val repwd: String = editTextRePassword.text.toString()

        if (user.isEmpty() || pwd.isEmpty() || repwd.isEmpty()) {
            Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_LONG).show()
            return
        }

        if (pwd == repwd) {
            if (dphelper.cheackUserName(user)) {
                Toast.makeText(this, "User already exists", Toast.LENGTH_LONG).show()
                return
            }

            val register: Boolean = dphelper.insertData(user, pwd)
            if (register) {
                Toast.makeText(this, "User registered successfully", Toast.LENGTH_LONG).show()
                val intent = Intent(this, HomePage::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(this, "User registration failed", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
        }
    }
}