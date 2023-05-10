package com.example.mameto

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*
import android.window.SplashScreen
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.example.mameto.databinding.ActivityOnboardingScreenBinding
//import kotlinx.android.synthetic.main.activity_onboarding_screen.*
//import kotlinx.android.synthetic.main.activity_signup3.*
//import kotlinx.android.synthetic.main.slider_layout.*

class onboardingScreen : AppCompatActivity() {

    private lateinit var onboardingItemsAdapter: OnboardingItemsAdapter
    private lateinit var indicatorsContainer: LinearLayout

    private lateinit var binding: ActivityOnboardingScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityOnboardingScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setOnboardingItems()
        setUpIndicators()
        setCurrentIndicator(0)

        val sharedPref = getSharedPreferences("boardingScreen", Context.MODE_PRIVATE)
        val isclicked = sharedPref.getBoolean("clicked", false)
        if (isclicked) {
            val intent = Intent(applicationContext, AfterSplashActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.btnGetStarted.setOnClickListener {
            val sharedpref = getSharedPreferences("boardingScreen", Context.MODE_PRIVATE)
            val editor = sharedpref.edit()
            editor.putBoolean("clicked", true)
            editor.apply()

            val intent = Intent(this, AfterSplashActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setOnboardingItems() {
        onboardingItemsAdapter= OnboardingItemsAdapter(
            listOf(
                    OnboardingItem(
                        onboardingImage = R.drawable.modified_community,
                        title = "Are you looking for moms' community?"
                    ),
                OnboardingItem(
                    onboardingImage = R.drawable.spe,
                    title = "Do you want an online specialist?"
                ),
                OnboardingItem(
                    onboardingImage = R.drawable.kid,
                    title = "Are you looking for kids area?"

                ),
          )
        )

        val onboardingViewPager = findViewById<ViewPager2>(R.id.onboardingViewPager)
        onboardingViewPager.adapter = onboardingItemsAdapter
        onboardingViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
        (onboardingViewPager.getChildAt(0) as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        binding.btnNext.setOnClickListener {
            if (onboardingViewPager.currentItem +1 < onboardingItemsAdapter.itemCount){
                onboardingViewPager.currentItem +=1
            }
        }

      // val jj = onboardingViewPager.currentItem



        binding.btnskip.setOnClickListener {
            var sharedpref = getSharedPreferences("boardingScreen", Context.MODE_PRIVATE)
            var editor = sharedpref.edit()
            editor.putBoolean("clicked", true)
            editor.apply()

            val intent = Intent(this, AfterSplashActivity::class.java)
            startActivity(intent)
            finish()
        }




    }

    private fun setUpIndicators (){
            indicatorsContainer = findViewById(R.id.indicatorsContainer)
            val indicators = arrayOfNulls<ImageView>(onboardingItemsAdapter.itemCount)
            val layoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            layoutParams.setMargins(8, 0, 8, 0)
            for (i in indicators.indices){
                indicators[i] = ImageView(applicationContext)
                indicators[i]?.let {
                    it.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_inactive_background))
                    it.layoutParams = layoutParams
                    indicatorsContainer.addView(it)
                }
            }
    }

    private fun setCurrentIndicator(position: Int){
        val childCount = indicatorsContainer.childCount
        for(i in 0 until childCount){
            val imageView = indicatorsContainer.getChildAt(i) as ImageView
            if(i == position){
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_active_background))
            } else{
                imageView.setImageDrawable(ContextCompat.getDrawable(applicationContext, R.drawable.indicator_inactive_background))
            }
            if (position  == (onboardingItemsAdapter.itemCount-1)){
                binding.btnNext.visibility = View.GONE
                binding.btnskip.visibility = View.GONE
                binding.btnGetStarted.visibility = View.VISIBLE
                binding.btnGetStarted.animate().alpha(1f).duration = 2000
            } else {
                binding.btnNext.visibility = View.VISIBLE
                binding.btnskip.visibility = View.VISIBLE
                binding.btnGetStarted.alpha = 0f
                binding.btnGetStarted.visibility = View.GONE
            }
        }

    }
}