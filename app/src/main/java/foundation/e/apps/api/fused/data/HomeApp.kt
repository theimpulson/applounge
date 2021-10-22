package foundation.e.apps.api.fused.data

data class HomeApp(
    val _id: String,
    val icon_image_path: String,
    val name: String,
    val other_images_path: List<String>
)
