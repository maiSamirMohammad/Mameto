package com.example.mameto

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mameto.databinding.ActivityCommunityCommentsBinding
import com.example.mameto.databinding.ActivityCommunityPostsBinding
import com.example.mameto.databinding.ActivitySpecialistCommentsBinding
import com.example.mameto.models.Comment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "Specialist_comments_Activity"
open class Specialist_comments_Activity : AppCompatActivity() {
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth

    lateinit var sharedPrefTest: SharedPreferences
    lateinit var editorTest: SharedPreferences.Editor

    lateinit var myDB: FirebaseFirestore
    private lateinit var comments: MutableList<Comment>
    private lateinit var adapter: SpeCommentsAdapter
    lateinit var postID: String
    lateinit var GET_AuthorID: String
    //lateinit var TEST: String
    private val SpecommentCollectionRef = Firebase.firestore.collection("SpecialistComments")
    private val testpostRef = Firebase.firestore.collection("SpecialistPosts")
    private lateinit var binding: ActivitySpecialistCommentsBinding
    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialistCommentsBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        sharedPrefTest= getSharedPreferences("TESTT", AppCompatActivity.MODE_PRIVATE)
//        editorTest = sharedPrefTest.edit()
//        val TEST = sharedPrefTest.getString("TEST", null)

            GET_AuthorID = intent.getStringExtra("GET_AuthorID").toString()
            //TEST = intent.getStringExtra("TEST").toString()

            sharedPref = getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
            editor = sharedPref.edit()
            val userID = sharedPref.getString("userID",null)
            if(userID == null || userID != GET_AuthorID){
                binding.edWriteComment.visibility = View.GONE
                binding.imSendComment.visibility = View.GONE
            }
            else{
                binding.edWriteComment.visibility = View.VISIBLE
                binding.imSendComment.visibility = View.VISIBLE
            }

            val UserRef = FirebaseFirestore.getInstance().collection("Users")
            UserRef.document(userID.toString()).addSnapshotListener { snap, ex ->
                val IsSpecialist = snap?.get("IsSpecialist")

                    UserRef.document(GET_AuthorID).addSnapshotListener { value, error ->
                        val IsPostOwnerSpecialist = value?.get("IsSpecialist")

                    if (IsSpecialist == true && IsPostOwnerSpecialist == true || IsSpecialist == true) {
                        binding.edWriteComment.visibility = View.VISIBLE
                        binding.imSendComment.visibility = View.VISIBLE
                    }
                }
            }

        comments = mutableListOf()
        adapter = SpeCommentsAdapter(this, comments)
        binding.commentsRecyclerView.adapter = adapter
        binding.commentsRecyclerView.layoutManager = LinearLayoutManager(this)

        myDB = FirebaseFirestore.getInstance()
        var commentsRef = myDB
            .collection("SpecialistComments")
            .orderBy("Date", Query.Direction.ASCENDING)

        postID = intent.getStringExtra("KEY_2").toString()

        commentsRef = commentsRef.whereEqualTo("PostID", postID)

        commentsRef.addSnapshotListener { value, error ->
            if (error != null || value == null) {
                Log.e(TAG, "EXceptionnnnnn", error)
                return@addSnapshotListener
            }
            val tesLis = value.toObjects(Comment::class.java)
            comments.clear()
            comments.addAll(tesLis)
            adapter.notifyDataSetChanged()
            for (comment in tesLis) {
                Log.i(TAG, "Comment ${comment}")
            }
        }

//        binding.edWriteComment.setOnClickListener {
//            Toast.makeText(this, "$TEST", Toast.LENGTH_LONG).show()
//        }

        //val userID = sharedPref.getString("userID", null).toString()
        binding.imSendComment.setOnClickListener {
            if (binding.edWriteComment.text.isEmpty()) {
                AlertDialog.Builder(this)
                    .setTitle("You can't send an empty comment")
                    .setIcon(R.drawable.ic_baseline_sentiment_dissatisfied_24)
                    .setPositiveButton("Ok"){
                            dialog,_->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
            if (binding.edWriteComment.text.trim().isNotEmpty()) {
                val autoGeneratedCommentID = SpecommentCollectionRef.document().id
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val data = hashMapOf(
                            "AuthorID" to userID,
                            "Date" to System.currentTimeMillis(),
                            "PostID" to postID,
                            "Text" to binding.edWriteComment.text.toString(),
                            "id" to autoGeneratedCommentID
                        )
                        SpecommentCollectionRef.document(autoGeneratedCommentID).set(data).addOnSuccessListener {
                            val UserRef = FirebaseFirestore.getInstance().collection("Users")
                            UserRef.document(userID.toString()).addSnapshotListener { value, error ->
                                val IsSpecialist = value?.get("IsSpecialist")

                                if (IsSpecialist == true) {
                                    val spePostsCollectionRef = FirebaseFirestore.getInstance().collection("SpecialistPosts")
                                    spePostsCollectionRef.document(postID).update("isPending", false)
                                }
                            }
                        }
                            .addOnCompleteListener {
                                binding.edWriteComment.setText("")
                                testpostRef.document(postID).update(
                                    "Comments",
                                    FieldValue.arrayUnion(autoGeneratedCommentID)
                                )
                            }
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@Specialist_comments_Activity,
                            e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}