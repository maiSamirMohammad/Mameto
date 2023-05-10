package com.example.mameto

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivitySignup3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*

class Signup3Activity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth
    var storage = Firebase.storage
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivitySignup3Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout XML file and return a binding object instance
        binding = ActivitySignup3Binding.inflate(layoutInflater)
        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)

        ////////////
        val sharedpref_welcome = getSharedPreferences("welcome", Context.MODE_PRIVATE)
        val editor_welcome = sharedpref_welcome.edit()

        auth = Firebase.auth
        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor= sharedPref.edit()
        // Create a storage reference from our app
        val storageRef = storage.reference
        val userID = sharedPref.getString("userID",null)
        val fileName = userID.toString()
        var profilePic: Uri? = null
        var skipOrGetstartedState: Boolean
        editor.apply {
            skipOrGetstartedState=false
            putBoolean("skipOrGetstartedState",skipOrGetstartedState)
            apply()
        }
        // Add pic
        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {
                binding.profileImage.setImageURI(it)
                profilePic = it
            }
        )
        binding.profileImage.setOnClickListener {
            getImage.launch("image/*")
            if (binding.profileImage.drawable!=null){
                binding.addPhoto.visibility = View.GONE
            }
        }

        binding.skip.setOnClickListener {
            var firstTime: Boolean
            editor.apply {
                firstTime=false
                putBoolean("firstTime",firstTime)
                apply()
            }
            editor.apply {
                skipOrGetstartedState=true
                putBoolean("skipOrGetstartedState",skipOrGetstartedState)
                apply()
            }
            val intent = Intent(this@Signup3Activity,HomeScreenActivity::class.java)
            startActivity(intent)
            finish()

        }

        binding.getStartedBtn.setOnClickListener {
            val profilePicRef = storageRef.child(fileName)
                if (profilePic != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                        try {
                            profilePicRef.putFile(profilePic!!)
                                .addOnFailureListener {
                                    // Handle unsuccessful uploads
                                    binding.progressBar.visibility = View.GONE
                                    // If uploading profile pic fails, display a message to the user.
                                    val dialog1 = MaterialDialog(this@Signup3Activity)
                                        .noAutoDismiss()
                                        .customView(R.layout.successful_reset_password)
                                    dialog1.findViewById<TextView>(R.id.ok_btn)
                                        .setOnClickListener { dialog1.dismiss() }
                                    dialog1.findViewById<TextView>(R.id.dialog_title).text = "Sorry"
                                    dialog1.findViewById<TextView>(R.id.dialog_content).text = "uploading profile pic fails"
                                    dialog1.show()
                                }.addOnSuccessListener { taskSnapshot ->
                                    profilePicRef.downloadUrl
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val downloadUri = task.result
                                                usersCollectionRef.document(userID!!).update("ImageURL", downloadUri)
                                                    .addOnSuccessListener {
                                                        binding.progressBar.visibility = View.GONE
                                                        val intent = Intent(this@Signup3Activity, HomeScreenActivity::class.java)
                                                        startActivity(intent)
                                                        finish()
                                                        var firstTime: Boolean
                                                        editor.apply {
                                                            firstTime=false
                                                            putBoolean("firstTime",firstTime)
                                                            apply()
                                                        }
                                                        editor.apply {
                                                            skipOrGetstartedState = true
                                                            putBoolean("skipOrGetstartedState", skipOrGetstartedState)
                                                            apply()
                                                        }
                                                        editor_welcome.putBoolean("get_started_clicked", true)
                                                        editor_welcome.apply()
                                                    }
                                                    .addOnFailureListener { e ->
                                                        binding.progressBar.visibility = View.GONE
                                                        // If uploading profile pic fails, display a message to the user.
                                                        val dialog1 = MaterialDialog(this@Signup3Activity)
                                                            .noAutoDismiss()
                                                            .customView(R.layout.successful_reset_password)
                                                        dialog1.findViewById<TextView>(R.id.ok_btn)
                                                            .setOnClickListener { dialog1.dismiss() }
                                                        dialog1.findViewById<TextView>(R.id.dialog_title).text = "Sorry"
                                                        dialog1.findViewById<TextView>(R.id.dialog_content).text = e.message
                                                        dialog1.show() }
                                            }else{
                                                binding.progressBar.visibility = View.GONE
                                                // If uploading profile pic fails, display a message to the user.
                                                val dialog1 = MaterialDialog(this@Signup3Activity)
                                                    .noAutoDismiss()
                                                    .customView(R.layout.successful_reset_password)
                                                dialog1.findViewById<TextView>(R.id.ok_btn)
                                                    .setOnClickListener { dialog1.dismiss() }
                                                dialog1.findViewById<TextView>(R.id.dialog_title).text = "Sorry"
                                                dialog1.findViewById<TextView>(R.id.dialog_content).text = "uploading profile pic fails"
                                                dialog1.show() } } }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE

                            // If uploading profile pic fails, display a message to the user.
                            val dialog1 = MaterialDialog(this@Signup3Activity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog1.findViewById<TextView>(R.id.ok_btn)
                                .setOnClickListener { dialog1.dismiss() }
                            dialog1.findViewById<TextView>(R.id.dialog_title).text = "Sorry"
                            dialog1.findViewById<TextView>(R.id.dialog_content).text = e.message
                            dialog1.show() } } }
            }else {
                val intent = Intent(this@Signup3Activity,HomeScreenActivity::class.java)
                    startActivity(intent)
                    var firstTime: Boolean
                    editor.apply {
                        firstTime=false
                        putBoolean("firstTime",firstTime)
                        apply()
                    }
                    editor.apply {
                        skipOrGetstartedState=true
                        putBoolean("skipOrGetstartedState",skipOrGetstartedState)
                        apply()
                    }


            }


        }


    }


}