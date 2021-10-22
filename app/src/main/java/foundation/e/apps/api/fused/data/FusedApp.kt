package foundation.e.apps.api.fused.data

data class FusedApp(
    val _id: String,
    val author: String,
    val category: String,
    val description: String,
    val exodus_perms: List<Any>,
    val exodus_tracker: List<Any>,
    val icon_image_path: String,
    val last_modified: String,
    val latest_version_code: Int,
    val latest_version_number: String,
    val licence: String,
    val name: String,
    val other_images_path: List<String>,
    val package_name: String,
    val ratings: Ratings,
)
