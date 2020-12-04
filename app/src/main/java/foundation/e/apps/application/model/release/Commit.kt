package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName


data class Commit (

		@SerializedName("id") val id : String,
		@SerializedName("short_id") val short_id : String,
		@SerializedName("created_at") val created_at : String,
		@SerializedName("parent_ids") val parent_ids : List<String>,
		@SerializedName("title") val title : String,
		@SerializedName("message") val message : String,
		@SerializedName("author_name") val author_name : String,
		@SerializedName("author_email") val author_email : String,
		@SerializedName("authored_date") val authored_date : String,
		@SerializedName("committer_name") val committer_name : String,
		@SerializedName("committer_email") val committer_email : String,
		@SerializedName("committed_date") val committed_date : String,
		@SerializedName("web_url") val web_url : String
)