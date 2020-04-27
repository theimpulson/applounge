package foundation.e.apps.XAPK

import com.google.gson.GsonBuilder
import java.io.Reader


object JsonUtils {
    val gson by lazy { GsonBuilder().excludeFieldsWithoutExposeAnnotation().create() }



    fun <T> objectFromJson(json: Reader, classOfT: Class<T>): T? {
        return try {
            gson.fromJson(json, classOfT)
        } catch (e: Exception) {
            null
        }

    }








}

