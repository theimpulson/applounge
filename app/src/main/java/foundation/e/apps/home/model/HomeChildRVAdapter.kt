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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.databinding.HomeChildListItemBinding
import foundation.e.apps.home.HomeFragmentDirections

class HomeChildRVAdapter(private val fusedAPIInterface: FusedAPIInterface) :
    RecyclerView.Adapter<HomeChildRVAdapter.ViewHolder>() {

    private var oldList = emptyList<FusedApp>()

    inner class ViewHolder(val binding: HomeChildListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HomeChildListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val homeApp = oldList[position]
        holder.binding.apply {
            if (homeApp.origin == Origin.CLEANAPK) {
                appIcon.load(CleanAPKInterface.ASSET_URL + homeApp.icon_image_path)
            } else {
                appIcon.load(homeApp.icon_image_path)
            }
            appName.text = homeApp.name
            installButton.setOnClickListener {
                // Send dummy values as we are fetching home screen data from cleanAPK
                fusedAPIInterface.getApplication(
                    homeApp._id,
                    homeApp.name,
                    homeApp.package_name,
                    homeApp.latest_version_code,
                    homeApp.offer_type,
                    homeApp.origin
                )
            }
            homeLayout.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToApplicationFragment(
                    homeApp._id,
                    homeApp.package_name,
                    homeApp.origin
                )
                holder.itemView.findNavController().navigate(action)
            }
        }
    }

    override fun getItemCount(): Int {
        return oldList.size
    }

    fun setData(newList: List<FusedApp>) {
        val diffUtil = HomeChildDiffUtil(oldList, newList)
        val diffResult = DiffUtil.calculateDiff(diffUtil)
        oldList = newList
        diffResult.dispatchUpdatesTo(this)
    }
}
