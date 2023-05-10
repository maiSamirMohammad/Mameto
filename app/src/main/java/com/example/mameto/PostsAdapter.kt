package com.example.mameto

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.text.format.DateUtils
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import coil.api.load
import coil.transform.BlurTransformation
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.example.mameto.Community_posts_Activity
import com.example.mameto.CommunityFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.internal.cache.DiskLruCache
private var Authorid: String? = null

class PostsAdapter(val context: Context, val posts: List<Post>, val itemClick: (Post) -> Unit): RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.item_list, parent, false)
        return ViewHolder(v, itemClick)

    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(posts[position])
    }


    inner class ViewHolder(itemView: View, val itemClick: (Post) -> Unit): RecyclerView.ViewHolder(itemView) {

        fun bind(post: Post) {
            val AuthorID = post.AuthorID.toString()
            val ref = FirebaseFirestore.getInstance().collection("Users")
            ref.document(AuthorID).addSnapshotListener { value, error ->
                Authorid = AuthorID
                val FirstName = "${value?.get("FirstName")}"
                val LastName = "${value?.get("LastName")}"
                val ImageURL = "${value?.get("ImageURL")}"


                itemView.findViewById<TextView>(R.id.tvUserName).text = "$FirstName $LastName"
                try {
                    Glide.with(context).load(ImageURL)
                        .placeholder(R.drawable.ic_baseline_account_circle)
                        .into(itemView.findViewById<CircleImageView>(R.id.circleivUserPhoto))
                } catch (e: Exception) {
                    itemView.findViewById<CircleImageView>(R.id.circleivUserPhoto)
                        .setImageResource(R.drawable.ic_baseline_account_circle)
                }
            }

            //code of tvPostOptions
            itemView.findViewById<TextView>(R.id.tvPostOptions).setOnClickListener {
                popupMenus(it, post.id.toString())
            }

            itemView.findViewById<TextView>(R.id.tvTimeAgo).text =
                DateUtils.getRelativeTimeSpanString(post.Date)
            if (post.ImageURL != null) {
                Glide.with(context).load(post.ImageURL)
                    .into(itemView.findViewById<ImageView>(R.id.imvPostPhoto))

                itemView.findViewById<ImageView>(R.id.container)
                    .load(post.ImageURL) {
                        crossfade(true)
                        transformations(BlurTransformation(context))
                    }

                itemView.findViewById<ImageView>(R.id.imvPostPhoto).visibility = View.VISIBLE
                itemView.findViewById<ImageView>(R.id.container).visibility = View.VISIBLE
            } else {
                itemView.findViewById<ImageView>(R.id.imvPostPhoto).visibility = View.GONE
                itemView.findViewById<ImageView>(R.id.container).visibility = View.GONE
            }
            itemView.findViewById<TextView>(R.id.expandable_text_view).text = post.Text

            if (itemView.findViewById<TextView>(R.id.expandable_text_view).text.length > 200) {
                itemView.findViewById<ImageButton>(R.id.btnDropDown).visibility = View.VISIBLE
                itemView.findViewById<TextView>(R.id.expandable_text_view).maxLines = 5
                itemView.findViewById<ImageButton>(R.id.btnDropDown)
                    .setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            } else {
                itemView.findViewById<ImageButton>(R.id.btnDropDown).visibility = View.GONE
            }

            itemView.findViewById<ImageButton>(R.id.btnDropDown).setOnClickListener {
                if (itemView.findViewById<TextView>(R.id.expandable_text_view).maxLines == 5) {
                    itemView.findViewById<TextView>(R.id.expandable_text_view).maxHeight =
                        DEFAULT_BUFFER_SIZE
                    itemView.findViewById<ImageButton>(R.id.btnDropDown)
                        .setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                } else if (itemView.findViewById<TextView>(R.id.expandable_text_view).lineCount > 5) {
                    itemView.findViewById<TextView>(R.id.expandable_text_view).maxLines = 5
                    itemView.findViewById<ImageButton>(R.id.btnDropDown)
                        .setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                }
            }

            //get commentsCount
            val refPosts = FirebaseFirestore.getInstance().collection("Posts")
            refPosts.document(post.id.toString()).addSnapshotListener { value, error ->
                if (value?.get("Comments") != null){
                    val tt = value.get("Comments") as List<*>
                    val numComments = tt.size.toString()
                    if (numComments == "1" ){
                        itemView.findViewById<Button>(R.id.btnCOMMENT).text = "$numComments comment"
                    }else if (numComments == "0"){
                        refPosts.document(post.id.toString()).update("Comments", FieldValue.delete())
                    }
                    else{
                        itemView.findViewById<Button>(R.id.btnCOMMENT).text = "$numComments comments"
                    }
                }else{
                    //refPosts.document(post.id.toString()).update("Comments", FieldValue.arrayRemove())
                    itemView.findViewById<Button>(R.id.btnCOMMENT).text = "0 comments"
                }
            }


            itemView.findViewById<Button>(R.id.btnCOMMENT).setOnClickListener {
                itemClick(post)
            }

            val UserRef = FirebaseFirestore.getInstance().collection("Users")
            UserRef.document(post.AuthorID.toString()).addSnapshotListener { snap, ex ->
                val IsSpecialist = snap?.get("IsSpecialist")
                if (IsSpecialist == true) {
                    itemView.findViewById<ImageView>(R.id.imVerified).visibility = View.VISIBLE
                } else {
                    itemView.findViewById<ImageView>(R.id.imVerified).visibility = View.GONE
                }
            }
        }

        private fun popupMenus(view: View, documentNamedbyPostID: String) {
            //val position = posts[adapterPosition]
            val popupMenus = PopupMenu(context.applicationContext, view)
            popupMenus.inflate(R.menu.menu_post)

            val signedInUser = Firebase.auth.currentUser?.uid
            val Postref = FirebaseFirestore.getInstance().collection("Posts")

            Postref.document(documentNamedbyPostID).addSnapshotListener { value, error ->
                val authorID = "${value?.get("AuthorID")}"
                if (signedInUser != authorID){
                    popupMenus.menu.findItem(R.id.itemDelete).isVisible = false
                    popupMenus.menu.findItem(R.id.itemReport).isVisible = true
                }else if(signedInUser == authorID){
                    popupMenus.menu.findItem(R.id.itemDelete).isVisible = true
                    popupMenus.menu.findItem(R.id.itemReport).isVisible = false
                }
            }

            popupMenus.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.itemDelete -> {
                        AlertDialog.Builder(context)
                            .setTitle("Delete")
                            .setIcon(R.drawable.ic_baseline_warning_24)
                            .setMessage("Are you sure you want to delete this Post?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                posts as MutableList<Post>
                                Postref.document(documentNamedbyPostID)
                                    .addSnapshotListener { value, error ->
                                        if (value?.get("Comments") != null) {
                                            val comments = value.get("Comments") as List<*>
                                            val comReff = FirebaseFirestore.getInstance()
                                                .collection("Comments")
                                            for (i in comments) {
                                                comReff.document(i.toString()).delete()
                                            }
                                        }
                                    }
                                Postref.document(documentNamedbyPostID).delete()
                                posts.removeAt(adapterPosition)
                                notifyDataSetChanged()
                                dialog.dismiss()
                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    R.id.itemReport-> {
                        AlertDialog.Builder(context)
                            .setTitle("Report")
                            .setIcon(R.drawable.ic_baseline_warning_24)
                            .setMessage("Are you sure you want to report this Post?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                val dialogWait = MaterialDialog(context)
                                    .noAutoDismiss()
                                    .customView(R.layout.loader_dialog)
                                dialogWait.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                                dialogWait.show()

                                val communityPostsReportsRef = Firebase.firestore.collection("CommunityPostsReports")
                                val autoGeneratedReportID = communityPostsReportsRef.document().id
                                val reporterID : Any?
                                if (Firebase.auth.currentUser != null){
                                    reporterID = Firebase.auth.currentUser!!.uid
                                    val reportData = hashMapOf(
                                        "PostID" to  documentNamedbyPostID,
                                        "Timestamp"     to  System.currentTimeMillis(),
                                        "Reporter id" to reporterID
                                    )
                                    communityPostsReportsRef.document(autoGeneratedReportID).set(reportData).addOnCompleteListener {
                                        dialogWait.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                                        dialogWait.dismiss()

                                        AlertDialog.Builder(context)
                                            .setTitle("Done")
                                            .setIcon(R.drawable.ic_baseline_done_outline_24)
                                            .setMessage("Your Report has been submitted and an admin will review it.")
                                            .setPositiveButton("Ok") { dialog_2, _ ->
                                                dialog_2.dismiss()
                                            }
                                            .create()
                                            .show()
                                    }
                                    dialog.dismiss()
                                }else if (Firebase.auth.currentUser == null){
                                    reporterID = null
                                    val reportData = hashMapOf(
                                        "PostID" to  documentNamedbyPostID,
                                        "Timestamp"     to  System.currentTimeMillis(),
                                        "Reporter id" to reporterID
                                    )
                                    communityPostsReportsRef.document(autoGeneratedReportID).set(reportData).addOnCompleteListener {
                                        dialogWait.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                                        dialogWait.dismiss()

                                        AlertDialog.Builder(context)
                                            .setTitle("Done")
                                            .setIcon(R.drawable.ic_baseline_done_outline_24)
                                            .setMessage("Your Report has been submitted and an admin will review it.")
                                            .setPositiveButton("Ok") { dialog_2, _ ->
                                                dialog_2.dismiss()
                                            }
                                            .create()
                                            .show()
                                    }
                                    dialog.dismiss()
                                }

                            }
                            .setNegativeButton("Cancel") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                        true
                    }
                    else-> true
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                popupMenus.gravity = Gravity.END
            }
            popupMenus.show()
            val popup = PopupMenu::class.java.getDeclaredField("mPopup")
            popup.isAccessible = true
            val menu = popup.get(popupMenus)
            menu.javaClass.getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(menu, true)
        }
    }

    override fun getItemCount(): Int {
        return posts.size
    }

}
