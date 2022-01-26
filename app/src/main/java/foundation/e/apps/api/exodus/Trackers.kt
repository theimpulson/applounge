package foundation.e.apps.api.exodus

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters

data class Trackers (
    val trackers: Map<String, Tracker>
)

@Entity
data class Tracker (
    @PrimaryKey
    val id: Long,
    val name: String,
    val description: String?,
    val creationDate: String?,
    val codeSignature: String?,
    val networkSignature: String?,
    val website: String?,
//    val categories: List<Category>?
)

enum class Category {
    Advertisement,
    Analytics,
    CrashReporting,
    Identification,
    Location,
    Profiling,
    None
}

class CategoryConverter {
    @TypeConverter
    fun fromStringToConverter(categoryString: String): Category? {
        return enumValueOf<Category>(categoryString)
    }

    @TypeConverter
    fun fromCategoryToString(category: Category): String? {
        return category.name
    }
}