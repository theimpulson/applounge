package foundation.e.apps.api.cleanapk.data.search

import foundation.e.apps.api.data.Origin

data class CleanAPKSearchApp(
    val _id: String,
    val author: String,
    val category: String,
    val exodus_score: Int,
    val icon_image_path: String,
    val name: String,
    val package_name: String,
    val ratings: Ratings,
    val origin: Origin = Origin.CLEANAPK
)