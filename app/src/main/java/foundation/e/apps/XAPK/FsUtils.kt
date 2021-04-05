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

package foundation.e.apps.XAPK

import android.os.Environment
import android.text.TextUtils
import java.io.File

object FsUtils {

    val isSdUsable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    fun getStorageDir(): File? {
        return if (isSdUsable) {
            Environment.getExternalStorageDirectory()
        } else {
            null
        }
    }

    fun exists(filePath: String?): Boolean {
        return !TextUtils.isEmpty(filePath) && exists(File(filePath))
    }

    fun exists(file: File?): Boolean {
        return file != null && file.exists()
    }

    fun deleteFileOrDir(filePath: String?) {
        filePath?.let {
            deleteFileOrDir(File(it))
        }
    }
    fun deleteFileOrDir(file: File?) {
        if (file != null && exists(file)) {
            if (file.isFile) {
                file.delete()
            } else if (file.isDirectory) {
                file.listFiles()?.forEach {
                    deleteFileOrDir(it)
                }
                file.delete()
            }
        }
    }

    fun createOnNotFound(folder: File?): File? {
        if (folder == null) {
            return null
        }
        if (!exists(folder)) {
            folder.mkdirs()
        }
        return if (exists(folder)) {
            folder
        } else {
            null
        }
    }
}
