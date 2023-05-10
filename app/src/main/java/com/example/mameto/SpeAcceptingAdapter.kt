package com.example.mameto

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.BlurTransformation
import com.bumptech.glide.Glide
import com.example.mameto.models.SpecialistPost
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

private var Authorid: String? = null
class SpeAcceptingAdapter(val context: Context, val posts: List<SpecialistPost>, val itemClick: (SpecialistPost) -> Unit): RecyclerView.Adapter<SpeAcceptingAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_2, parent, false)
        return ViewHolder(v, itemClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }


    override fun getItemCount(): Int {
        return posts.size
    }

    inner class ViewHolder(itemView: View, val itemClick: (SpecialistPost) -> Unit): RecyclerView.ViewHolder(itemView) {
        fun bind(post: SpecialistPost) {
            val AuthorID = post.AuthorID.toString()
            val ref = FirebaseFirestore.getInstance().collection("Users")
            ref.document(AuthorID).addSnapshotListener { value, error ->
                Authorid = AuthorID
                val FirstName = "${value?.get("FirstName")}"
                val LastName = "${value?.get("LastName")}"
                val ImageURL = "${value?.get("ImageURL")}"

                itemView.findViewById<TextView>(R.id.tvUserName_2).text = "$FirstName $LastName"
                try {
                    Glide.with(context).load(ImageURL).placeholder(R.drawable.ic_baseline_account_circle)
                        .into(itemView.findViewById<CircleImageView>(R.id.circleivUserPhoto_2))
                } catch (e: Exception) {
                    itemView.findViewById<CircleImageView>(R.id.circleivUserPhoto_2)
                        .setImageResource(R.drawable.ic_baseline_account_circle)
                }
            }

            //
            itemView.findViewById<Button>(R.id.btnComment_2).setOnClickListener {
                itemClick(post)
            }

            //Delete
            itemView.findViewById<ImageButton>(R.id.tvCancel).setOnClickListener {
                val spePostref = FirebaseFirestore.getInstance().collection("SpecialistPosts")
                posts as MutableList<SpecialistPost>
                spePostref.document(post.id.toString()).delete()
                posts.removeAt(adapterPosition)
                notifyDataSetChanged()
            }

            //Date
            itemView.findViewById<TextView>(R.id.tvTimeAgo_2).text = DateUtils.getRelativeTimeSpanString(post.Date)
            if (post.ImageURL != null) {
                Glide.with(context).load(post.ImageURL)
                    .into(itemView.findViewById<ImageView>(R.id.imvPostPhoto_2))

                itemView.findViewById<ImageView>(R.id.container_2)
                    .load(post.ImageURL){
                        crossfade(true)
                        transformations(BlurTransformation(context))
                    }

                itemView.findViewById<ImageView>(R.id.imvPostPhoto_2).visibility = View.VISIBLE
                itemView.findViewById<ImageView>(R.id.container_2).visibility = View.VISIBLE
            } else {
                itemView.findViewById<ImageView>(R.id.imvPostPhoto_2).visibility = View.GONE
                itemView.findViewById<ImageView>(R.id.container_2).visibility = View.GONE
            }
            itemView.findViewById<TextView>(R.id.tvContent).text = post.Text

            if (itemView.findViewById<TextView>(R.id.tvContent).text.length > 200) {
                itemView.findViewById<ImageButton>(R.id.btnDropDown_2).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.tvContent).maxLines = 5
                itemView.findViewById<ImageButton>(R.id.btnDropDown_2)
                    .setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            } else {
                itemView.findViewById<ImageButton>(R.id.btnDropDown_2).visibility = View.GONE
            }

            itemView.findViewById<ImageButton>(R.id.btnDropDown_2).setOnClickListener {
                if (itemView.findViewById<TextView>(R.id.tvContent).maxLines == 5) {
                    itemView.findViewById<TextView>(R.id.tvContent).maxHeight =
                        DEFAULT_BUFFER_SIZE
                    itemView.findViewById<ImageButton>(R.id.btnDropDown_2)
                        .setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                } else if (itemView.findViewById<TextView>(R.id.tvContent).lineCount > 5) {
                    itemView.findViewById<TextView>(R.id.tvContent).maxLines = 5
                    itemView.findViewById<ImageButton>(R.id.btnDropDown_2)
                        .setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                }
            }
        }


    }

}