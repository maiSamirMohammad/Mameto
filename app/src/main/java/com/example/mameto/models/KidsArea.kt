package com.example.mameto.models


data class KidsArea(
    var Address: String= "",
    var ChildrenAgeRange: Map<String, Int> = mapOf("From" to 0, "To" to 0),
    var City: String= "",
    var Country: String= "",
    var Description: String= "",
    var KidsAreaId: String= "",
    var Location: Map<String, Int> = mapOf("Latitude" to 0, "Longitude" to 0),
    var Name: String= "",
    var OwnerFullName: String= "",
    var OwnerPhone: String= "",
    var PhoneNumber: String= "",
    var Photos:List<String> = listOf<String>(),
    var WorkingHours: Map<String, String> = mapOf("From" to "0AM", "To" to "0PM")
)
