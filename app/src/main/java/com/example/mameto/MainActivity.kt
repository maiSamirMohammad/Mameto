package com.example.mameto

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityMainBinding

    override fun onStart() {
        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()
        val userID = sharedPref.getString("userID",null)
        super.onStart()
        checkUserState()
        if (userID != null){
            CoroutineScope(Dispatchers.IO).launch {
            val querySnapshot = usersCollectionRef.document(userID).get().await()
            val user = querySnapshot.toObject<User>()

            withContext(Dispatchers.Main) {
                if(user?.IsSpecialist==true){
                    editor.apply {
                        val isSpecialist=true
                        putBoolean("isSpecialist",isSpecialist)
                        apply()
                    }
                }else{
                    editor.apply {
                        val isSpecialist=false
                        putBoolean("isSpecialist",isSpecialist)
                        apply()
                    }
                }


            }

        }}


    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout XML file and return a binding object instance
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth
        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor= sharedPref.edit()

        // Set up a key listener  to listen for "enter" button presses on email edit_text
        binding.loginEmail.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }
        // Set up a key listener  to listen for "enter" button presses on password edit_text
        binding.loginPassword.setOnKeyListener { view, keyCode, _ ->
            handleKeyEvent(
                view,
                keyCode
            )
        }

        //back to afterSplash screen
        binding.btnBack.setOnClickListener {
            val intent = Intent(this, AfterSplashActivity::class.java)
            startActivity(intent)
            finish()
        }
        /////////////////////
        val sharedpref_welcome = getSharedPreferences("welcome", Context.MODE_PRIVATE)
        val editor_welcome = sharedpref_welcome.edit()

        //login
        binding.loginBtn.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val password = binding.loginPassword.text.toString()
            if (email.isEmpty()){
                val dialog5 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog5.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog5.dismiss() }
                dialog5.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog5.findViewById<TextView>(R.id.dialog_content).text="Please enter your email"
                dialog5.show()
            }else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
                val dialog6 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog6.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog6.dismiss() }
                dialog6.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog6.findViewById<TextView>(R.id.dialog_content).text="Invalid email"
                dialog6.show()
            }else if (password.isEmpty()){
                val dialog3 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog3.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog3.dismiss() }
                dialog3.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog3.findViewById<TextView>(R.id.dialog_content).text="Please enter your password"
                dialog3.show()
            }else if (password.length < 6){
                val dialog4 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog4.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog4.dismiss() }
                dialog4.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog4.findViewById<TextView>(R.id.dialog_content).text="Your password should be 6 characters or more"
                dialog4.show()
            }else if (android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && password.length >= 6){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                       auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this@MainActivity) { task ->
                           if (task.isSuccessful) {
                               // Sign in success
                               val querySnapshot = usersCollectionRef.document(auth.currentUser?.uid.toString()).get().addOnSuccessListener { document ->
                                   if (document != null) {
                                       Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                                       val userdata = document.toObject<User>()
                                       binding.progressBar.visibility = View.GONE
                                       val isSpecialist=userdata?.IsSpecialist
                                       val id=userdata?.id
                                       editor.apply {
                                           putString("userID",id)
                                           apply() }
                                       editor.apply {
                                           if (isSpecialist==true){
                                               putBoolean("isSpecialist",true)
                                               apply()
                                           }else{
                                               putBoolean("isSpecialist",false)
                                               apply() } }
                                       var firstTime: Boolean
                                       editor.apply {
                                           firstTime=false
                                           putBoolean("firstTime",firstTime)
                                           apply() }

                                       editor_welcome.putBoolean("log_in_clicked", true)
                                       editor_welcome.apply()

                                       checkUserState()
                                   } else {
                                       binding.progressBar.visibility = View.GONE
                                       Log.d("TAG", "No such document") } }
                                   .addOnFailureListener { exception -> Log.d("TAG", "get failed with ", exception) }
                               Log.d("TAG", "signInWithEmail:success")
                               val user = auth.currentUser
                           } else {
                               // If sign in fails, display a message to the user.
                               Log.w("TAG", "signInWithEmail:failure", task.exception)
                               binding.progressBar.visibility = View.GONE
                               // If sign in fails, display a message to the user.
                               val dialog7 = MaterialDialog(this@MainActivity)
                                   .noAutoDismiss()
                                   .customView(R.layout.successful_reset_password)
                               dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                               dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                               dialog7.findViewById<TextView>(R.id.dialog_content).text= task.exception?.message
                               dialog7.show() } } } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            // If sign in fails, display a message to the user.
                            val dialog7 = MaterialDialog(this@MainActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                            dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                            dialog7.show() } } } } }
        // an explicit intent to open the next_screen
        binding.signupBtn.setOnClickListener {
            val intent = Intent(this,Signup1Activity::class.java)
            startActivity(intent)
        }

        //reset password
        binding.forgotPasswordBtn.setOnClickListener {
            val dialog1 = MaterialDialog(this)
                .noAutoDismiss()
                .customView(R.layout.dialog_forgot_password)
            dialog1.findViewById<TextView>(R.id.reset_password_btn).setOnClickListener {
                val emailAddress = dialog1.findViewById<EditText>(R.id.email).text.toString()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) {
                            dialog1.dismiss()
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        auth.sendPasswordResetEmail(emailAddress).await()
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            val dialog2 = MaterialDialog(this@MainActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                            dialog2.show()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            // If reset password fails, display a message to the user.
                            val dialog2 = MaterialDialog(this@MainActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                            dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                            dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                            dialog2.show()
                        } } } }
            dialog1.findViewById<TextView>(R.id.cancel_btn).setOnClickListener { dialog1.dismiss() }
            dialog1.show()
        }

//        binding.skip.setOnClickListener {
//            val intent = Intent(this,HomeScreenActivity::class.java)
//            startActivity(intent)
//        }

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

     fun checkUserState() {
        val userID = sharedPref.getString("userID",null)
        val skipOrGetstartedState = sharedPref.getBoolean("skipOrGetstartedState",false)
        val firstTime = sharedPref.getBoolean("firstTime",false)

        if (userID!=null && skipOrGetstartedState==false&&firstTime==true){
            val intent = Intent(this,Signup3Activity::class.java)
            startActivity(intent)}
        if(userID!=null && skipOrGetstartedState==true&&firstTime==true){
            val intent = Intent(this,HomeScreenActivity::class.java)
            startActivity(intent)}
        if(userID!=null &&firstTime==false){
            val intent = Intent(this,HomeScreenActivity::class.java)
            startActivity(intent)
        }


    }
}