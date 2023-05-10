package com.example.mameto.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.RoundedCornersTransformation
import com.example.mameto.R

class DetailKidsAreaVPAdapter (val images: Array<String>): RecyclerView.Adapter<DetailKidsAreaVPAdapter.DetailKidsAreaVPViewHolder>() {



    /**
     * Initialize view elements
     */
    class DetailKidsAreaVPViewHolder(view: View?): RecyclerView.ViewHolder(view!!) {
        // TODO: Declare and initialize  UI components
        val imageView: ImageView? = view?.findViewById(R.id.single_image)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetailKidsAreaVPViewHolder {

        val adapterLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_kids_area_img_swipe, parent, false)


        return DetailKidsAreaVPViewHolder(adapterLayout)
    }

    override fun getItemCount()= images.size // TODO: return the size of the data set

    override fun onBindViewHolder(holder: DetailKidsAreaVPViewHolder, position: Int) {
        val imgUrl = images[position]
        imgUrl.let {
            holder.imageView?.load(imgUrl) {
                placeholder(R.drawable.loading_animation)
                error(R.drawable.ic_broken_image)
                transformations(RoundedCornersTransformation(20f))
            }
        }

    }
}