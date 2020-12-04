package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName


data class Author (

		@SerializedName("id") val id : Int=-1,
		@SerializedName("name") val name : String="",
		@SerializedName("username") val username : String="",
		@SerializedName("state") val state : String="",
		@SerializedName("avatar_url") val avatar_url : String="",
		@SerializedName("web_url") val web_url : String
)