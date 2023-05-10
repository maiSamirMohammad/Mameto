package com.example.mameto.models

data class Comment(var AuthorID: String? = null,
                   var Date: Long = 0,
                   var PostID: String? = null,
                   var Text: String? = null,
                   var id: String? = null) {
    constructor(Date: Long, Text: String?) : this()
}