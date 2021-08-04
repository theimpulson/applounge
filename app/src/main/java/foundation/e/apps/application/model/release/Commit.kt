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

data class Commit(

    @SerializedName("id") val id: String,
    @SerializedName("short_id") val short_id: String,
    @SerializedName("created_at") val created_at: String,
    @SerializedName("parent_ids") val parent_ids: List<String>,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("author_name") val author_name: String,
    @SerializedName("author_email") val author_email: String,
    @SerializedName("authored_date") val authored_date: String,
    @SerializedName("committer_name") val committer_name: String,
    @SerializedName("committer_email") val committer_email: String,
    @SerializedName("committed_date") val committed_date: String,
    @SerializedName("web_url") val web_url: String
)
