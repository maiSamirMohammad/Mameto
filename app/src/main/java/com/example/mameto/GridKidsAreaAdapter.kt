package com.example.mameto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import com.example.mameto.R
import com.example.mameto.models.KidsArea
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.startActivity


class GridKidsAreaAdapter (var kidsAreaList: MutableList<KidsArea>,var scrollFlag:Int, private val context: Context): RecyclerView.Adapter<GridKidsAreaAdapter.GridKidsAreaViewHolder>() {
    private lateinit var mListener: OnItemClickListener
    interface OnItemClickListener {
        fun onItemClick(position:Int)
    }
    fun setOnItemClickListener(listener: OnItemClickListener){
        mListener = listener
    }

    /**
     * Initialize view elements
     */


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GridKidsAreaViewHolder {
        // TODO Inflate the layout
        if (scrollFlag%2==0){
            val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.grid_kids_area_item, parent, false)
            return GridKidsAreaViewHolder(adapterLayout,mListener)
        }else{
            val adapterLayout = LayoutInflater.from(parent.context)
                .inflate(R.layout.vertical_kids_area_item, parent, false)
            return GridKidsAreaViewHolder(adapterLayout,mListener)
        }

    }

    override fun getItemCount()= kidsAreaList.size // TODO: return the size of the data set

    override fun onBindViewHolder(holder: GridKidsAreaViewHolder, position: Int) {
        if (scrollFlag%2==0){
            val item = kidsAreaList[position]
            val imgUrl = item.Photos[0]
            //Picasso.get().load(imgUrl).into(holder.kidsAreaImage)
            imgUrl.let {
                holder.kidsAreaImage?.load(imgUrl) {
                    placeholder(R.drawable.loading_animation)
                    error(R.drawable.ic_broken_image)
                }
            }
            holder.kidsAreaName?.text =item.Name
            holder.kidsAreaCity?.text =item.City
        }else{
            val item = kidsAreaList[position]
            val imgUrl = item.Photos[0]
            imgUrl.let {
                holder.image_ver?.load(imgUrl) {
                    placeholder(R.drawable.loading_animation)
                    error(R.drawable.ic_broken_image)
                }
            }
            holder.name_ver?.text =item.Name
            holder.city_ver?.text =item.City
            holder.address_ver?.text =item.Address
            holder.children_age_range_ver?.text = context.resources.getString(R.string.children_age_range,item.ChildrenAgeRange["From"] , item.ChildrenAgeRange["To"])
            //call kids_area
            holder.phone_number_ver?.setOnClickListener {
                var uri = Uri.parse("tel:${item.PhoneNumber}")
                context.startActivity(Intent(Intent.ACTION_VIEW,uri))
            }
            //Open kids_area location on the  map
            holder.location_ver?.setOnClickListener {
                // Creates an Intent that will load a map
                val gmmIntentUri =
                    Uri.parse("geo:0,0?q=${item.Name}+${item.Address}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
            }

        }




    }

    class GridKidsAreaViewHolder(itemView: View?, listener: OnItemClickListener): RecyclerView.ViewHolder(itemView!!) {
        // TODO: Declare and initialize all of the grid item UI components.
        val kidsAreaImage: ImageView? = itemView?.findViewById(R.id.kids_area_image)
        val kidsAreaName: TextView? = itemView?.findViewById(R.id.kids_area_name)
        val kidsAreaCity: TextView? = itemView?.findViewById(R.id.kids_area_city)
        //vertical
        val image_ver: ImageView? = itemView?.findViewById(R.id.image_ver)
        val name_ver: TextView? = itemView?.findViewById(R.id.name_ver)
        val address_ver: TextView? = itemView?.findViewById(R.id.address_ver)
        val city_ver: TextView? = itemView?.findViewById(R.id.city_ver)
        val children_age_range_ver: TextView? = itemView?.findViewById(R.id.children_age_range_ver)

        val phone_number_ver: TextView? = itemView?.findViewById(R.id.phone_number_ver)
        val location_ver: TextView? = itemView?.findViewById(R.id.location_ver)

        init {
            itemView!!.setOnClickListener {
                listener.onItemClick(bindingAdapterPosition)
            }
        }
    }
}

