package com.example.mameto

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import coil.api.load
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivityUpdateProfilePicBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.*

class UpdateProfilePicActivity : AppCompatActivity() {
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    lateinit var sharedPref: SharedPreferences
    var storage = Firebase.storage
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityUpdateProfilePicBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout XML file and return a binding object instance
        binding = ActivityUpdateProfilePicBinding.inflate(layoutInflater)
        // Set the content view of the Activity to be the root view of the layout
        setContentView(binding.root)
        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        val storageRef = storage.reference
        val userID = sharedPref.getString("userID",null)
        val fileName = userID.toString()
        val dialog22 = MaterialDialog(this@UpdateProfilePicActivity)
            .noAutoDismiss()
            .customView(R.layout.loader_dialog)
        CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                dialog22.show()
                delay(1000)
            }
            try {
                val querySnapshot = usersCollectionRef.document(userID!!).get().addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject<User>()
                       // withContext(Dispatchers.Main) {
                        dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        dialog22.dismiss()
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
                        dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        dialog22.dismiss()
                        binding.progressBar.visibility = View.GONE
                        // Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        val dialog2 = MaterialDialog(this@UpdateProfilePicActivity)
                            .noAutoDismiss()
                            .customView(R.layout.successful_reset_password)
                        dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                        dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                        dialog2.findViewById<TextView>(R.id.dialog_content).text= "Cannot retrieve user data"
                        dialog2.show()
                        Log.d("TAG", "No such document")
                    }
                }
                    .addOnFailureListener { exception ->
                        Log.d("TAG", "get failed with ", exception)
                        dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                        dialog22.dismiss()
                        binding.progressBar.visibility = View.GONE
                        // Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                        val dialog2 = MaterialDialog(this@UpdateProfilePicActivity)
                            .noAutoDismiss()
                            .customView(R.layout.successful_reset_password)
                        dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                        dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                        dialog2.findViewById<TextView>(R.id.dialog_content).text= exception.message
                        dialog2.show()
                    }
            } catch(e: Exception) {
                withContext(Dispatchers.Main) {
                    dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    dialog22.dismiss()
                    binding.progressBar.visibility = View.GONE
                    // Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                    val dialog2 = MaterialDialog(this@UpdateProfilePicActivity)
                        .noAutoDismiss()
                        .customView(R.layout.successful_reset_password)
                    dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                    dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                    dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                    dialog2.show()
                }
            }

        }
        var profilePic: Uri? = null
        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback {

                binding.profileImage.setImageURI(it)
                profilePic = it
            }
        )
        binding.edit.setOnClickListener {
            getImage.launch("image/*")
        }
        binding.backBtn.setOnClickListener { finish() }
        binding.doneBtn.setOnClickListener {
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
                                 val dialog1 = MaterialDialog(this@UpdateProfilePicActivity)
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
                                                    finish() }
                                                .addOnFailureListener { e ->
                                                    binding.progressBar.visibility = View.GONE
                                                    // If uploading profile pic fails, display a message to the user.
                                                    val dialog1 = MaterialDialog(this@UpdateProfilePicActivity)
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
                                            val dialog1 = MaterialDialog(this@UpdateProfilePicActivity)
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
                            val dialog1 = MaterialDialog(this@UpdateProfilePicActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog1.findViewById<TextView>(R.id.ok_btn)
                                .setOnClickListener { dialog1.dismiss() }
                            dialog1.findViewById<TextView>(R.id.dialog_title).text = "Sorry"
                            dialog1.findViewById<TextView>(R.id.dialog_content).text = e.message
                            dialog1.show() } } }
            }else {
                val intent = Intent(this@UpdateProfilePicActivity,EditProfileActivity::class.java)
                startActivity(intent)
                finish() } }

    }
}