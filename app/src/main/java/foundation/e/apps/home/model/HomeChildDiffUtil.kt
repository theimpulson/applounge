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

package foundation.e.apps.home.model

import androidx.recyclerview.widget.DiffUtil
import foundation.e.apps.api.fused.data.FusedApp

class HomeChildDiffUtil(
    private val oldList: List<FusedApp>,
    private val newList: List<FusedApp>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        // Check id, package name and origin as we are fetching data from multiple sources to avoid issues
        return oldList[oldItemPosition]._id == newList[newItemPosition]._id &&
            oldList[oldItemPosition].package_name == newList[newItemPosition].package_name &&
            oldList[oldItemPosition].origin == newList[newItemPosition].origin
    }

    // TODO: FIX LOGIC HERE TO AVOID UPDATING ENTIRE ITEM
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return when {
            oldList[oldItemPosition]._id != newList[newItemPosition]._id -> false
            oldList[oldItemPosition].author != newList[newItemPosition].author -> false
            oldList[oldItemPosition].category != newList[newItemPosition].category -> false
            oldList[oldItemPosition].description != newList[newItemPosition].description -> false
            oldList[oldItemPosition].perms != newList[newItemPosition].perms -> false
            oldList[oldItemPosition].trackers != newList[newItemPosition].trackers -> false
            oldList[oldItemPosition].last_modified != newList[newItemPosition].last_modified -> false
            oldList[oldItemPosition].latest_version_code != newList[newItemPosition].latest_version_code -> false
            oldList[oldItemPosition].latest_version_number != newList[newItemPosition].latest_version_number -> false
            oldList[oldItemPosition].licence != newList[newItemPosition].licence -> false
            oldList[oldItemPosition].name != newList[newItemPosition].name -> false
            oldList[oldItemPosition].other_images_path != newList[newItemPosition].other_images_path -> false
            oldList[oldItemPosition].package_name != newList[newItemPosition].package_name -> false
            oldList[oldItemPosition].ratings != newList[newItemPosition].ratings -> false
            oldList[oldItemPosition].offer_type != newList[newItemPosition].offer_type -> false
            oldList[oldItemPosition].status != newList[newItemPosition].status -> false
            oldList[oldItemPosition].origin != newList[newItemPosition].origin -> false
            oldList[oldItemPosition].shareUrl != newList[newItemPosition].shareUrl -> false
            oldList[oldItemPosition].appSize != newList[newItemPosition].appSize -> false
            else -> false
        }
    }
}
