package foundation.e.apps.application.model.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Application
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import foundation.e.apps.utils.ImagesLoader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class PwasBasicData@JsonCreator
constructor(@param:JsonProperty("_id") val id: String,
            @param:JsonProperty("name") val name: String,
            @param:JsonProperty("description") val description: String?,
            @param:JsonProperty("is_pwa") val is_pwa: Boolean,
            @param:JsonProperty("is_web_app") val is_web_app: Boolean,
            @param:JsonProperty("has_https") val has_https: Boolean,
            @param:JsonProperty("url") val url: String?,
            @param:JsonProperty("category") val category: String,
            @param:JsonProperty("icon_image_path") val icon_uri: String,
            @param:JsonProperty("other_images_path") val imagesUri: Array<String>,
            @param:JsonProperty("created_on") val created_on: String?){


    init{
        thisActivity = this
    }

    companion object {

         var thisActivity: PwasBasicData?= null

    }

    private var icon: Bitmap? = null
    
    private var images: List<Bitmap>? = null

    var uri = icon_uri


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


    fun updateLoadedImages(other: PwasBasicData) {
        if (icon_uri == other.icon_uri) {
            icon = other.icon
        }
        if (imagesUri.contentEquals(other.imagesUri)) {
            images = other.images
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
    internal fun loadIconSynced(): Error? {
        if (icon == null) {
            try {
                val url = URL(Constants.BASE_URL + "media/" + icon_uri)
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = Constants.REQUEST_METHOD_GET
                urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
                urlConnection.readTimeout = Constants.READ_TIMEOUT
                icon = BitmapFactory.decodeStream(urlConnection.inputStream)
                urlConnection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                return Error.UNKNOWN
            }
        }
        return null
    }
    interface IconLoaderCallback {
        fun onIconLoaded(application: Application, bitmap: Bitmap)
    }

}




