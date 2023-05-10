package com.example.mameto

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivityHomeScreenBinding
import com.example.mameto.ViewPagerAdapterForC_S_KFragments
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class HomeScreenActivity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityHomeScreenBinding

    private lateinit var adapter: ViewPagerAdapterForC_S_KFragments

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout XML file and return a binding object instance
        binding = ActivityHomeScreenBinding.inflate(layoutInflater)
        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)

        adapter = ViewPagerAdapterForC_S_KFragments(supportFragmentManager, lifecycle)

        binding.viewPager.adapter = adapter
        setUpTabs()

        auth = Firebase.auth
        sharedPref = getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()




    }

    private fun setUpTabs() {
        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            when(position) { 0 -> {
                tab.text = "community"
                tab.setIcon(R.drawable.community_icon)
            } 1 -> {
                tab.text = "specialist"
                tab.setIcon(R.drawable.specialist_icon)
            } 2 -> {
                tab.text = "kids area"
                tab.setIcon(R.drawable.kids_area_icon)
            } 3 -> {
                tab.text = "setting"
                tab.setIcon(R.drawable.ic_baseline_settings_24)
            }
            }
        }.attach()
    }
}