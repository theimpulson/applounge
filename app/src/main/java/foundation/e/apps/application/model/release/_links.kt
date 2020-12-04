package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName


data class _links (

		@SerializedName("self") val self : String,
		@SerializedName("edit_url") val edit_url : String
)