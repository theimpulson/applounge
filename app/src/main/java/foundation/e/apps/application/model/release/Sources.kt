package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName


data class Sources (

	@SerializedName("format") val format : String,
	@SerializedName("url") val url : String
)