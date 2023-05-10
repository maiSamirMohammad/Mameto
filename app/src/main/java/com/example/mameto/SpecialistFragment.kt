package com.example.mameto

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mameto.databinding.FragmentCommunityBinding
import com.example.mameto.databinding.FragmentSettingsBinding
import com.example.mameto.databinding.FragmentSpecialistBinding
import com.example.mameto.models.SpecialistPost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

private const val TAG = "SpecialistFragment"
class SpecialistFragment : Fragment(R.layout.fragment_specialist) {
    private var _binding: FragmentSpecialistBinding? = null
    private val binding get() = _binding!!
    lateinit var sharedPref: SharedPreferences
    private var state: SpecialistPost? = null
    private var isPending: Boolean? = null
    lateinit var editor: SharedPreferences.Editor
    private lateinit var firestoreDB: FirebaseFirestore
    private lateinit var posts: MutableList<SpecialistPost>
    private lateinit var adapter: SpePostsAdapter
    lateinit var sharedPrefForSpePosts: SharedPreferences
    lateinit var editorofSpePosts: SharedPreferences.Editor


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?


    ): View? {
        _binding = FragmentSpecialistBinding.inflate(inflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPref = requireContext().getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()

        val useriD = sharedPref.getString("userID",null).toString()
        //val Authorid = comment.AuthorID.toString()
        val UserRef = FirebaseFirestore.getInstance().collection("Users")
        UserRef.document(useriD).addSnapshotListener { value, error ->
            val IsSpecialist = value?.get("IsSpecialist")

            if (IsSpecialist == true) {
                if (_binding != null){
                    binding.imSpecialistNotification.visibility = View.VISIBLE
                    binding.imview.visibility = View.VISIBLE
                }

            } else {
                //TODO NULL CHECK
                if (_binding != null){
                    binding.imSpecialistNotification.visibility = View.GONE
                    binding.imview.visibility = View.GONE
                }


            }
        }
        sharedPrefForSpePosts = requireContext().getSharedPreferences("SpecialistPost", AppCompatActivity.MODE_PRIVATE)
        editorofSpePosts = sharedPrefForSpePosts.edit()
        val SpePostId = sharedPrefForSpePosts.getString("SpePostId", null).toString()

//        val refPosts = FirebaseFirestore.getInstance().collection("SpecialistPosts")
//        refPosts.document(SpePostId).addSnapshotListener { value, error ->
//            if (value?.get("Comments") != null) {
//                val tt = value.get("Comments") as List<*>
//            }
//        }

        posts = mutableListOf()
        adapter = SpePostsAdapter(requireContext(), posts){ post ->
            val intent = Intent(context, Specialist_comments_Activity::class.java)
            intent.putExtra("KEY_2", post.id)
            intent.putExtra("GET_AuthorID", post.AuthorID)
            startActivity(intent)
        }
        binding.speRecyclerView.adapter = adapter
        binding.speRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        firestoreDB = FirebaseFirestore.getInstance()

        var SpecialistPostsRef = firestoreDB.collection("SpecialistPosts")
            .orderBy("Date", Query.Direction.DESCENDING)

        SpecialistPostsRef = SpecialistPostsRef.whereEqualTo("isPending", false)

        SpecialistPostsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "Exception", exception)
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

        //get count of not answered posts
        var specialistPostsRef = firestoreDB.collection("SpecialistPosts")
            .orderBy("Date", Query.Direction.DESCENDING)

        specialistPostsRef = specialistPostsRef.whereEqualTo("isPending", true)

        specialistPostsRef.addSnapshotListener { snapshot, exception ->
            if (exception != null || snapshot == null) {
                Log.e(TAG, "EXceptionnnAccept", exception)
                return@addSnapshotListener
            }
            val postsList = snapshot.toObjects(SpecialistPost::class.java)
            var count = 0
            for (post in postsList) {
                Log.i(TAG, "Post ${post}")
                count++
            }
            //TODO NULL CHECK
            if (_binding!=null){
                binding.imview.text = "$count"

            }

        }




        binding.imbtnCreatePost.setOnClickListener {

            val currentuser = Firebase.auth.currentUser

            if (currentuser == null){
                Toast.makeText(requireContext(), "You didn't sign in yet!", Toast.LENGTH_LONG).show()
            }
            else{
                val intent = Intent(context, Specialist_posts_Activity::class.java)
                startActivity(intent)
            }

        }

        binding.speRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
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


        binding.imSpecialistNotification.setOnClickListener {
            val intent = Intent(context, SpecialistAcceptingActivity::class.java)
          //Toast.makeText(context, "$postId", Toast.LENGTH_LONG).show()
            startActivity(intent)
        }



    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}