/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.pwa

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