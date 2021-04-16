/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

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