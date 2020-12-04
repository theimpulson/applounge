package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName
import foundation.e.apps.application.model.release.Sources

data class Assets (

        @SerializedName("count") val count : Int,
        @SerializedName("sources") val sources : List<Sources>,
        @SerializedName("links") val links : List<Links>,
        @SerializedName("evidence_file_path") val evidence_file_path : String
)