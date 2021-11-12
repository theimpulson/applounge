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

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedHome
import foundation.e.apps.databinding.HomeParentListItemBinding

class HomeParentRVAdapter(private val fusedAPIInterface: FusedAPIInterface) :
    RecyclerView.Adapter<HomeParentRVAdapter.ViewHolder>() {

    private var oldList = emptyList<FusedHome>()

    inner class ViewHolder(val binding: HomeParentListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HomeParentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homeChildRVAdapter = HomeChildRVAdapter(fusedAPIInterface)
        homeChildRVAdapter.setData(oldList[position].list)

        holder.binding.titleTV.text = oldList[position].title
        holder.binding.childRV.apply {
            adapter = homeChildRVAdapter
            layoutManager =
                LinearLayoutManager(
                    holder.binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<FusedHome>) {
        val diffUtil = HomeParentDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}