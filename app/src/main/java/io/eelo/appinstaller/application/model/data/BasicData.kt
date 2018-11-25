package io.eelo.appinstaller.application.model.data;

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.utils.Constants.BASE_URL
import io.eelo.appinstaller.utils.Execute
import io.eelo.appinstaller.utils.ImagesLoader
import java.net.URL

class BasicData @JsonCreator
constructor(@param:JsonProperty("package_name") val packageName: String,
            @param:JsonProperty("_id") val id: String,
            @param:JsonProperty("name") val name: String,
            @param:JsonProperty("textScore") val score: Float,
            @param:JsonProperty("last_modified") val lastModified: String,
            @param:JsonProperty("latest_version") val lastVersion: String,
            @param:JsonProperty("latest_version_number") var lastVersionNumber_a: String?,
            @param:JsonProperty("author") val author: String,
            @param:JsonProperty("icon_image_path") private val iconUri: String,
            @param:JsonProperty("other_images_path") val imagesUri: Array<String>) {

    private var icon: Bitmap? = null
    private var images: List<Bitmap>? = null
    var lastVersionNumber = ""

    fun loadImagesAsyncly(getter: (List<Bitmap>) -> Unit) {
        if (images == null) {
            Execute({
                loadImagesSynced()
            }, {
                getter.invoke(images!!)
            })
        } else {
            getter.invoke(images!!)
        }
    }

    @Synchronized
    private fun loadImagesSynced() {
        if (images == null) {
            images = ImagesLoader(imagesUri).loadImages()
        }
    }

    fun loadIconAsync(getter: (Bitmap) -> Unit) {
        if (icon == null) {
            Execute({
                loadIconSynced()
            }, {
                icon?.let { getter.invoke(it) }
            })
        } else {
            getter.invoke(icon!!)
        }
    }

    @Synchronized
    private fun loadIconSynced() {
        if (icon == null) {
            val url = URL(BASE_URL + "media/" + iconUri)
            icon = BitmapFactory.decodeStream(url.openStream())
        }
    }

    fun updateLoadedImages(other: BasicData) {
        if (iconUri == other.iconUri) {
            icon = other.icon
        }
        if (imagesUri.contentEquals(other.imagesUri)) {
            images = other.images
        }
    }
}