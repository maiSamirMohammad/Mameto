package com.example.mameto

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.bumptech.glide.Glide
import com.example.mameto.models.Comment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import de.hdodenhof.circleimageview.CircleImageView

class SpeCommentsAdapter (val context: Context, val comments: List<Comment>): RecyclerView.Adapter<SpeCommentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.generated_comments_layout, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(comments[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        fun bind(comment: Comment) {
            //get Fname, Lname, proPic of owner
            val Authorid = comment.AuthorID.toString()
            val UserRef = FirebaseFirestore.getInstance().collection("Users")
            UserRef.document(Authorid).addSnapshotListener { value, error ->
                val FirstName = "${value?.get("FirstName")}"
                val LastName = "${value?.get("LastName")}"
                val ImageURL = "${value?.get("ImageURL")}"
                itemView.findViewById<TextView>(R.id.tvUserNameCommentsLayout).text =
                    "$FirstName $LastName"
                try {
                    Glide.with(context).load(ImageURL)
                        .placeholder(R.drawable.ic_baseline_account_circle)
                        .into(itemView.findViewById<CircleImageView>(R.id.circle_userPhoto_comments_layout))
                } catch (e: Exception) {
                    itemView.findViewById<CircleImageView>(R.id.circle_userPhoto_comments_layout)
                        .setImageResource(R.drawable.ic_baseline_account_circle)
                }
            }

            //Date of Comment
            itemView.findViewById<TextView>(R.id.tvTimesAgoCommentsLayout).text = DateUtils.getRelativeTimeSpanString(comment.Date)
            //Text -> CommentContent
            itemView.findViewById<TextView>(R.id.tvCommentsContent).text = comment.Text
            if (itemView.findViewById<TextView>(R.id.tvCommentsContent).text.length > 150) {
                itemView.findViewById<ImageButton>(R.id.btnDropDown_commentActivity).visibility =
                    View.VISIBLE
                itemView.findViewById<TextView>(R.id.tvCommentsContent).maxLines = 4
                itemView.findViewById<ImageButton>(R.id.btnDropDown_commentActivity)
                    .setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
            } else {
                itemView.findViewById<ImageButton>(R.id.btnDropDown_commentActivity).visibility =
                    View.GONE
            }

            itemView.findViewById<ImageButton>(R.id.btnDropDown_commentActivity)
                .setOnClickListener {
                    if (itemView.findViewById<TextView>(R.id.tvCommentsContent).maxLines == 4) {
                        itemView.findViewById<TextView>(R.id.tvCommentsContent).maxHeight =
                            DEFAULT_BUFFER_SIZE
                        itemView.findViewById<ImageButton>(R.id.btnDropDown_commentActivity)
                            .setImageResource(R.drawable.ic_baseline_arrow_drop_up_24)
                    } else if (itemView.findViewById<TextView>(R.id.tvCommentsContent).lineCount > 4) {
                        itemView.findViewById<TextView>(R.id.tvCommentsContent).maxLines = 4
                        itemView.findViewById<ImageButton>(R.id.btnDropDown_commentActivity)
                            .setImageResource(R.drawable.ic_baseline_arrow_drop_down_24)
                    }
                }

            itemView.setOnLongClickListener {
                popupMenus(it, comment.id.toString(), comment.PostID.toString())
                true
            }

            val UserRefrence = FirebaseFirestore.getInstance().collection("Users")
            UserRefrence.document(comment.AuthorID.toString()).addSnapshotListener { snap, ex ->
                val IsSpecialist = snap?.get("IsSpecialist")
                if (IsSpecialist == true) {
                    itemView.findViewById<ImageView>(R.id.imVerifiedComments).visibility = View.VISIBLE
                } else {
                    itemView.findViewById<ImageView>(R.id.imVerifiedComments).visibility = View.GONE
                }
            }

        }
        private fun popupMenus(view: View, commentID: String, postID: String) {
            val dia = BottomSheetDialog(context)
            dia.setContentView(R.layout.bottom_sheet_dialog)

            val signedInUser = Firebase.auth.currentUser?.uid
            val SpecommentRef = FirebaseFirestore.getInstance().collection("SpecialistComments")
            val SpepostRef = FirebaseFirestore.getInstance().collection("SpecialistPosts")
            SpecommentRef.document(commentID).addSnapshotListener { value, error ->
                val authorID = "${value?.get("AuthorID")}"
                if (signedInUser != authorID){
                    dia.findViewById<Button>(R.id.btnDelete)?.isVisible = false
                    dia.findViewById<Button>(R.id.btnReport)?.isVisible = true
                }else if(signedInUser == authorID){
                    dia.findViewById<Button>(R.id.btnDelete)?.isVisible = true
                    dia.findViewById<Button>(R.id.btnReport)?.isVisible = false
                }
            }
            dia.show()

            dia.findViewById<Button>(R.id.btnDelete)?.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Delete")
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setMessage("Are you sure you want to delete this Comment?")
                    .setPositiveButton("Yes"){
                            dialog,_->
                        comments as MutableList<Comment>
                        SpecommentRef.document(commentID).delete()
                        SpepostRef.document(postID).update("Comments", FieldValue.arrayRemove(commentID))
                        comments.removeAt(adapterPosition)
                        notifyDataSetChanged()
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel"){
                            dialog,_->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                dia.dismiss()
            }
            dia.findViewById<Button>(R.id.btnReport)?.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Report")
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setMessage("Are you sure you want to report to this Comment?")
                    .setPositiveButton("Yes") { dialog, _ ->

                        val dialogWait = MaterialDialog(context)
                            .noAutoDismiss()
                            .customView(R.layout.loader_dialog)
                        dialogWait.findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
                        dialogWait.show()

                        val specialistCommentsReportsRef =
                            Firebase.firestore.collection("SpecialistsCommentsReports")
                        val autoGeneratedReportID = specialistCommentsReportsRef.document().id
                        val reporterID: Any?
                        if (Firebase.auth.currentUser != null) {
                            reporterID = Firebase.auth.currentUser!!.uid
                            val reportData = hashMapOf(
                                "CommentID" to commentID,
                                "Timestamp" to System.currentTimeMillis(),
                                "Reporter id" to reporterID
                            )
                            specialistCommentsReportsRef.document(autoGeneratedReportID)
                                .set(reportData)
                                .addOnCompleteListener {
                                    dialogWait.findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                                    dialogWait.dismiss()
                                    //AlertDialog.Builder(context)
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
                        } else if (Firebase.auth.currentUser == null) {
                            reporterID = null
                            val reportData = hashMapOf(
                                "CommentID" to commentID,
                                "Timestamp" to System.currentTimeMillis(),
                                "Reporter id" to reporterID
                            )
                            specialistCommentsReportsRef.document(autoGeneratedReportID)
                                .set(reportData)
                                .addOnCompleteListener {
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
                    .setNegativeButton("Cancel"){
                            dialog,_->
                        dialog.dismiss()
                    }
                    .create()
                    .show()
                dia.dismiss()
            }

        }
    }

    override fun getItemCount(): Int {
        return comments.size
    }


}