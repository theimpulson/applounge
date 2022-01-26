package foundation.e.apps.api.exodus

data class AppTrackerResponse (val appTrackerResponse: Map<String, TrackerInfo> = mapOf())

data class TrackerInfo (
    val name: String,
    val creator: String,
    val reports: List<Report>
)

data class Report (
    val id: Long,
    val creationDate: String,
    val updatedAt: String,
    val version: String,
    val versionCode: String,
    val trackers: List<Long>
)