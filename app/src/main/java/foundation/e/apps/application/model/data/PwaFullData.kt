package foundation.e.apps.application.model.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.categories.model.Category

class PwaFullData @JsonCreator
constructor(
        @JsonProperty("_id")val id: String,
        @JsonProperty("name")val name: String,
        @param:JsonProperty("description") val description: String,
        @param:JsonProperty("is_pwa") val is_pwa: Boolean,
        @param:JsonProperty("is_web_app") val is_web_app: Boolean,
        @param:JsonProperty("has_https") val has_https: Boolean,
        @param:JsonProperty("url") val url: String,
        @JsonProperty("category") categoryId: String,
        @param:JsonProperty("icon_image_path") val icon_uri: String,
        @param:JsonProperty("other_images_path") val imagesUri: Array<String>,
        @param:JsonProperty("created_on") val created_on: String){



      var pwabasicdata =PwasBasicData( id, name, description,is_pwa,is_web_app, has_https, url,categoryId,icon_uri,imagesUri,created_on)

      val category: Category

      init {
              this.category = Category(categoryId, "")
      }

}


