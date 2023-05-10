package com.example.mameto

import android.net.Uri
import java.util.*

data class Post(var AuthorID: String? = null,
                var Date: Long = 0,
                var ImageURL: String? = null,
                var Text: String? = null,
                var id: String? = null
                // var Comments: ArrayTransformOperation.Union?  = null
) {
    constructor(AuthorID: String?, Date: Long, Text: String?, id: String?) : this()
    //constructor(AuthorID: String?, Date: Long, Text: String?, id: String?, Comments: ArrayTransformOperation.Union?) : this()

}

