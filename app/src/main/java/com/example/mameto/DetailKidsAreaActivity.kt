package com.example.mameto


import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import coil.api.load
import com.example.mameto.adapter.DetailKidsAreaVPAdapter
import com.example.mameto.databinding.ActivityDetailKidsAreaBinding
//import com.squareup.picasso.Picasso

class DetailKidsAreaActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailKidsAreaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailKidsAreaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imgUrls = intent?.extras?.getStringArray("Photos")
        val name = intent?.extras?.getString("Name")
        val address = intent?.extras?.getString("Address")
        val city = intent?.extras?.getString("City")
        val childrenAgeRangeFrom = intent?.extras?.getInt("ChildrenAgeRangeFrom")
        val childrenAgeRangeTo = intent?.extras?.getInt("ChildrenAgeRangeTo")
        val phoneNumber = intent?.extras?.getString("PhoneNumber")
        val workingHoursFrom = intent?.extras?.getString("WorkingHoursFrom")
        val workingHoursTo = intent?.extras?.getString("WorkingHoursTo")
        val latitude = intent?.extras?.getInt("Latitude")
        val longitude = intent?.extras?.getInt("Longitude")
        //assign views values to those get extras
       // Picasso.get().load(imgUrl).into(binding.image)
      /*  imgUrl.let {
            binding.image.load(imgUrl) {
                placeholder(R.drawable.loading_animation)
                error(R.drawable.ic_broken_image)
            }
        }*/
        if(imgUrls!=null){
            val adapter = DetailKidsAreaVPAdapter(imgUrls)
            binding.viewPagerKaImages.adapter=adapter
        }
        Log.d("hellofromhere","imgUrls= $imgUrls")

        binding.activityName.text=name
        binding.name.text=name
        binding.address.text=address
        binding.city.text=city
        binding.childrenAgeRange.text= resources.getString(R.string.children_age_range, childrenAgeRangeFrom, childrenAgeRangeTo)
        binding.workingHours.text=resources.getString(R.string.range, workingHoursFrom, workingHoursTo)

        //call kids_area
        binding.phoneNumber.setOnClickListener {
            var uri = Uri.parse("tel:$phoneNumber")
            startActivity(Intent(Intent.ACTION_VIEW,uri))
        }
        //Open kids_area location on the  map
        binding.location.setOnClickListener {
            // Creates an Intent that will load a map
            val gmmIntentUri =
                Uri.parse("geo:0,0?q=$name+$address")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            startActivity(mapIntent)
        }
        binding.backBtn.setOnClickListener { finish() }





    }
}