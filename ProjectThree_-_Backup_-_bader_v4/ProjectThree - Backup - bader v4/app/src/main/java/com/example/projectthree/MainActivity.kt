package com.example.projectthree

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun signup(view: View){
        val intent =  Intent(this, SignUp::class.java);
        startActivity(intent)
    }

    fun login(view: View){
        val intent =  Intent(this, Login::class.java);
        startActivity(intent)
    }
}