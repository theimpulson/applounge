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
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Version
import foundation.e.apps.categories.model.Category

class FullData @JsonCreator
constructor(
        @JsonProperty("_id") id: String,
        @JsonProperty("name") var name: String,
        @JsonProperty("package_name") packageName: String,
        @JsonProperty("latest_version_number") var latestVersionNumber: String,
        @JsonProperty("latest_version_code") lastVersionCode: Long,
        @JsonProperty("latest_downloaded_version") latestDownloadableUpdate: String,
        @JsonProperty("x86_64_latest_downloaded_version") val x86_64_latestDownloadableUpdate: String = "-1",
        @JsonProperty("x86_64_latest_version_number") val x86_64_lastVersionNumber: String = "-1",
        @JsonProperty("x86_64_latest_version_code") var x86_64_lastVersionCode: Long = -1,
        @JsonProperty("armeabi_latest_downloaded_version") val armeabi_latestDownloadableUpdate: String = "-1",
        @JsonProperty("armeabi_latest_version_number") val armeabi_lastVersionNumber: String = "-1",
        @JsonProperty("armeabi_latest_version_code") var armeabi_lastVersionCode: Long = -1,
        @JsonProperty("arm64-v8a_latest_downloaded_version") val arm64_v8a_latest_latestDownloadableUpdate: String = "-1",
        @JsonProperty("arm64-v8a_latest_version_number") val arm64_v8a_lastVersionNumber: String = "-1",
        @JsonProperty("arm64-v8a_latest_version_code") var arm64_v8a_lastVersionCode: Long = -1,
        @JsonProperty("x86_latest_downloaded_version") val x86_latestDownloadableUpdate: String = "-1",
        @JsonProperty("x86_latest_version_number") val x86_lastVersionNumber: String = "-1",
        @JsonProperty("x86_latest_version_code") var x86_lastVersionCode: Long = -1,
        @JsonProperty("armeabi-v7a_latest_downloaded_version") val armeabi_v7a_latestDownloadableUpdate: String = "-1",
        @JsonProperty("armeabi-v7a_latest_version_number") val armeabi_v7a_lastVersionNumber: String = "-1",
        @JsonProperty("armeabi-v7a_latest_version_code") var armeabi_v7a_lastVersionCode: Long = -1,
        @JsonProperty("architectures") val apkArchitecture: ArrayList<String>?,
        @JsonProperty("author") author: String,
        @JsonProperty("icon_image_path") iconUri: String,
        @JsonProperty("other_images_path") imagesUri: Array<String>,
        @JsonProperty("category") categoryId: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("licence") val licence: String,
        @JsonProperty("ratings") ratings: BasicData.Ratings?,
        @JsonProperty("is_pwa ") val is_pwa: Boolean,
        var downloadUrl: String? = null) {


    var basicData = if (ratings == null) {
        BasicData(id, name, packageName, latestVersionNumber, lastVersionCode, latestDownloadableUpdate,
                x86_64_latestDownloadableUpdate, x86_64_lastVersionNumber, x86_64_lastVersionCode, armeabi_latestDownloadableUpdate,
                armeabi_lastVersionNumber, armeabi_lastVersionCode, arm64_v8a_latest_latestDownloadableUpdate, arm64_v8a_lastVersionNumber, arm64_v8a_lastVersionCode,
                x86_latestDownloadableUpdate, x86_lastVersionNumber, x86_lastVersionCode, armeabi_v7a_latestDownloadableUpdate, armeabi_v7a_lastVersionNumber, armeabi_v7a_lastVersionCode, apkArchitecture,
                author, iconUri, imagesUri, null, BasicData.Ratings(-1f, -1f), categoryId, is_pwa)
    } else {
        BasicData(id, name, packageName, latestVersionNumber, lastVersionCode, latestDownloadableUpdate,
                x86_64_latestDownloadableUpdate, x86_64_lastVersionNumber, x86_64_lastVersionCode, armeabi_latestDownloadableUpdate,
                armeabi_lastVersionNumber, armeabi_lastVersionCode, arm64_v8a_latest_latestDownloadableUpdate, arm64_v8a_lastVersionNumber, arm64_v8a_lastVersionCode,
                x86_latestDownloadableUpdate, x86_lastVersionNumber, x86_lastVersionCode, armeabi_v7a_latestDownloadableUpdate, armeabi_v7a_lastVersionNumber, armeabi_v7a_lastVersionCode, apkArchitecture,
                author, iconUri, imagesUri, ratings.privacyRating, ratings, categoryId, is_pwa)
    }

    var latestVersion: Version? = null;
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
        this.category = Category(categoryId, "")
    }

    @Suppress("unused")
    @JsonAnySetter
    fun jsonCreator(name: String, value: Any) {
        if (name == basicData.latestDownloadableUpdate) {
            val result = value as LinkedHashMap<*, *>
            latestVersion = Version(result["downloaded_flag"] as String?,
                    result["download_link"] as String,
                    result["min_android"] as String,
                    result["apk_file_sha1"] as String?,
                    result["created_on"] as String,
                    result["version"] as String,
                    result["signature"] as String,
                    result["apk_file_size"] as String,
                    result["update_on"] as String,
                    name,
                    result["exodus_score"] as Int?,
                    getPermissions(result["exodus_perms"] as ArrayList<String>?),
                    getTrackers(result["exodus_trackers"] as ArrayList<LinkedHashMap<String, String>>?),
                    result["architecture"] as String?,
                    result["is_xapk"] as Boolean)
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
