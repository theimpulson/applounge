package foundation.e.apps.api.exodus

data class Trackers (
    val trackers: Map<String, Tracker>
)

data class Tracker (
    val id: Long,
    val name: String,
    val description: String,
    val creationDate: String,
    val codeSignature: String,
    val networkSignature: String,
    val website: String,
    val categories: List<Category>
)

enum class Category {
    Advertisement,
    Analytics,
    CrashReporting,
    Identification,
    Location,
    Profiling
}