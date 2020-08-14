package io.bigradar.app.bigradar.models.conversation

data class Element (
    var filters: ArrayList<Filter>,
    var _id: String,
    var name: String,
    var match: String
)

data class Filter (
    val condition: String,
    val field: String,
    val value: String
)

