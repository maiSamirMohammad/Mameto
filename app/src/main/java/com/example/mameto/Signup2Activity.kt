package com.example.mameto

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivitySignup2Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class Signup2Activity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth
    private val usersCollectionRef = Firebase.firestore.collection("Users")

    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivitySignup2Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout XML file and return a binding object instance
        binding = ActivitySignup2Binding.inflate(layoutInflater)
        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)

        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor= sharedPref.edit()
        auth = Firebase.auth

        // Set up a key listener  to listen for "enter" button presses on email edit_text
        binding.email.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }
        // Set up a key listener  to listen for "enter" button presses on password edit_text
        binding.password.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }
        // Set up a key listener  to listen for "enter" button presses on confirm_password edit_text
        binding.confirmPassword.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }
        val fName = intent?.extras?.getString("FNAME").toString()
        val lName = intent?.extras?.getString("LNAME").toString()
        // signup
        binding.signupBtn.setOnClickListener{
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            if (email.isEmpty()){
                val dialog1 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog1.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog1.dismiss() }
                dialog1.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog1.findViewById<TextView>(R.id.dialog_content).text="Please enter an email"
                dialog1.show()

            }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
                val dialog2 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog2.findViewById<TextView>(R.id.dialog_content).text="Invalid email"
                dialog2.show()
            }else if (password.isEmpty()){
                val dialog3 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog3.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog3.dismiss() }
                dialog3.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog3.findViewById<TextView>(R.id.dialog_content).text="Please enter a password"
                dialog3.show()
            }else if (password.length < 6){
                val dialog4 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog4.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog4.dismiss() }
                dialog4.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog4.findViewById<TextView>(R.id.dialog_content).text="Your password should be 6 characters or more"
                dialog4.show()
            }else if (confirmPassword.isEmpty()){
                val dialog5 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog5.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog5.dismiss() }
                dialog5.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog5.findViewById<TextView>(R.id.dialog_content).text="Confirm password cannot be empty"
                dialog5.show()
            }else if (password!=confirmPassword){
                val dialog6 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog6.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog6.dismiss() }
                dialog6.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog6.findViewById<TextView>(R.id.dialog_content).text="Confirm password does not match!"
                dialog6.show()
            }else if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6 && password ==confirmPassword){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(this@Signup2Activity) { task ->
                            if (task.isSuccessful) {
                                // Sign in success, update UI with the signed-in user's information
                                // Sign up success
                                val currentUser = auth.currentUser
                                val userID=currentUser?.uid.toString()
                                val Email=currentUser?.email.toString()
                                editor.apply {
                                    putString("userID",userID)
                                    apply()
                                }
                                var firstTime: Boolean
                                editor.apply {
                                    firstTime=true
                                    putBoolean("firstTime",firstTime)
                                    apply()
                                }
                                val user = hashMapOf(
                                    "Email" to Email,
                                    "FirstName" to fName,
                                    "LastName" to lName,
                                    "id" to userID
                                )
                                usersCollectionRef.document(userID).set(user).addOnSuccessListener { Log.d("TAG", "DocumentSnapshot successfully updated!")
                                    binding.progressBar.visibility = View.GONE
                                    val intent = Intent(this@Signup2Activity,Signup3Activity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                    .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e)
                                        binding.progressBar.visibility = View.GONE
                                        // If uploading data to firestore fails, display a message to the user.
                                        val dialog = MaterialDialog(this@Signup2Activity)
                                            .noAutoDismiss()
                                            .customView(R.layout.successful_reset_password)
                                        dialog.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog.dismiss() }
                                        dialog.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                        dialog.findViewById<TextView>(R.id.dialog_content).text= e.message
                                        dialog.show()
                                    }

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("TAG", "createUserWithEmail:failure", task.exception)

                                binding.progressBar.visibility = View.GONE
                                // If sign up fails, display a message to the user.
                                val dialog7 = MaterialDialog(this@Signup2Activity)
                                    .noAutoDismiss()
                                    .customView(R.layout.successful_reset_password)
                                dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                                dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry :("
                                dialog7.findViewById<TextView>(R.id.dialog_content).text= task.exception?.message.toString()
                                dialog7.show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            // If sign up fails, display a message to the user.
                            val dialog7 = MaterialDialog(this@Signup2Activity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                            dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                            dialog7.show()
                        } } } } }
        //  intent to return to the previous screen
        binding.backBtn.setOnClickListener {
            val intent = Intent(this,Signup1Activity::class.java)
            startActivity(intent) }
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