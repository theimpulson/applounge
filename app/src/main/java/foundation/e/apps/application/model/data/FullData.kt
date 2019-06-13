/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application.model.data

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Version
import foundation.e.apps.categories.model.Category

class FullData @JsonCreator @JsonIgnoreProperties(ignoreUnknown = true)
constructor(
        @JsonProperty("package_name") packageName: String,
        @JsonProperty("_id") id: String,
        @JsonProperty("name") name: String,
        @JsonProperty("last_modified") lastModified: String,
        @JsonProperty("latest_version") lastVersion: String,
        @JsonProperty("latest_version_number") latestVersionNumber: String?,
        @JsonProperty("latest_downloaded_version") latestDownloadableUpdate: String,
        @JsonProperty("author") author: String,
        @JsonProperty("icon_image_path") iconUri: String,
        @JsonProperty("other_images_path") imagesUri: Array<String>,
        @JsonProperty("category") categoryId: String,
        @JsonProperty("created_on") val createdOn: String,
        @JsonProperty("source") val source: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("app_link") val appLink: String,
        @JsonProperty("licence") val licence: String,
        @JsonProperty("ratings") ratings: BasicData.Ratings?) {

    var basicData = if (ratings == null) {
        BasicData(packageName, id, name, -1f, lastModified, lastVersion,
                latestVersionNumber, latestDownloadableUpdate, author, iconUri, imagesUri,
                null, BasicData.Ratings(-1f, -1f), categoryId)
    } else {
        BasicData(packageName, id, name, -1f, lastModified, lastVersion,
                latestVersionNumber, latestDownloadableUpdate, author, iconUri, imagesUri,
                ratings.privacyRating, ratings, categoryId)
    }

    private var latestVersion: Version? = null;
    val packageName: String
        get() = basicData.packageName

    fun getLastVersion(): Version? {
        return if (basicData.latestDownloadableUpdate != "-1") {
            latestVersion
        } else {
            null
        }
    }

    val category: Category

    init {
        this.category = Category(categoryId)
    }

    @Suppress("unused")
    @JsonAnySetter
    fun jsonCreator(name: String, value: Any) {
        if (name == basicData.latestDownloadableUpdate) {
            val result = value as LinkedHashMap<*, *>
            latestVersion = Version(result["downloaded_flag"] as String?,
                    result["eelo_download_link"] as String,
                    result["min_android"] as String,
                    result["apk_file_sha1"] as String?,
                    result["created_on"] as String,
                    result["version"] as String,
                    result["signature"] as String,
                    result["apk_file_size"] as String,
                    result["update_on"] as String,
                    result["source_apk_download"] as String,
                    result["whats_new"] as String?,
                    name,
                    result["exodus_score"] as Int?,
                    getPermissions(result["exodus_perms"] as ArrayList<String>?),
                    getTrackers(result["exodus_trackers"] as ArrayList<LinkedHashMap<String, String>>?),
                    result["architecture"] as String?)
        }
    }

    private fun getPermissions(rawPermissions: ArrayList<String>?): ArrayList<String>? {
        return if (rawPermissions != null) {
            val permissions = ArrayList<String>()
            rawPermissions.forEach {
                permissions.add(it.substring(it.lastIndexOf(".") + 1))
            }
            permissions
        } else {
            null
        }
    }

    private fun getTrackers(rawTrackers: ArrayList<LinkedHashMap<String, String>>?): ArrayList<String>? {
        return if (rawTrackers != null) {
            val trackers = ArrayList<String>()
            rawTrackers.forEach {
                trackers.add(it["name"]!!)
            }
            trackers
        } else {
            null
        }
    }
}
