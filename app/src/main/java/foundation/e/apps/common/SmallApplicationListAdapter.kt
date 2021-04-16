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

package foundation.e.apps.common

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.R
import foundation.e.apps.application.SmallApplicationViewHolder
import foundation.e.apps.application.model.Application

class SmallApplicationListAdapter(private val activity: Activity, private val applicationList: List<Application>) : RecyclerView.Adapter<SmallApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallApplicationViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.small_application_list_item, parent, false)
        return SmallApplicationViewHolder(activity, listItemContainer)
    }

    override fun onBindViewHolder(holder: SmallApplicationViewHolder, position: Int) {
        holder.createApplicationView(applicationList[position])
    }

    override fun getItemCount() = applicationList.size

    override fun onViewRecycled(holder: SmallApplicationViewHolder) {
        holder.onViewRecycled()
    }
}
