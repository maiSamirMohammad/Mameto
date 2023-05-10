package com.example.mameto

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import coil.api.load
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivityEditProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class EditProfileActivity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityEditProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("editprofileito", "onCreate")
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor= sharedPref.edit()
        val userID = sharedPref.getString("userID",null)
        auth = Firebase.auth
        val map = mutableMapOf<String, Any>()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                val querySnapshot = usersCollectionRef.document(userID!!).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject<User>()
                        binding.progressBar.visibility = View.GONE
                        binding.firstNameEditText.hint=user?.FirstName
                        binding.lastNameEditText.hint=user?.LastName
                        binding.emailEditText.hint=user?.Email
                        if (user?.ImageURL !=null){
                            user?.ImageURL?.let {
                                binding.profileImage.load(user?.ImageURL) {
                                    placeholder(R.drawable.loading_animation)
                                    error(R.drawable.ic_broken_image) } }
                        }else{ binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24) }
                    } else { Log.d("TAG", "No such document")
                        binding.progressBar.visibility = View.GONE
                        // Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        val dialog2 = MaterialDialog(this@EditProfileActivity)
                            .noAutoDismiss()
                            .customView(R.layout.successful_reset_password)
                        dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                        dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                        dialog2.findViewById<TextView>(R.id.dialog_content).text= "Cannot retrieve user data"
                        dialog2.show()
                    }
                }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "get failed with ", exception)
                        binding.progressBar.visibility = View.GONE
                        // Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        val dialog2 = MaterialDialog(this@EditProfileActivity)
                            .noAutoDismiss()
                            .customView(R.layout.successful_reset_password)
                        dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                        dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                        dialog2.findViewById<TextView>(R.id.dialog_content).text= exception.message
                        dialog2.show()}
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    // Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    val dialog2 = MaterialDialog(this@EditProfileActivity)
                        .noAutoDismiss()
                        .customView(R.layout.successful_reset_password)
                    dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                    dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                    dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                    dialog2.show()
                }
            }

            }

        binding.changeProfilePhoto.setOnClickListener {
            val intent = Intent(this,UpdateProfilePicActivity::class.java)
            startActivity(intent)

        }

        binding.doneBtn.setOnClickListener {
            val firstName = binding.firstNameEditText.text.toString()
            val lastName = binding.lastNameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            if(firstName.isNotEmpty()) { map["FirstName"] = firstName }
            if(lastName.isNotEmpty()) { map["LastName"] = lastName }
            if(email.isNotEmpty()&&android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                        auth.currentUser?.updateEmail(email)?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("TAG", "User email address updated.")
                                map["Email"] = email
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        usersCollectionRef.document(userID!!).set(map, SetOptions.merge()).addOnSuccessListener {
                                            binding.progressBar.visibility = View.GONE
                                            finish() }
                                            .addOnFailureListener { e -> Log.w("TAG", "Error updating document", e)
                                                binding.progressBar.visibility = View.GONE
                                                val dialog7 = MaterialDialog(this@EditProfileActivity)
                                                    .noAutoDismiss()
                                                    .customView(R.layout.successful_reset_password)
                                                dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                                                dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                                dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                                                dialog7.show()}
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            binding.progressBar.visibility = View.GONE
                                            val dialog7 = MaterialDialog(this@EditProfileActivity)
                                                .noAutoDismiss()
                                                .customView(R.layout.successful_reset_password)
                                            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                                            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                            dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                                            dialog7.show()
                                        } } } }else{
                                binding.progressBar.visibility = View.GONE
                                val dialog7 = MaterialDialog(this@EditProfileActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                                dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                                dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                dialog7.findViewById<TextView>(R.id.dialog_content).text= "Please,log out and then sign in to update your email"
                                dialog7.show()} }
                        }catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            // If sign in fails, display a message to the user.
                            val dialog7 = MaterialDialog(this@EditProfileActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                            dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                            dialog7.show()
                        } } }
            }else if (email.isNotEmpty()&&!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ){
                val dialog6 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog6.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog6.dismiss() }
                dialog6.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog6.findViewById<TextView>(R.id.dialog_content).text="Invalid email"
                dialog6.show()
            }else if(firstName.isNotEmpty()||lastName.isNotEmpty()){
                CoroutineScope(Dispatchers.IO).launch {
                    withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                    try {
                        usersCollectionRef.document(userID!!).set(map, SetOptions.merge()).await()
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            finish() }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            val dialog7 = MaterialDialog(this@EditProfileActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                            dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                            dialog7.show()
                        } } } } }


    binding.changePasswordBtn.setOnClickListener {
        val intent = Intent(this,ChangePasswordActivity::class.java)
        startActivity(intent)
        finish()
    }
    binding.cancel.setOnClickListener {
        finish()
    }

    }

/*    override fun onStart() {
        super.onStart()
        Log.d("editprofileito", "onStart")


        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        val userID = sharedPref.getString("userID",null)
        val querySnapshot = usersCollectionRef.document(userID!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject<User>()
                    binding.firstNameEditText.hint=user?.FirstName
                    binding.lastNameEditText.hint=user?.LastName
                    binding.emailEditText.hint=user?.Email
                    if (user?.ImageURL !=null){
                        user?.ImageURL?.let {
                            binding.profileImage.load(user?.ImageURL) {
                                placeholder(R.drawable.loading_animation)
                                error(R.drawable.ic_broken_image)
                            }
                        }
                    }else{
                        binding.profileImage.setImageResource(R.drawable.ic_baseline_account_circle)
                    }


                } else {
                    Log.d("TAG", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }


    }*/
    override fun onResume() {
        super.onResume()
        Log.d("editprofileito", "onResume")



    sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
    val userID = sharedPref.getString("userID",null)
    val querySnapshot = usersCollectionRef.document(userID!!).get()
        .addOnSuccessListener { document ->
            if (document != null) {
                val user = document.toObject<User>()
                binding.firstNameEditText.hint=user?.FirstName
                binding.lastNameEditText.hint=user?.LastName
                binding.emailEditText.hint=user?.Email
                if (user?.ImageURL !=null){
                    user?.ImageURL?.let {
                        binding.profileImage.load(user?.ImageURL) {
                            placeholder(R.drawable.loading_animation)
                            error(R.drawable.ic_broken_image)
                        }
                    }
                }else{
                    binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                }


            } else {
                val dialog7 = MaterialDialog(this@EditProfileActivity)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog7.findViewById<TextView>(R.id.dialog_content).text= "Cannot retrieve user data"
                dialog7.show()
                Log.d("TAG", "No such document")
            }
        }
        .addOnFailureListener { exception ->
            Log.d("TAG", "get failed with ", exception)
            val dialog7 = MaterialDialog(this@EditProfileActivity)
                .noAutoDismiss()
                .customView(R.layout.successful_reset_password)
            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
            dialog7.findViewById<TextView>(R.id.dialog_content).text= exception.message
            dialog7.show()
        }

    }

/*    override fun onRestart() {
        super.onRestart()
        Log.d("editprofileito", "onRestart")
        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        val userID = sharedPref.getString("userID",null)
        val querySnapshot = usersCollectionRef.document(userID!!).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val user = document.toObject<User>()
                    binding.firstNameEditText.hint=user?.FirstName
                    binding.lastNameEditText.hint=user?.LastName
                    binding.emailEditText.hint=user?.Email
                    if (user?.ImageURL !=null){
                        user?.ImageURL?.let {
                            binding.profileImage.load(user?.ImageURL) {
                                placeholder(R.drawable.loading_animation)
                                error(R.drawable.ic_broken_image)
                            }
                        }
                    }else{
                        binding.profileImage.setImageResource(R.drawable.ic_baseline_account_circle)
                    }


                } else {
                    Log.d("TAG", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
    }*/
}