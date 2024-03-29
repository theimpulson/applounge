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
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.AppInfoFetchViewModel
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.FusedHome
import foundation.e.apps.databinding.HomeParentListItemBinding
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.User
import foundation.e.apps.utils.modules.PWAManagerModule

class HomeParentRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val pkgManagerModule: PkgManagerModule,
    private val pwaManagerModule: PWAManagerModule,
    private val user: User,
    private val mainActivityViewModel: MainActivityViewModel,
    private val appInfoFetchViewModel: AppInfoFetchViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val paidAppHandler: ((FusedApp) -> Unit)? = null
) : ListAdapter<FusedHome, HomeParentRVAdapter.ViewHolder>(FusedHomeDiffUtil()) {

    private val viewPool = RecyclerView.RecycledViewPool()
    private var isDownloadObserverAdded = false

    inner class ViewHolder(val binding: HomeParentListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            HomeParentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fusedHome = getItem(position)
        val homeChildRVAdapter =
            HomeChildRVAdapter(
                fusedAPIInterface,
                pkgManagerModule,
                pwaManagerModule,
                appInfoFetchViewModel,
                mainActivityViewModel,
                user,
                lifecycleOwner,
                paidAppHandler
            )
        homeChildRVAdapter.setData(fusedHome.list)

        holder.binding.titleTV.text = fusedHome.title
        holder.binding.childRV.apply {
            recycledViewPool.setMaxRecycledViews(0, 0)
            adapter = homeChildRVAdapter
            layoutManager =
                LinearLayoutManager(
                    holder.binding.root.context,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            setRecycledViewPool(viewPool)
        }
        observeAppInstall(fusedHome, homeChildRVAdapter)
    }

    private fun observeAppInstall(
        fusedHome: FusedHome,
        homeChildRVAdapter: RecyclerView.Adapter<*>?
    ) {
        mainActivityViewModel.downloadList.observe(lifecycleOwner) {
            mainActivityViewModel.updateStatusOfFusedApps(fusedHome.list, it)
            (homeChildRVAdapter as HomeChildRVAdapter).setData(fusedHome.list)
        }
    }

    fun setData(newList: List<FusedHome>) {
        submitList(newList.map { it.copy() })
    }
}
