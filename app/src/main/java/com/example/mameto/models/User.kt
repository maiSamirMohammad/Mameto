package com.example.mameto

data class User (var Email: String? = null,
                 var FirstName: String? = null,
                 var IsSpecialist:Boolean?=false,
                 var LastName: String? = null,
                 var id: String? = null,
                 var ImageURL:String? = null){
    constructor(Email: String?, FirstName: String?, LastName: String?, id: String?) : this()
}