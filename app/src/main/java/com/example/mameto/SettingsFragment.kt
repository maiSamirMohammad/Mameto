package com.example.mameto


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import coil.api.load
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.databinding.FragmentSettingsBinding
import com.example.mameto.databinding.GuestSettingsBinding
import com.example.mameto.databinding.SpecialistSettingsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*

class SettingsFragment : Fragment() {
    lateinit var auth: FirebaseAuth
    private val usersCollectionRef = Firebase.firestore.collection("Users")
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor


    // AuthenticatedUserSettings
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    // GuestSettings
    private var _binding2: GuestSettingsBinding? = null
    private val binding2 get() = _binding2!!
    // SpecialistSettings
    private var _binding3: SpecialistSettingsBinding? = null
    private val binding3 get() = _binding3!!





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("fragmentoz","onCreate")


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("fragmentoz","onCreateView")
        sharedPref = requireContext().getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()
        val view: ConstraintLayout
        val userID = sharedPref.getString("userID",null)
        val isSpecialist = sharedPref.getBoolean("isSpecialist",false)
        if (userID != null ){

            if (isSpecialist == true){
                // Retrieve and inflate the layout for SpecialistSettings
                _binding3 = SpecialistSettingsBinding.inflate(inflater, container, false)
                view = binding3.root
            }else{
                // Retrieve and inflate the layout for FragmentSettings
                _binding = FragmentSettingsBinding.inflate(inflater, container, false)
                view = binding.root
            }
            Log.d("TAG", "Nopppsuchdocumenttt  ${isSpecialist}")





        }else{
             // Retrieve and inflate the layout for GuestSettings
             _binding2 = GuestSettingsBinding.inflate(inflater, container, false)
              view = binding2.root
         }

        return view
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        Log.d("fragmentoz","onViewCreated")
        val dialog22 = MaterialDialog(requireContext())
            .noAutoDismiss()
            .customView(R.layout.loader_dialog)

        ///////////////////
        val sharedpref_welcome = requireContext().getSharedPreferences("welcome", Context.MODE_PRIVATE)
        val editor_welcome = sharedpref_welcome.edit()


        auth = Firebase.auth
        val userID = sharedPref.getString("userID",null)
        if (userID != null ){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    withContext(Dispatchers.Main) {
                    dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                    dialog22.show()
                        delay(1000)
                }
                    val querySnapshot = usersCollectionRef.document(userID).get().addOnSuccessListener { document ->
                        if (document != null) {
                            //withContext(Dispatchers.Main) {

                            dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                            dialog22.dismiss()
                                val user = document.toObject<User>()
                                Log.d("IsSpecialist","IsSpecialist3st ${user?.IsSpecialist}")

                                if (user?.IsSpecialist==true){
                                    binding3.username.text= resources.getString(R.string.username, user?.FirstName, user?.LastName)
                                    if (user?.ImageURL!=null){
                                        user?.ImageURL?.let {
                                            binding3.sProfileImage.load(user?.ImageURL) {
                                                placeholder(R.drawable.loading_animation)
                                                error(R.drawable.ic_broken_image)
                                            }
                                        }
                                    }else{
                                        binding3.sProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
                                    }

                                    binding3.editBtn.setOnClickListener {
                                        val intent = Intent(context,EditProfileActivity::class.java)
                                        startActivity(intent)
                                    }
                                    binding3.logoutBtn.setOnClickListener {
                                        val dialog3 = MaterialDialog(requireContext())
                                            .noAutoDismiss()
                                            .customView(R.layout.logout_dialog)
                                        dialog3.findViewById<TextView>(R.id.log_out_dialog_btn).setOnClickListener {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    auth.signOut()
                                                    withContext(Dispatchers.Main) {
                                                    //////////////////////////////////////
                                                        editor_welcome.putBoolean("log_out_clicked", true)
                                                        editor_welcome.apply()

                                                        editor.apply {
                                                            putString("userID",null)
                                                            apply()
                                                        }
                                                        editor.apply {
                                                            val skipOrGetstartedState=false
                                                            putBoolean("skipOrGetstartedState",skipOrGetstartedState)
                                                            apply()
                                                        }
                                                        editor.apply {
                                                            val firstTime=false
                                                            putBoolean("firstTime",firstTime)
                                                            apply()
                                                        }
                                                        editor.apply {
                                                            val isSpecialist=false
                                                            putBoolean("isSpecialist",isSpecialist)
                                                            apply()
                                                        }

                                                        val intent = Intent(context,AfterSplashActivity::class.java)
                                                        startActivity(intent)
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        // If signout fails, display a message to the user.
                                                        val dialog2 = MaterialDialog(requireContext())
                                                            .noAutoDismiss()
                                                            .customView(R.layout.successful_reset_password)
                                                        dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                                                        dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                                        dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                                                        dialog2.show()

                                                    }
                                                }
                                            }                        }
                                        dialog3.findViewById<TextView>(R.id.no_btn).setOnClickListener { dialog3.dismiss() }
                                        dialog3.show()
                                    }
                                }else{
                                    binding.username.text= resources.getString(R.string.username, user?.FirstName, user?.LastName)
                                    if (user?.ImageURL !=null){
                                        user?.ImageURL?.let {
                                            binding.profileImage.load(user?.ImageURL) {
                                                placeholder(R.drawable.loading_animation)
                                                error(R.drawable.ic_broken_image)
                                            }
                                        }
                                        //Glide.with(this@SettingsFragment).load(user.ImageURL).into(binding.profileImage)
                                    }else{
                                        binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                                    }
                                    binding.profileImage.setOnClickListener {
                                        val intent = Intent(context,UpdateProfilePicActivity::class.java)
                                        startActivity(intent)
                                    }
                                    binding.icEditImage.setOnClickListener {
                                        val intent = Intent(context,UpdateProfilePicActivity::class.java)
                                        startActivity(intent)
                                    }
                                    binding.editProfileBtn.setOnClickListener {
                                        val intent = Intent(context,EditProfileActivity::class.java)
                                        startActivity(intent)
                                    }


                                    binding.logoutBtn.setOnClickListener {
                                        val dialog3 = MaterialDialog(requireContext())
                                            .noAutoDismiss()
                                            .customView(R.layout.logout_dialog)
                                        dialog3.findViewById<TextView>(R.id.log_out_dialog_btn).setOnClickListener {
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {

                                                    auth.signOut()
                                                    withContext(Dispatchers.Main) {
                                                        ///////
                                                        editor_welcome.putBoolean("log_out_clicked", true)
                                                        editor_welcome.apply()

                                                        editor.apply {
                                                            putString("userID",null)
                                                            apply()
                                                        }

                                                        editor.apply {
                                                            val skipOrGetstartedState=false
                                                            putBoolean("skipOrGetstartedState",skipOrGetstartedState)
                                                            apply()
                                                        }
                                                        editor.apply {
                                                            val firstTime=false
                                                            putBoolean("firstTime",firstTime)
                                                            apply()
                                                        }
                                                        editor.apply {
                                                            val IsSpecialist=false
                                                            putBoolean("IsSpecialist",IsSpecialist)
                                                            apply()
                                                        }

                                                        val intent = Intent(context,AfterSplashActivity::class.java)
                                                        startActivity(intent)
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        // If signout fails, display a message to the user.
                                                        val dialog2 = MaterialDialog(requireContext())
                                                            .noAutoDismiss()
                                                            .customView(R.layout.successful_reset_password)
                                                        dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                                                        dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                                                        dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                                                        dialog2.show()

                                                    }
                                                }
                                            }                        }
                                        dialog3.findViewById<TextView>(R.id.no_btn).setOnClickListener { dialog3.dismiss() }
                                        dialog3.show()
                                    }
                                }


                           // }
                            Log.d("TAG", "DocumentSnapshot data: ${document.data}")
                        } else {
                            dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                            dialog22.dismiss()
                            Log.d("TAG", "No such document")
                        }
                    }
                        .addOnFailureListener { exception ->
                            dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                            dialog22.dismiss()
                            Log.d("TAG", "get failed with ", exception)
                        }


                 }  catch(e: Exception) {
                       withContext(Dispatchers.Main) {
                           dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                           dialog22.dismiss()
                           val dialog2 = MaterialDialog(requireContext())
                               .noAutoDismiss()
                               .customView(R.layout.successful_reset_password)
                           dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                           dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                           dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                           dialog2.show()
                       }
                   }
               }

        }else {
            binding2.loginbutton.setOnClickListener {
                val intent = Intent(context,MainActivity::class.java)
                startActivity(intent)
            }
        }


    }


/*
    override fun onStart() {
        super.onStart()
        Log.d("fragmentoz","onStart")
        val userID = sharedPref.getString("userID",null)
       // Log.d("fragmentoz","userID${userID}")

        if (userID != null ){

            val querySnapshot = usersCollectionRef.document(userID).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject<User>()
                        if (user?.IsSpecialist==true){
                            binding3.username.text= resources.getString(R.string.username, user?.FirstName, user?.LastName)
                            if (user?.ImageURL!=null){
                                user?.ImageURL?.let {
                                    binding3.sProfileImage.load(user?.ImageURL) {
                                        placeholder(R.drawable.loading_animation)
                                        error(R.drawable.ic_broken_image)
                                    }
                                }
                            }else{
                                binding3.sProfileImage.setImageResource(R.drawable.ic_baseline_account_circle)
                            }
                        }else{
                            binding.username.text= resources.getString(R.string.username, user?.FirstName, user?.LastName)
                            if (user?.ImageURL !=null){
                                user?.ImageURL?.let {
                                    binding.profileImage.load(user?.ImageURL) {
                                        placeholder(R.drawable.loading_animation)
                                        error(R.drawable.ic_broken_image)
                                    }
                                }
                                //Glide.with(this@SettingsFragment).load(user.ImageURL).into(binding.profileImage)
                            }else{
                                binding.profileImage.setImageResource(R.drawable.ic_baseline_account_circle)
                            }
                        }

                    } else {
                        Log.d("TAG", "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d("TAG", "get failed with ", exception)
                }
        }
    }*/

    override fun onResume() {
        super.onResume()
        Log.d("fragmentoz","onResume")

        val userID = sharedPref.getString("userID",null)
        // Log.d("fragmentoz","userID${userID}")

        if (userID != null ){

            val querySnapshot = usersCollectionRef.document(userID).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject<User>()
                        if (user?.IsSpecialist==true){
                            binding3.username.text= resources.getString(R.string.username, user?.FirstName, user?.LastName)
                            if (user?.ImageURL!=null){
                                user?.ImageURL?.let {
                                    binding3.sProfileImage.load(user?.ImageURL) {
                                        placeholder(R.drawable.loading_animation)
                                        error(R.drawable.ic_broken_image)
                                    }
                                }
                            }else{
                                binding3.sProfileImage.setImageResource(R.drawable.ic_baseline_person_24)
                            }
                        }else{
                            binding.username.text= resources.getString(R.string.username, user?.FirstName, user?.LastName)
                            if (user?.ImageURL !=null){
                                user?.ImageURL?.let {
                                    binding.profileImage.load(user?.ImageURL) {
                                        placeholder(R.drawable.loading_animation)
                                        error(R.drawable.ic_broken_image)
                                    }
                                }
                                //Glide.with(this@SettingsFragment).load(user.ImageURL).into(binding.profileImage)
                            }else{
                                binding.profileImage.setImageResource(R.drawable.ic_baseline_person_24)
                            }
                        }

                    } else {
                        Log.d("TAG", "No such document")
                        val dialog2 = MaterialDialog(requireContext())
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
                    val dialog2 = MaterialDialog(requireContext())
                        .noAutoDismiss()
                        .customView(R.layout.successful_reset_password)
                    dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                    dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                    dialog2.findViewById<TextView>(R.id.dialog_content).text= "Cannot retrieve user data"
                    dialog2.show()
                }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("fragmentoz","onDestroyView")

    }

    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
        _binding2 = null
        _binding3 = null
    }









}