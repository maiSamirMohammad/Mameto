package com.example.mameto

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.example.mameto.*
import com.example.mameto.Community_comments_Activity
import com.example.mameto.Community_posts_Activity
//import com.example.my.Adapters.PostsAdapter
//import com.example.my.Post
import com.example.mameto.databinding.FragmentCommunityBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import okhttp3.internal.cache.DiskLruCache
import java.util.*

private const val TAG = "CommunityFragment"
class CommunityFragment : Fragment(R.layout.fragment_community) {

    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor
    lateinit var auth: FirebaseAuth
     private var _binding: FragmentCommunityBinding? = null
     private val binding get() = _binding!!
    private lateinit var firestoreDB: FirebaseFirestore
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    private lateinit var users: MutableList<User>
    private lateinit var posts: MutableList<Post>
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //////////////////////////////////////////////////////////////////////////
        sharedPref = requireContext().getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()

        posts = mutableListOf()
        adapter = PostsAdapter(requireContext(), posts){ post ->
            val intent = Intent(context, Community_comments_Activity::class.java)
            intent.putExtra("KEY", post.id)
            startActivity(intent)
        }
        binding.mRecyclerView.adapter = adapter
        binding.mRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        firestoreDB = FirebaseFirestore.getInstance()

        val postsReference = firestoreDB
            .collection("Posts")
            .orderBy("Date", Query.Direction.DESCENDING)
        postsReference.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Excption when querying posts", exception)
                return@addSnapshotListener
            }
            val postsList = snapshot.toObjects(Post::class.java)
            posts.clear()
            posts.addAll(postsList)
            adapter.notifyDataSetChanged()
            for (post in postsList) {
                Log.i(TAG, "Post ${post}")
            }
        }

        auth = Firebase.auth
        sharedPref= requireContext().getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor= sharedPref.edit()

        binding.imbtnCreatePost.setOnClickListener {

           val currentuser = Firebase.auth.currentUser

            if (currentuser == null){
                Toast.makeText(requireContext(), "You didn't sign in yet!", Toast.LENGTH_LONG).show()
            }
            else{
                val intent = Intent(context, Community_posts_Activity::class.java)
                startActivity(intent)
            }


        }

        binding.mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val userID = sharedPref.getString("userID",null)
                if (dy > 0){
                    binding.imbtnCreatePost.hide()
                }else{
                    binding.imbtnCreatePost.show()

                }
                if(userID == null){
                    binding.imbtnCreatePost.visibility = View.GONE
                }
                else{
                    binding.imbtnCreatePost.visibility = View.VISIBLE
                }
            }
        })

    }
    private fun mm (){
        val usersRefr = firestoreDB.collection("Users")
        usersRefr.addSnapshotListener { value, error ->
            if (error != null || value == null){
                Log.e(TAG, "Excption when querying posts", error)
                return@addSnapshotListener
            }
            val userlist = value.toObjects(User::class.java)
            users.clear()
            users.addAll(userlist)
            adapter.notifyDataSetChanged()
            for (user in userlist){
                Log.i(TAG, "Post ${user}")

            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}