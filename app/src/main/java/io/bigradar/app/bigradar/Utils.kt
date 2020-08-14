package io.bigradar.app.bigradar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Patterns
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar


private const val SECOND_MILLIS = 1000
private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
private const val DAY_MILLIS = 24 * HOUR_MILLIS
@Suppress("DEPRECATION")




fun isNetworkConnected(context: Context): Boolean
{
    val cm = context.getSystemService(
        Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    return cm.activeNetworkInfo != null
}


fun errorMessage(view: View, message: String, context: Context, responseCode: Int)
{
    if (responseCode == 500) {
        showMessage("Server Error, Please Try Again", view, context)
    } else {
        showMessage(message, view, context)
    }
}


private fun showMessage(message: String, view: View, context: Context)
{
    val snackBar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
    snackBar.view.setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
    snackBar.show()
}


fun changeBackground(context: Context,view: TextView, color:String) {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val shape= GradientDrawable()
    shape.shape = GradientDrawable.OVAL
    shape.setColor(Color.parseColor(color))
    shape.cornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,  50f, metrics)
    view.background = shape
}


fun isValidEmail(target: CharSequence): Boolean {
    return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
}


fun toast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun openKeyInput(editText: EditText, context: Context) {
    editText.requestFocus()
    val imm: InputMethodManager? =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
}

fun camelCase(name: String): String {
    return name
}

//fun urlToBitmap(url:String,context:Context):Bitmap?{
//    var theBitmap: Bitmap? = null
//    Glide.with(context)
//        .load("Your URL")
//        .asBitmap()
//        .into(object : SimpleTarget<Bitmap?>() {
//            fun onResourceReady(
//                res: Bitmap?,
//                animation: GlideAnimation<in Bitmap?>?
//            ) {
//                // assign res(Bitmap object) to your local theBitmap(Bitmap object)
//                theBitmap = res
//            }
//        })
//    return theBitmap
//}


fun drawableToBitmap(drawable: Drawable): Bitmap? {
    var bitmap: Bitmap? = null
    if (drawable is BitmapDrawable) {
        val bitmapDrawable = drawable
        if (bitmapDrawable.bitmap != null) {
            return bitmapDrawable.bitmap
        }
    }
    bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
        Bitmap.createBitmap(
            1,
            1,
            Bitmap.Config.ARGB_8888
        ) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
    }
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

