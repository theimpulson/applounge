package foundation.e.apps.api.cleanapk.data.search

data class App(
    val _id: String,
    val author: String,
    val category: String,
    val exodus_score: Int,
    val icon_image_path: String,
    val last_modified: String,
    val latest_version_number: String,
    val name: String,
    val other_images_path: List<String>,
    val package_name: String,
    val ratings: Ratings
)