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

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.databinding.HomeChildListItemBinding
import foundation.e.apps.home.HomeFragmentDirections
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status

class HomeChildRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val pkgManagerModule: PkgManagerModule
) :
    RecyclerView.Adapter<HomeChildRVAdapter.ViewHolder>() {

    private var oldList = emptyList<FusedApp>()

    private val shimmer = Shimmer.ColorHighlightBuilder()
        .setDuration(500)
        .setBaseAlpha(0.7f)
        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
        .setHighlightAlpha(0.6f)
        .setAutoStart(true)
        .build()

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
        val view = holder.itemView
        val homeApp = oldList[position]
        val shimmerDrawable = ShimmerDrawable().apply { setShimmer(shimmer) }

        holder.binding.apply {
            if (homeApp.origin == Origin.CLEANAPK) {
                appIcon.load(CleanAPKInterface.ASSET_URL + homeApp.icon_image_path) {
                    placeholder(shimmerDrawable)
                }
            } else {
                appIcon.load(homeApp.icon_image_path) {
                    placeholder(shimmerDrawable)
                }
            }
            appName.text = homeApp.name
            homeLayout.setOnClickListener {
                val action = HomeFragmentDirections.actionHomeFragmentToApplicationFragment(
                    homeApp._id,
                    homeApp.package_name,
                    homeApp.origin
                )
                holder.itemView.findNavController().navigate(action)
            }
            when (homeApp.status) {
                Status.INSTALLED -> {
                    installButton.apply {
                        isEnabled = true
                        text = context.getString(R.string.open)
                        setTextColor(Color.WHITE)
                        backgroundTintList = ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            context.startActivity(pkgManagerModule.getLaunchIntent(homeApp.package_name))
                        }
                    }
                }
                Status.UPDATABLE -> {
                    installButton.apply {
                        text = context.getString(R.string.update)
                        setTextColor(Color.WHITE)
                        backgroundTintList = ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            installApplication(homeApp)
                        }
                    }
                }
                Status.UNAVAILABLE -> {
                    installButton.apply {
                        text = context.getString(R.string.install)
                        setOnClickListener {
                            installApplication(homeApp)
                        }
                    }
                }
                Status.QUEUED, Status.DOWNLOADING -> {
                    installButton.apply {
                        text = context.getString(R.string.cancel)
                        setOnClickListener {
                            cancelDownload(homeApp)
                        }
                    }
                }
                Status.INSTALLING, Status.UNINSTALLING -> {
                    installButton.isEnabled = false
                }
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

    private fun installApplication(homeApp: FusedApp) {
        fusedAPIInterface.getApplication(homeApp)
    }

    private fun cancelDownload(homeApp: FusedApp) {
        fusedAPIInterface.cancelDownload(homeApp)
    }
}
