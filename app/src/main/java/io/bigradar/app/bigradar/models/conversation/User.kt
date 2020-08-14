package io.bigradar.app.bigradar.models.conversation

val mColors = arrayOf(
    "607D8B",
    "6D4C41",
    "2E7D32",
    "AD1457",
    "283593",
    "827717",
    "6A1B9A",
    "BF360C",
    "347474",
    "35495e",
    "ee8572",
    "192965",
    "3282b8",
    "9d2503",
    "2c786c",
    "4a69bb"
)


data class User(
    val _id: String?,
    val name: String,
    val phone: String?,
    val email: String?,
    val avatar: String?,
    var status: Int?,
    val location: Location?,
    var lastSeen: String?,
    val fullname: String?,
    var isTyping: Boolean = false

) {
    val color: String
        get() {
            return '#' + mColors[_id?.last().toString().toInt(16)]
        }

    val shortName: String
        get() {
            return name.split(" ").take(2).map { a -> a[0].toUpperCase() }.joinToString("")
        }
}


