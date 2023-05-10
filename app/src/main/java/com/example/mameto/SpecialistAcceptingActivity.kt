package com.example.mameto

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mameto.databinding.ActivitySpecialistAcceptingBinding
import com.example.mameto.databinding.ActivitySpecialistPostsBinding
import com.example.mameto.models.SpecialistPost
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
private const val TAG = "SpecialistAcceptingActivity"
class SpecialistAcceptingActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySpecialistAcceptingBinding
    lateinit var sharedPref: SharedPreferences
//    private var state: SpecialistPost? = null
//    private var isPending: Boolean? = null
    lateinit var editor: SharedPreferences.Editor
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var posts: MutableList<SpecialistPost>
    private lateinit var adapter: SpeAcceptingAdapter
    lateinit var sharedPrefForSpePosts: SharedPreferences
    lateinit var editorofSpePosts: SharedPreferences.Editor


    @SuppressLint("LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpecialistAcceptingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPref = getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()

        posts = mutableListOf()
        adapter = SpeAcceptingAdapter(this, posts) { post ->
            val intent = Intent(this, Specialist_comments_Activity::class.java)
            intent.putExtra("GET_AuthorID", post.AuthorID)
            intent.putExtra("KEY_2", post.id)
            startActivity(intent)
        }

        binding.speRecyclerViewAccepting.adapter = adapter
        binding.speRecyclerViewAccepting.layoutManager = LinearLayoutManager(this)
        firestoreDB = FirebaseFirestore.getInstance()

        sharedPrefForSpePosts = getSharedPreferences("SpecialistPost", AppCompatActivity.MODE_PRIVATE)
        editorofSpePosts = sharedPrefForSpePosts.edit()

        var SpecialistPostsRef = firestoreDB.collection("SpecialistPosts")
            .orderBy("Date", Query.Direction.DESCENDING)

            SpecialistPostsRef = SpecialistPostsRef.whereEqualTo("isPending", true)

            SpecialistPostsRef.addSnapshotListener { snapshot, exception ->
                if (exception != null || snapshot == null) {
                    Log.e(TAG, "EXceptionnnAccept", exception)
                    return@addSnapshotListener
                }
                val postsList = snapshot.toObjects(SpecialistPost::class.java)
                posts.clear()
                posts.addAll(postsList)
                adapter.notifyDataSetChanged()
                for (post in postsList) {
                    Log.i(TAG, "Post ${post}")
            }
        }

    }
}