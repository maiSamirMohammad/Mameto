package com.example.mameto

import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import com.example.mameto.databinding.ActivityAnimationLogoBinding
import com.example.mameto.databinding.ActivityCommunityPostsBinding
import kotlinx.coroutines.*
import java.lang.Runnable

class AnimationLogo : AppCompatActivity() {
    private val delay = 200
    private val handler = Handler()
    private var index: Int = 0
    lateinit var charSequence: CharSequence
    private lateinit var binding: ActivityAnimationLogoBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnimationLogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPref_onboarding = getSharedPreferences("boardingScreen", Context.MODE_PRIVATE)
        val isclicked = sharedPref_onboarding.getBoolean("clicked", false)

        val sharedPref_user= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)

        val sharedPref = getSharedPreferences("welcome", Context.MODE_PRIVATE)
        val shown = sharedPref.getBoolean("shown", false)
        val log_out_clicked = sharedPref.getBoolean("log_out_clicked", false)
        val log_in_clicked = sharedPref.getBoolean("log_in_clicked", false)
        val get_started_clicked = sharedPref.getBoolean("get_started_clicked", false)
        val welcome_skip_clicked = sharedPref.getBoolean("welcome_skip_clicked", false)

        val userID = sharedPref_user.getString("userID",null)


        if (shown && !isclicked) {
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    binding.logoActivity.alpha = 0f
                    binding.logoActivity.animate().alpha(1f).duration = 1000
                    delay(1000)
//                    finish()
                    val intent = Intent(applicationContext, onboardingScreen::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            return
        }
        if (shown && userID != null || shown && userID != null){
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    binding.logoActivity.alpha = 0f
                    binding.logoActivity.animate().alpha(1f).duration = 1000
                    delay(1000)
                    val intent = Intent(applicationContext, HomeScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
            return
        }
        if (shown && isclicked && userID != null){
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    binding.logoActivity.alpha = 0f
                    binding.logoActivity.animate().alpha(1f).duration = 1000
                    delay(1000)
                    val intent = Intent(applicationContext, HomeScreenActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
                    return
        }
        if(shown && userID == null){
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
                    binding.logoActivity.alpha = 0f
                    binding.logoActivity.animate().alpha(1f).duration = 1000
                    delay(1000)
                    val intent = Intent(applicationContext, AfterSplashActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
                    return
        }

        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.logoActivity.animate().alpha(1f).duration = 2000
                val animation_1 = AnimationUtils.loadAnimation(this@AnimationLogo, R.anim.animation_1)
                binding.mameto.animation = animation_1
                delay(2500)
                val objAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    binding.logoo,
                    PropertyValuesHolder.ofFloat("scaleX", 1.2f),
                    PropertyValuesHolder.ofFloat("scaleY", 1.2f)
                )
                objAnimator.setDuration(500)
                objAnimator.repeatCount = ValueAnimator.INFINITE
                objAnimator.repeatMode = ValueAnimator.REVERSE
                objAnimator.start()
                delay(2500)
                binding.logoo.animate().translationXBy(2000f).duration = 1000
                binding.mameto.animate().translationXBy(-2000f).duration = 1000
                delay(1000)

                val sharedpref = getSharedPreferences("welcome", Context.MODE_PRIVATE)
                val editor = sharedpref.edit()
                editor.putBoolean("shown", true)
                editor.apply()
                    val intent = Intent(applicationContext, onboardingScreen::class.java)
                    startActivity(intent)
                    finish()

//                CoroutineScope(Dispatchers.IO).launch {
//                    withContext(Dispatchers.Main) {
//                        binding.logoActivity.animate().alpha(1f).duration = 1000
//                        delay(1000)
//                        finish()
//                    }
            }
        }
    }
}