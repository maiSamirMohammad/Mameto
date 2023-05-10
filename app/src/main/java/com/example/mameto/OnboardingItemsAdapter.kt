package com.example.mameto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OnboardingItemsAdapter (private val onboardingItems : List<OnboardingItem>) : RecyclerView.Adapter<OnboardingItemsAdapter.OnboardingItemViewHolder>(){

//Called by RecyclerView to display the data at the specified position.
// This method should update the contents of the RecyclerView.
// ViewHolder.itemView to reflect the item at the given position.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingItemViewHolder {
        return OnboardingItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.slider_layout, parent, false))
    }

//Called by RecyclerView to display the data at the specified position.
// This method should update the contents of the RecyclerView.
// ViewHolder.itemView to reflect the item at the given position.
    override fun onBindViewHolder(holder: OnboardingItemViewHolder, position: Int) {
        holder.bind(onboardingItems[position])
    }

    override fun getItemCount(): Int {
        return onboardingItems.size

    }

    inner class OnboardingItemViewHolder (view : View) : RecyclerView.ViewHolder(view){

        private val imageOnboarding = view.findViewById<ImageView>(R.id.imageOnboarding)
        private val textTitle = view.findViewById<TextView>(R.id.textTitle)


        fun bind (onboardingItem: OnboardingItem){

            imageOnboarding.setImageResource(onboardingItem.onboardingImage)
            textTitle.text = onboardingItem.title

        }
    }
}