package com.example.mameto

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mameto.databinding.ActivityAfterSplashBinding
import com.example.mameto.databinding.ActivityCommunityPostsBinding

class AfterSplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAfterSplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAfterSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedpref_welcome = getSharedPreferences("welcome", Context.MODE_PRIVATE)
        val editor_welcome = sharedpref_welcome.edit()


        binding.btnLogIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, Signup1Activity::class.java)
            startActivity(intent)
            finish()
        }

        binding.skip.setOnClickListener {
            editor_welcome.putBoolean("welcome_skip_clicked", true)
            editor_welcome.apply()
            val intent = Intent(this,HomeScreenActivity::class.java)
            startActivity(intent)
        }

    }
}