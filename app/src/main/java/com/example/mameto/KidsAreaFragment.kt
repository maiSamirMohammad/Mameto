package com.example.mameto


import android.annotation.SuppressLint
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
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.mameto.adapter.GridKidsAreaAdapter
import com.example.mameto.databinding.FragmentKidsAreaBinding
import com.example.mameto.models.KidsArea
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*

class KidsAreaFragment : Fragment() {


    private var _binding: FragmentKidsAreaBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val kidsAreaCollectionRef = Firebase.firestore.collection("KidsArea")
    private lateinit var recyclerView: RecyclerView
    private var kidsAreaList = mutableListOf<KidsArea>()
    private var displayList = mutableListOf<KidsArea>()
    lateinit var sharedPref: SharedPreferences
    lateinit var editor: SharedPreferences.Editor





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPref = requireContext().getSharedPreferences("User", AppCompatActivity.MODE_PRIVATE)
        editor = sharedPref.edit()
        editor.apply {
            putInt("scrollFlag",2)
            apply()
        }
        // Retrieve and inflate the layout for this fragment
        _binding = FragmentKidsAreaBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val dialog22 = MaterialDialog(requireContext())
            .noAutoDismiss()
            .customView(R.layout.loader_dialog)
        recyclerView = binding.gridRecyclerView
        var scrollFlag = sharedPref.getInt("scrollFlag",2)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                    dialog22.show()
                    delay(1000)
                }
                val querySnapshot = kidsAreaCollectionRef.get().await()
                for(document in querySnapshot.documents) {
                    val kidsArea = document.toObject<KidsArea>()
                    if (kidsArea!=null){
                        kidsAreaList.add(kidsArea)
                    }
                }
                displayList.addAll(kidsAreaList)
                withContext(Dispatchers.Main) {
                    dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                    dialog22.dismiss()
                    recyclerView.layoutManager = GridLayoutManager(this@KidsAreaFragment.requireContext(), 2)
                    val adapter= GridKidsAreaAdapter(displayList,scrollFlag,requireContext())
                    recyclerView.adapter=adapter
                    adapter.setOnItemClickListener(object :GridKidsAreaAdapter.OnItemClickListener{
                        override fun onItemClick(position: Int) {
                            val intent = Intent(context,DetailKidsAreaActivity::class.java)
                            intent.putExtra("Photos",displayList[position].Photos.toTypedArray())
                            intent.putExtra("Name",displayList[position].Name)
                            intent.putExtra("Address",displayList[position].Address)
                            intent.putExtra("City",displayList[position].City)
                            intent.putExtra("ChildrenAgeRangeFrom",displayList[position].ChildrenAgeRange["From"])
                            intent.putExtra("ChildrenAgeRangeTo",displayList[position].ChildrenAgeRange["To"])
                            intent.putExtra("PhoneNumber",displayList[position].PhoneNumber)
                            intent.putExtra("WorkingHoursFrom",displayList[position].WorkingHours["From"])
                            intent.putExtra("WorkingHoursTo",displayList[position].WorkingHours["To"])
                            intent.putExtra("Latitude",displayList[position].Location["Latitude"])
                            intent.putExtra("Longitude",displayList[position].Location["Longitude"])
                            startActivity(intent)
                        }

                    })

                }
            } catch(e: Exception) {
                dialog22.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                dialog22.dismiss()
                Log.d("TAG","AN ERROR ${e.message}")
                val dialog2 = MaterialDialog(requireContext())
                    .noAutoDismiss()
                    .customView(R.layout.successful_reset_password)
                dialog2.findViewById<TextView>(R.id.ok_btn).setOnClickListener { dialog2.dismiss() }
                dialog2.findViewById<TextView>(R.id.dialog_title).text="Sorry"
                dialog2.findViewById<TextView>(R.id.dialog_content).text= e.message
                dialog2.show()
            }
        }
        binding.searchView.setOnQueryTextListener(object :SearchView.OnQueryTextListener,
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(p0: String?): Boolean {
                return true
            }

            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextChange(p0: String?): Boolean {
                if (p0!!.isNotEmpty()){
                    displayList.clear()
                    var search = p0.lowercase(Locale.getDefault())
                    for (kidsArea in kidsAreaList){
                        if (kidsArea.Name.lowercase(Locale.getDefault()).contains(search)) {
                            displayList.add(kidsArea)
                            binding.notFoundImg.visibility = View.GONE
                            binding.notFoundText.visibility = View.GONE

                        }




                        recyclerView.adapter?.notifyDataSetChanged()

                    }
               /*     binding.notFoundImg.visibility = View.GONE
                    binding.notFoundText.visibility = View.GONE*/

                }else{
                    binding.notFoundImg.visibility = View.GONE
                    binding.notFoundText.visibility = View.GONE
                    displayList.clear()
                    displayList.addAll(kidsAreaList)
                    recyclerView.adapter?.notifyDataSetChanged()


                }

                return true
            }
        })

        binding.verticalBtn.setOnClickListener {
            var scrollFlag = sharedPref.getInt("scrollFlag",2)
            scrollFlag+=1
            if (scrollFlag%2==0){
                recyclerView.layoutManager = GridLayoutManager(this.requireContext(), 2)

                editor.apply {
                    putInt("scrollFlag",scrollFlag)
                    apply()
                }
                val adapter= GridKidsAreaAdapter(displayList,scrollFlag,requireContext())
                recyclerView.adapter=adapter
                binding.verticalBtn.setImageResource(R.drawable.format_list_bulleted)
                adapter.setOnItemClickListener(object :GridKidsAreaAdapter.OnItemClickListener{
                    override fun onItemClick(position: Int) {
                        val intent = Intent(context,DetailKidsAreaActivity::class.java)
                        intent.putExtra("Photos",displayList[position].Photos.toTypedArray())
                        intent.putExtra("Name",displayList[position].Name)
                        intent.putExtra("Address",displayList[position].Address)
                        intent.putExtra("City",displayList[position].City)
                        intent.putExtra("ChildrenAgeRangeFrom",displayList[position].ChildrenAgeRange["From"])
                        intent.putExtra("ChildrenAgeRangeTo",displayList[position].ChildrenAgeRange["To"])
                        intent.putExtra("PhoneNumber",displayList[position].PhoneNumber)
                        intent.putExtra("WorkingHoursFrom",displayList[position].WorkingHours["From"])
                        intent.putExtra("WorkingHoursTo",displayList[position].WorkingHours["To"])
                        intent.putExtra("Latitude",displayList[position].Location["Latitude"])
                        intent.putExtra("Longitude",displayList[position].Location["Longitude"])
                        startActivity(intent)
                    }

                })

            }else{
                recyclerView.layoutManager = LinearLayoutManager(this.requireContext())

                editor.apply {
                    putInt("scrollFlag",scrollFlag)
                    apply()
                }
                val adapter= GridKidsAreaAdapter(displayList,scrollFlag,requireContext())
                recyclerView.adapter=adapter
                binding.verticalBtn.setImageResource(R.drawable.ic_grid)
                adapter.setOnItemClickListener(object :GridKidsAreaAdapter.OnItemClickListener{
                    override fun onItemClick(position: Int) {
                        val intent = Intent(context,DetailKidsAreaActivity::class.java)
                        intent.putExtra("Photos",displayList[position].Photos.toTypedArray())
                        intent.putExtra("Name",displayList[position].Name)
                        intent.putExtra("Address",displayList[position].Address)
                        intent.putExtra("City",displayList[position].City)
                        intent.putExtra("ChildrenAgeRangeFrom",displayList[position].ChildrenAgeRange["From"])
                        intent.putExtra("ChildrenAgeRangeTo",displayList[position].ChildrenAgeRange["To"])
                        intent.putExtra("PhoneNumber",displayList[position].PhoneNumber)
                        intent.putExtra("WorkingHoursFrom",displayList[position].WorkingHours["From"])
                        intent.putExtra("WorkingHoursTo",displayList[position].WorkingHours["To"])
                        intent.putExtra("Latitude",displayList[position].Location["Latitude"])
                        intent.putExtra("Longitude",displayList[position].Location["Longitude"])
                        startActivity(intent)
                    }

                })



            }

        }

    }

    //private fun retrieveKidsAreas() =

    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}