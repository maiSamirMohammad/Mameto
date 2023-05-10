package com.example.mameto

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class ChangePasswordActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    // Binding object instance with access to the views in the activity_main.xml layout
    private lateinit var binding: ActivityChangePasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        binding.currentPasswordEditText.hint="******"
        binding.newPasswordEditText.hint="******"
        binding.confirmPasswordEditText.hint="******"
        binding.backBtn.setOnClickListener {
            val intent = Intent(this,EditProfileActivity::class.java)
            startActivity(intent)
            finish() }
        binding.doneBtn.setOnClickListener {
            val currentPassword = binding.currentPasswordEditText.text.toString()
            val newPassword = binding.newPasswordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            //local validation
            if (currentPassword.isEmpty()){
                val dialog3 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog3.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog3.dismiss() }
                dialog3.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog3.findViewById<TextView>(R.id.dialog_content).text="Please enter your old password"
                dialog3.show()
            }else if (currentPassword.length < 6){
                val dialog4 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog4.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog4.dismiss() }
                dialog4.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog4.findViewById<TextView>(R.id.dialog_content).text="Your password should be 6 characters or more"
                dialog4.show()
            }else if (newPassword.isEmpty()){
                val dialog3 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog3.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog3.dismiss() }
                dialog3.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog3.findViewById<TextView>(R.id.dialog_content).text="Please enter your new password"
                dialog3.show()
            }else if (newPassword.length < 6){
                val dialog4 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog4.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog4.dismiss() }
                dialog4.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog4.findViewById<TextView>(R.id.dialog_content).text="Your password should be 6 characters or more"
                dialog4.show()
            }else if (currentPassword==newPassword){
                val dialog5 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog5.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog5.dismiss() }
                dialog5.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog5.findViewById<TextView>(R.id.dialog_content).text="New password cannot be same as the old password"
                dialog5.show()
            }else if (confirmPassword.isEmpty()){
                val dialog5 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog5.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog5.dismiss() }
                dialog5.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog5.findViewById<TextView>(R.id.dialog_content).text="Confirm password cannot be empty"
                dialog5.show()
            }else if (newPassword!=confirmPassword){
                val dialog6 = MaterialDialog(this)
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog6.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog6.dismiss() }
                dialog6.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog6.findViewById<TextView>(R.id.dialog_content).text="Confirm password does not match!"
                dialog6.show()
            }else if ( currentPassword.length >= 6 && newPassword.length >= 6 && newPassword ==confirmPassword){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        withContext(Dispatchers.Main) { binding.progressBar.visibility = View.VISIBLE }
                        val user = Firebase.auth.currentUser!!
                    // Get auth credentials from the user for re-authentication.
                        if (user != null && user.email != null) {
                            val credential = EmailAuthProvider
                                .getCredential(user.email!!, currentPassword)
                    // Prompt the user to re-provide their sign-in credentials
                            user.reauthenticate(credential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        user.updatePassword(newPassword)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    binding.progressBar.visibility = View.GONE
                                                    val dialog2 = MaterialDialog(this@ChangePasswordActivity)
                                                        .noAutoDismiss()
                                                        .customView(R.layout.successful_reset_password)
                                                    dialog2.findViewById<TextView>(R.id.dialog_content).text="Password changed successfully."
                                                    dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener {
                                                        dialog2.dismiss()
                                                        val intent = Intent(this@ChangePasswordActivity,EditProfileActivity::class.java)
                                                        startActivity(intent)
                                                        finish()}
                                                    dialog2.show() } }
                                    }else{
                                        binding.progressBar.visibility = View.GONE
                                        val dialog7 = MaterialDialog(this@ChangePasswordActivity)
                                            .noAutoDismiss()
                                            .customView(R.layout.successful_reset_password)
                                        dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                                        dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                        dialog7.findViewById<TextView>(R.id.dialog_content).text= "Your old password was entered incorrectly"
                                        dialog7.show() } } }
                    }catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            binding.progressBar.visibility = View.GONE
                            // If sign in fails, display a message to the user.
                            val dialog7 = MaterialDialog(this@ChangePasswordActivity)
                                .noAutoDismiss()
                                .customView(R.layout.successful_reset_password)
                            dialog7.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog7.dismiss() }
                            dialog7.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                            dialog7.findViewById<TextView>(R.id.dialog_content).text= e.message
                            dialog7.show() } } } } }



    }
}