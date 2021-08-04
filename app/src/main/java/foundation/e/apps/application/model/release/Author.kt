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

data class Author(

    @SerializedName("id") val id: Int = -1,
    @SerializedName("name") val name: String = "",
    @SerializedName("username") val username: String = "",
    @SerializedName("state") val state: String = "",
    @SerializedName("avatar_url") val avatar_url: String = "",
    @SerializedName("web_url") val web_url: String
)
