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

package foundation.e.apps.applicationlist.model

import androidx.recyclerview.widget.DiffUtil
import foundation.e.apps.api.fused.data.SearchApp

class ApplicationListDiffUtil(
    private val oldList: List<SearchApp>,
    private val newList: List<SearchApp>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Check both id and package name as we are fetching data from multiple sources to avoid issues
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id && oldList[oldItemPosition].package_name == newList[newItemPosition].package_name
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition]._id != newList[newItemPosition]._id -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].author != newList[newItemPosition].author -> false
            oldList[oldItemPosition].ratings.privacyScore != newList[newItemPosition].ratings.privacyScore -> false
            oldList[oldItemPosition].ratings.usageQualityScore != newList[newItemPosition].ratings.usageQualityScore -> false
            oldList[oldItemPosition].icon_image_path != newList[newItemPosition].icon_image_path -> false
            else -> true
        }
    }
}
