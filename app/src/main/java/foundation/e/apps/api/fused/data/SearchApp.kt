package foundation.e.apps.api.fused.data

data class SearchApp(
    val _id: String,
    val author: String,
    val category: String,
    val exodus_score: Int,
    val icon_image_path: String,
    val name: String,
    val package_name: String,
    val ratings: Ratings,
    var origin: Origin?,
    val latest_version_code: Int,
    val offerType: Int?
)
