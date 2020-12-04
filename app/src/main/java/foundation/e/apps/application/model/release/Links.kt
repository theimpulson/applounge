package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName

data class Links(
        @field:SerializedName("id") val id: Int,
        @field:SerializedName("name") val name: String,
        @field:SerializedName("url") val url: String,
        @field:SerializedName("direct_asset_url") val direct_asset_url: String,
        @field:SerializedName("external") val external: String
)