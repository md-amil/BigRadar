package io.bigradar.app.bigradar.models

data class Field(
    val type:String?,
    val sort: Boolean?,
    val edit: Boolean,
    val quick: Boolean?,
    val _id: String?,
    val fixed: Boolean?,
    val name: String,
    val label: String?,
    val project_id: String?,
    val __v: Int?
)