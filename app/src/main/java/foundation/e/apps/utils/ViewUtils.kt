package foundation.e.apps.utils

import android.content.res.Resources

fun Int.toDP(): Int {
    return (this / Resources.getSystem().displayMetrics.density).toInt()
}

fun Int.toPX(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}
