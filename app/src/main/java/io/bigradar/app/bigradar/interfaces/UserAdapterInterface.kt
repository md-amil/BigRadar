package io.bigradar.app.bigradar.interfaces

import io.bigradar.app.bigradar.models.conversation.Filter

interface UserAdapterInterface {
    fun onClick(uid:String,name:String)
    fun onClose(filter:Filter)
}