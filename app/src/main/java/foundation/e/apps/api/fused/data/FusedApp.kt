/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
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

package foundation.e.apps.api.fused.data

data class FusedApp(
    val _id: String,
    val author: String,
    val category: String,
    val description: String,
    val exodus_perms: List<Any>,
    val exodus_tracker: List<Any>,
    val icon_image_path: String,
    val last_modified: String,
    val latest_version_code: Int,
    val latest_version_number: String,
    val licence: String,
    val name: String,
    val other_images_path: List<String>,
    val package_name: String,
    val ratings: Ratings,
    val offer_type: Int?,
    var status: Status?,
    var origin: Origin?
)
