package com.example.labexam03

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.lab03ex.R

class OnboardingActivity1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding1)

        findViewById<Button>(R.id.next_button1).setOnClickListener {
            startActivity(Intent(this, OnboardingActivity2::class.java))
            finish()
        }
    }
}
