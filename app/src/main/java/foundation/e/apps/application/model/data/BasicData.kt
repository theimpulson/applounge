package foundation.e.apps.application.model.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Application
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.BASE_URL
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import foundation.e.apps.utils.ImagesLoader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class BasicData @JsonCreator
constructor(@param:JsonProperty("package_name") val packageName: String,
            @param:JsonProperty("_id") val id: String,
            @param:JsonProperty("name") val name: String,
            @param:JsonProperty("search_score") val score: Float,
            @param:JsonProperty("last_modified") val lastModified: String,
            @param:JsonProperty("latest_version") val lastVersion: String,
            @param:JsonProperty("latest_version_number") var lastVersionNumber: String?,
            @param:JsonProperty("latest_downloaded_version") val latestDownloadableUpdate: String,
            @param:JsonProperty("author") val author: String,
            @param:JsonProperty("icon_image_path") private val iconUri: String,
            @param:JsonProperty("other_images_path") val imagesUri: Array<String>,
            @param:JsonProperty("exodus_score") val privacyRating: Float?,
            @param:JsonProperty("ratings") val ratings: Ratings,
            @param:JsonProperty("category") val category: String) {

    private var icon: Bitmap? = null
    private var images: List<Bitmap>? = null

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

    fun loadIconAsync(application: Application, iconLoaderCallback: IconLoaderCallback) {
        if (icon == null) {
            var error: Error? = null
            Execute({
                error = loadIconSynced()
            }, {
                if (error == null) {
                    icon?.let {
                        iconLoaderCallback.onIconLoaded(application, it)
                    }
                }
            })
        } else {
            iconLoaderCallback.onIconLoaded(application, icon!!)
        }
    }

    @Synchronized
    private fun loadIconSynced(): foundation.e.apps.utils.Error? {
        if (icon == null) {
            try {
                val url = URL(BASE_URL + "media/" + iconUri)
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = Constants.REQUEST_METHOD
                urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
                urlConnection.readTimeout = Constants.READ_TIMEOUT
                icon = BitmapFactory.decodeStream(urlConnection.inputStream)
                urlConnection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                return foundation.e.apps.utils.Error.UNKNOWN
            }
        }
        return null
    }

    fun updateLoadedImages(other: BasicData) {
        if (iconUri == other.iconUri) {
            icon = other.icon
        }
        if (imagesUri.contentEquals(other.imagesUri)) {
            images = other.images
        }
    }

    class Ratings(@param:JsonProperty("usageQualityScore") val rating: Float,
                  @param:JsonProperty("privacyScore") val privacyRating: Float)

    interface IconLoaderCallback {
        fun onIconLoaded(application: Application, bitmap: Bitmap)
    }
}