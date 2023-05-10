package com.example.mameto

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivityMainBinding
import com.example.mameto.databinding.ActivitySignup1Binding

class Signup1Activity : AppCompatActivity() {
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivitySignup1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout XML file and return a binding object instance
        binding = ActivitySignup1Binding.inflate(layoutInflater)
        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)

        // Set up a key listener  to listen for "enter" button presses on first_name edit_text
        binding.firstName.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }
        // Set up a key listener  to listen for "enter" button presses on last_name edit_text
        binding.lastName.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }
        //go to the next screen
        binding.nextBtn.setOnClickListener {
            val firstName = binding.firstName.text.toString()
            val lastName = binding.lastName.text.toString()
            if (firstName.isEmpty()){
                val dialog1 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog1.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog1.dismiss() }
                dialog1.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog1.findViewById<TextView>(R.id.dialog_content).text="Please enter your first name"
                dialog1.show()
            }else if (lastName.isEmpty()){
                val dialog2 = MaterialDialog(this)
                .noAutoDismiss()
                .customView(R.layout.successful_reset_password)
                dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog2.findViewById<TextView>(R.id.dialog_content).text="Please enter your last name"
                dialog2.show()
            }else if (firstName.isNotEmpty() && lastName.isNotEmpty()){
                val intent = Intent(this,Signup2Activity::class.java)
                intent.putExtra("FNAME",firstName)
                intent.putExtra("LNAME",lastName)
                startActivity(intent)
            }

        }
        // intent to return to the previous screen
        binding.backBtn.setOnClickListener {
            val intent = Intent(this,AfterSplashActivity::class.java)
            startActivity(intent)
            finish() }
    }

    /**
     * Key listener for hiding the keyboard when the "Enter" button is tapped.
     */
    private fun handleKeyEvent(view: View, keyCode: Int): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            // Hide the keyboard
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
            return true
        }
        return false
    }
}