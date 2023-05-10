package com.example.mameto.models

import com.google.firebase.firestore.FieldValue

data class SpecialistPost(var AuthorID: String? = null,
                          var Date: Long = 0,
                          var ImageURL: String? = null,
                          var Text: String? = null,
                          var id: String? = null,
                          var isPending: Boolean = true) {
}