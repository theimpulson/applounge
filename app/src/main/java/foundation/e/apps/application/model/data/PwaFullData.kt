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

package foundation.e.apps.application.model.data

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.categories.model.Category

class PwaFullData @JsonCreator
constructor(
    @JsonProperty("_id")val id: String,
    @JsonProperty("name")val name: String,
    @param:JsonProperty("description") val description: String,
    @param:JsonProperty("is_pwa") val is_pwa: Boolean,
    @param:JsonProperty("is_web_app") val is_web_app: Boolean,
    @param:JsonProperty("has_https") val has_https: Boolean,
    @param:JsonProperty("url") val url: String,
    @JsonProperty("category") categoryId: String,
    @param:JsonProperty("icon_image_path") val icon_uri: String,
    @param:JsonProperty("other_images_path") val imagesUri: Array<String>,
    @param:JsonProperty("created_on") val created_on: String
) {

    var pwabasicdata = PwasBasicData(id, name, description, is_pwa, is_web_app, has_https, url, categoryId, icon_uri, imagesUri, created_on)

    val category: Category

    init {
        this.category = Category(categoryId, "")
    }
}
