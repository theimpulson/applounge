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

package foundation.e.apps.categories.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.api.fused.data.CategoryApp
import foundation.e.apps.categories.CategoriesFragmentDirections
import foundation.e.apps.databinding.CategoriesListItemBinding

class CategoriesRVAdapter :
    RecyclerView.Adapter<CategoriesRVAdapter.ViewHolder>() {

    private var oldList = listOf<CategoryApp>()

    inner class ViewHolder(val binding: CategoriesListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            CategoriesListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.apply {
            categoryLayout.setOnClickListener {
                val direction =
                    CategoriesFragmentDirections.actionCategoriesFragmentToApplicationListFragment(
                        oldList[position].id,
                        oldList[position].name
                    )
                holder.itemView.findNavController().navigate(direction)
            }
            categoryIcon.load(oldList[position].drawable)
            categoryTitle.text = oldList[position].name
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<CategoryApp>) {
        val diffUtil = CategoriesDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}
