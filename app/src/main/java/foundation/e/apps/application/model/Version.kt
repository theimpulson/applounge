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

package foundation.e.apps.application.model

class Version(
    val downloadFlag: String?,
    val downloadLink: String,
    val minAndroid: String,
    val apkSHA: String?,
    val createdOn: String,
    val version: String,
    val signature: String,
    val fileSize: String,
    val updateDate: String,
    val updateName: String,
    val privacyRating: Int?,
    val exodusPermissions: ArrayList<String>?,
    val exodusTrackers: ArrayList<String>?,
    val apkArchitecture: String?,
    val is_xapk: Boolean
)
