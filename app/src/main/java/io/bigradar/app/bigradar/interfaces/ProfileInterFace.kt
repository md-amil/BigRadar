package io.bigradar.app.bigradar.interfaces

import android.text.Editable
import io.bigradar.app.bigradar.models.Field

interface ProfileInterFace {
    fun onclick(field: Field, value: String, position:Int,editable:Boolean)
}