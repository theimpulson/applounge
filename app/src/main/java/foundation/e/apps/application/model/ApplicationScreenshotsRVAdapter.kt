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

package foundation.e.apps.application.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.databinding.ApplicationScreenshotsListItemBinding

class ApplicationScreenshotsRVAdapter(
    private val circularProgressDrawable: CircularProgressDrawable,
    private val origin: Origin
) :
    RecyclerView.Adapter<ApplicationScreenshotsRVAdapter.ViewHolder>() {

    private var oldList = emptyList<String>()

    inner class ViewHolder(val binding: ApplicationScreenshotsListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ApplicationScreenshotsListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val params = view.root.layoutParams
        params.width = (parent.width * 0.4).toInt()
        view.root.layoutParams = params
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (origin) {
            Origin.CLEANAPK -> {
                holder.binding.imageView.load(CleanAPKInterface.ASSET_URL + oldList[position]) {
                    placeholder(circularProgressDrawable)
                }
            }
            Origin.GPLAY -> {
                holder.binding.imageView.load(oldList[position]) {
                    placeholder(circularProgressDrawable)
                }
            }
            Origin.GITLAB -> {
                TODO("YET TO BE IMPLEMENTED")
            }
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<String>) {
        val diffUtil = ApplicationScreenshotsDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}