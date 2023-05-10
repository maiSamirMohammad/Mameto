package com.example.mameto

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import coil.api.load
import coil.transform.BlurTransformation
import com.bumptech.glide.Glide
import com.example.mameto.databinding.ActivityCommunityPostsBinding
import com.example.mameto.databinding.ActivitySpecialistPostsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val TAG = "Specialist_posts_Activity"
class Specialist_posts_Activity : AppCompatActivity() {
    private lateinit var binding: ActivitySpecialistPostsBinding
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var sharedPrefForSpePosts: SharedPreferences
    lateinit var editorofSpePosts: SharedPreferences.Editor
    private var FirstName: String? = null
    private var LastName: String? = null
    private var ProfilePicture: String? = null
    private var signedInuser: User? = null
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private val spePostsCollectionRef = Firebase.firestore.collection("SpecialistPosts")

    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialistPostsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref= getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor= sharedPref.edit()
        val userID = sharedPref.getString("userID",null)


        var isPending = true
        val UserRef = FirebaseFirestore.getInstance().collection("Users")
        UserRef.document(userID.toString()).addSnapshotListener { value, error ->
            val IsSpecialist = value?.get("IsSpecialist")

            if (IsSpecialist == true) {
                isPending = false
            }else{
                isPending = true
            }
            if (IsSpecialist == true) {
                binding.imVerifiedNewPostSpecialist.visibility = View.VISIBLE
            } else {
                binding.imVerifiedNewPostSpecialist.visibility = View.GONE
            }
        }

        val generatedpostId = spePostsCollectionRef.document().id

        storageReference = FirebaseStorage.getInstance().reference
        sharedPrefForSpePosts = getSharedPreferences("SpecialistPost", AppCompatActivity.MODE_PRIVATE)
        editorofSpePosts = sharedPrefForSpePosts.edit()
        editorofSpePosts.apply {
            putString("SpePostId",generatedpostId)
            apply()
        }
        val SpePostId = sharedPrefForSpePosts.getString("SpePostId", null).toString()

        CoroutineScope(Dispatchers.IO).launch {
            firestoreDB = FirebaseFirestore.getInstance()
            try {
                firestoreDB.collection("Users")
                    .document(userID!!)
                    .get()
                    .addOnSuccessListener { usersnapShot ->
                        signedInuser = usersnapShot.toObject(User::class.java)
                        FirstName = signedInuser?.FirstName
                        LastName = signedInuser?.LastName
                        ProfilePicture = signedInuser?.ImageURL

                        binding.tvUserNameDuringCreatingPost.text = "$FirstName $LastName"
                        // findViewById<TextView>(R.id.tvUserName).text = "$FirstName $LastName"
                        if(signedInuser?.ImageURL != null) {
                            Glide.with(this@Specialist_posts_Activity).load(ProfilePicture)
                                .into(binding.circleivUserPhotoDurringCreatingPost)
                        }else{
                            binding.circleivUserPhotoDurringCreatingPost.setImageResource(R.drawable.ic_baseline_account_circle)
                        }
                    }.await()
            } catch (e: Exception) {
                Toast.makeText(this@Specialist_posts_Activity, e.message, Toast.LENGTH_LONG).show()
            }
        }

        var pickedPic: Uri? = null
        //Picking picture from gallery, Change it and remove it
        val getImage = registerForActivityResult(
            ActivityResultContracts.GetContent(),
            ActivityResultCallback { binding.imvPostImage.setImageURI(it)
                binding.containerOfPostImage.load(it){
                    crossfade(true)
                    transformations(BlurTransformation(applicationContext))
                }
                pickedPic = it

                if(it == null){
                    binding.tvAddPhoto.visibility = View.VISIBLE
                    binding.containerOfPostImage.visibility = View.GONE
                    binding.imvPostImage.visibility = View.GONE
                    binding.btnChangePhoto.visibility = View.GONE
                    binding.btnRemovePic.visibility = View.GONE
                    //isNull.nullPhoto = true

                }
                else{
                    binding.tvAddPhoto.visibility = View.GONE
                    binding.containerOfPostImage.visibility = View.VISIBLE
                    binding.imvPostImage.visibility = View.VISIBLE
                    binding.btnChangePhoto.visibility = View.VISIBLE
                    binding.btnRemovePic.visibility = View.VISIBLE
                    //isNull.pickedPhoto = it

                }
            })

        binding.btnRemovePic.setOnClickListener {
            binding.containerOfPostImage.setImageResource(0)
            binding.imvPostImage.setImageResource(0)
            binding.tvAddPhoto.visibility = View.VISIBLE
            binding.containerOfPostImage.visibility = View.GONE
            binding.imvPostImage.visibility = View.GONE
            binding.btnChangePhoto.visibility = View.GONE
            binding.btnRemovePic.visibility = View.GONE
            pickedPic = null
        }

        binding.tvAddPhoto.setOnClickListener {
            getImage.launch("image/*")

        }

        binding.btnChangePhoto.setOnClickListener {
            getImage.launch("image/*")
        }

        binding.btnPost.isEnabled = true
        binding.btnBack.isEnabled = true
        binding.btnPost.setOnClickListener {
            handleSubmitButtonClick()
            if (pickedPic != null && binding.edPostContent.text.trim().isNotEmpty() && userID != null) {
                binding.loader.visibility = View.VISIBLE
                binding.btnPost.isEnabled = false
                binding.btnBack.isEnabled = false
                val photoUploadUri = pickedPic as Uri
                val PostPicRef = storageReference.child(SpePostId)
                PostPicRef.putFile(pickedPic!!)
                    .continueWithTask { photoUploadTask ->
                        Log.i(TAG, "uploaded bytes: ${photoUploadTask.result?.bytesTransferred}")
                        PostPicRef.downloadUrl
                    }.continueWithTask { downloadUrlTask ->
                        //create post with pic and add it to posts collection
                        Log.i(TAG, "URL: ${downloadUrlTask.result}")
                        val postWimg = hashMapOf(
                            "AuthorID" to  userID,
                            "Date"     to  System.currentTimeMillis(),
                            "ImageURL" to  downloadUrlTask.result.toString(),
                            "Text"     to  binding.edPostContent.text.toString(),
                            "id"       to  SpePostId,
                            "isPending" to isPending
                        )
                        spePostsCollectionRef.document(SpePostId).set(postWimg)
                    }.addOnCompleteListener { postwithimgCreationTask ->
                        binding.loader.visibility = View.GONE
                        if (!postwithimgCreationTask.isSuccessful){
                            Log.e(TAG, "Exception during Firebase operation", postwithimgCreationTask.exception)
                            Toast.makeText(this, "Failed to save post!", Toast.LENGTH_LONG).show()
                        }
                        else if(postwithimgCreationTask.isSuccessful && !isPending) {
                                    finish()
                        }
                        else if(postwithimgCreationTask.isSuccessful && isPending) {
                            AlertDialog.Builder(this)
                                .setTitle("Done")
                                .setIcon(R.drawable.ic_baseline_done_outline_24)
                                .setMessage("Your Post has been sent and a specialist will answer it.")
                                .setPositiveButton("Ok") { dialog_2, _ ->
                                    dialog_2.dismiss()
                                    finish()
                                }
                                .setOnDismissListener { finish() }
                                .create()
                                .show()
                        }
//                        binding.edPostContent.text.clear()
//                        binding.imvPostImage.setImageResource(0)

                    }
                // binding.btnPost.isEnabled = true
            } else if (binding.edPostContent.text.trim().isNotEmpty()) {
                binding.btnPost.isEnabled = false
                binding.btnBack.isEnabled = false
                CoroutineScope(Dispatchers.IO).launch {
                    val postWithoutPic = hashMapOf(
                        "AuthorID" to  userID,
                        "Date"     to  System.currentTimeMillis(),
                        "Text"     to  binding.edPostContent.text.toString(),
                        "id"       to  SpePostId,
                        "isPending" to isPending)

                    try {
                        spePostsCollectionRef.document(SpePostId).set(postWithoutPic).addOnCompleteListener { postwithOUTimgCreationTask->
                           if (!postwithOUTimgCreationTask.isSuccessful){
                               Log.e(TAG, "Exception during Firebase operation", postwithOUTimgCreationTask.exception)
                               Toast.makeText(this@Specialist_posts_Activity, "Failed to save post!", Toast.LENGTH_LONG).show()
                           }
                           else if(postwithOUTimgCreationTask.isSuccessful && !isPending) {
                               finish()
                           }
                           else if (postwithOUTimgCreationTask.isSuccessful && isPending) {
                               AlertDialog.Builder(this@Specialist_posts_Activity)
                                   .setTitle("Done")
                                   .setIcon(R.drawable.ic_baseline_done_outline_24)
                                   .setMessage("Your Post has been sent and a specialist will answer it.")
                                   .setPositiveButton("Ok") { dialog_2, _ ->
                                       dialog_2.dismiss()
                                       finish()
                                   }
                                   .setOnDismissListener { finish() }
                                   .create()
                                   .show()
                           }
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@Specialist_posts_Activity, e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

        }

        binding.btnBack.setOnClickListener {
            finish()
        }

    }
    private fun handleSubmitButtonClick(){
        if (binding.edPostContent.text.trim().isEmpty()){
            AlertDialog.Builder(this)
                .setTitle("Text can't be empty")
                .setIcon(R.drawable.ic_baseline_sentiment_dissatisfied_24)
                .setPositiveButton("Ok"){
                        dialog,_->
                    dialog.dismiss()
                }
                .create()
                .show()
            return
        }

    }
}