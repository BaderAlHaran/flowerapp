package com.example.projectthree

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity


class HomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
    }
    fun flowerIdentification(view: View){
        val intent = Intent(this, flowerDetection::class.java)
        startActivity(intent)
    }

    fun settings(view: View){
        val intent =  Intent(this, Settings::class.java);
        startActivity(intent)
    }

    fun profile(view:View){
        val intent = Intent(this, Profile::class.java)
        startActivity(intent)
    }
    fun disease_detection(view: View){
        val intent = Intent(this, disease_ai::class.java)
        startActivity(intent)
    }
    fun soil_detection(view: View) {
        val intent = Intent(this, soil_ai::class.java)
        startActivity(intent)
    }
}