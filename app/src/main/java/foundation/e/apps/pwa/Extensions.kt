package foundation.e.apps.pwa

import android.R.attr.bitmap
import android.graphics.Bitmap
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.IOException


fun Bitmap.toByteArray(): ByteArray? {
    // Try go guesstimate how much space the icon will take when serialized
    // to avoid unnecessary allocations/copies during the write.
    val size: Int = this.width * this.height * 4
    val out = ByteArrayOutputStream(size)
    return try {
        this.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
        out.toByteArray()
    } catch (e: IOException) {
        Log.w("Bitmap", "Could not write bitmap")
        null
    }
}