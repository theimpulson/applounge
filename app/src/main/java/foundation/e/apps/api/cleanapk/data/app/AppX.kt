package foundation.e.apps.api.cleanapk.data.app

data class AppX(
    val _id: String,
    val add_min_sdk: Boolean,
    val author: String,
    val category: String,
    val description: String,
    val exodus_perms: List<Any>,
    val exodus_tracker: List<Any>,
    val icon_image_path: String,
    val last_modified: String,
    val latest_version_number: String,
    val licence: String,
    val name: String,
    val number_of_downloads: Int,
    val object_is_valid: Boolean,
    val other_images_path: List<String>,
    val package_name: String,
    val ratings: Ratings,
    val user_ratings: Double,
)