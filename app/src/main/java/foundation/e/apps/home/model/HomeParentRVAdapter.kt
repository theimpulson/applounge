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

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import foundation.e.apps.AppProgressViewModel
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.FusedHome
import foundation.e.apps.databinding.HomeParentListItemBinding
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
import kotlinx.coroutines.launch

class HomeParentRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val pkgManagerModule: PkgManagerModule,
    private val user: User,
    private val mainActivityViewModel: MainActivityViewModel,
    private val lifecycleOwner: LifecycleOwner,
    private val appProgressViewModel: AppProgressViewModel
) : ListAdapter<FusedHome, HomeParentRVAdapter.ViewHolder>(FusedHomeDiffUtil()) {

    private val viewPool = RecyclerView.RecycledViewPool()

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
            HomeChildRVAdapter(fusedAPIInterface, pkgManagerModule, user, appProgressViewModel)
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

        appProgressViewModel.downloadProgress.observe(lifecycleOwner) {
            val childRV = holder.binding.childRV
            val adapter = childRV.adapter as HomeChildRVAdapter
            appProgressViewModel.viewModelScope.launch {
                adapter.currentList.forEach { fusedApp ->
                    if(fusedApp.status == Status.DOWNLOADING) {
                        val progress = appProgressViewModel.calculateProgress(fusedApp, it)
                        val downloadProgress =
                            ((progress.second / progress.first.toDouble()) * 100).toInt()
                        Log.d("HomeParentAdapter", "download progress of ===> ${fusedApp.name} : $downloadProgress")
                        val viewHolder = childRV.findViewHolderForAdapterPosition(adapter.currentList.indexOf(fusedApp))
                        viewHolder?.let {
                            (viewHolder as HomeChildRVAdapter.ViewHolder).binding.installButton.text = "$downloadProgress%"
                        }
                    }
                }

            }
        }
        observeAppInstall(fusedHome, homeChildRVAdapter)
    }

    private fun observeAppInstall(
        fusedHome: FusedHome,
        homeChildRVAdapter: RecyclerView.Adapter<*>?
    ) {
        mainActivityViewModel.downloadList.observe(lifecycleOwner) {
            updateInstallingAppStatus(it, fusedHome)
            (homeChildRVAdapter as HomeChildRVAdapter).setData(fusedHome.list)
        }
    }

    private fun updateInstallingAppStatus(
        downloadList: List<FusedDownload>,
        fusedHome: FusedHome
    ) {
        downloadList.forEach { fusedDownload ->
            findInstallingApp(fusedHome, fusedDownload)?.status = fusedDownload.status
        }
    }

    private fun findInstallingApp(fusedHome: FusedHome, fusedDownload: FusedDownload): FusedApp? {
        return fusedHome.list.find { app ->
            app.origin == fusedDownload.origin && (app.package_name == fusedDownload.package_name || app._id == fusedDownload.id)
        }
    }

    fun setData(newList: List<FusedHome>) {
        submitList(newList.map { it.copy() })
    }
}
