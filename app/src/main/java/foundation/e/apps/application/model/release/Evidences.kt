package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName


data class Evidences (

		@SerializedName("sha") val sha : String,
		@SerializedName("filepath") val filepath : String,
		@SerializedName("collected_at") val collected_at : String
)