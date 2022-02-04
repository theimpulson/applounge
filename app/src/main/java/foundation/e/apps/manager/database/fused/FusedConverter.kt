package foundation.e.apps.manager.database.fused

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FusedConverter {

    @TypeConverter
    fun listToJsonString(value: List<String>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonStringToList(value: String) =
        Gson().fromJson(value, Array<String>::class.java).toMutableList()

    @TypeConverter
    fun listToJsonLong(value: MutableMap<Long, Boolean>): String = Gson().toJson(value)

    @TypeConverter
    fun jsonLongToList(value: String): MutableMap<Long, Boolean> =
        Gson().fromJson(value, object : TypeToken<MutableMap<Long, Boolean>>() {}.type)
}
