package io.bigradar.app.bigradar.models.conversation

data class Location(
    val city: String,
    val country: String,
    val isp: String,
    val lat: Double,
    val lon: Double,
    val pincode: String,
    val region: String
)