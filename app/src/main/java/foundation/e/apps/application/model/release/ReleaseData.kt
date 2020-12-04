package foundation.e.apps.application.model.release

import com.google.gson.annotations.SerializedName


class ReleaseData(
        @SerializedName("name") val name: String,
        @SerializedName("tag_name") val tag_name: String,
        @SerializedName("description") val description: String,
        @SerializedName("description_html") val description_html: String,
        @SerializedName("created_at") val created_at: String,
        @SerializedName("released_at") val released_at: String,
        @SerializedName("author") val author: Author,
        @SerializedName("commit") val commit: Commit,
        @SerializedName("assets") val assets: Assets,
        @SerializedName("upcoming_release") val upcoming_release: Boolean,
        @SerializedName("commit_path") val commit_path: String,
        @SerializedName("tag_path") val tag_path: String,
        @SerializedName("evidence_sha") val evidence_sha: String,
        @SerializedName("evidences") val evidences: List<Evidences>
)