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
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.android.material.snackbar.Snackbar
import foundation.e.apps.AppInfoFetchViewModel
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.databinding.HomeChildListItemBinding
import foundation.e.apps.home.HomeFragmentDirections
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
import foundation.e.apps.utils.modules.PWAManagerModule

class HomeChildRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val pkgManagerModule: PkgManagerModule,
    private val pwaManagerModule: PWAManagerModule,
    private val appInfoFetchViewModel: AppInfoFetchViewModel,
    private val user: User,
    private val lifecycleOwner: LifecycleOwner,
    private val paidAppHandler: ((FusedApp) -> Unit)? = null
) : ListAdapter<FusedApp, HomeChildRVAdapter.ViewHolder>(HomeChildFusedAppDiffUtil()) {

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
        val viewHolder = ViewHolder(
            HomeChildListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.itemView
        val homeApp = getItem(position)
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
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            if (homeApp.is_pwa) {
                                pwaManagerModule.launchPwa(homeApp)
                            } else {
                                context.startActivity(pkgManagerModule.getLaunchIntent(homeApp.package_name))
                            }
                        }
                    }
                    progressBarInstall.visibility = View.GONE
                }
                Status.UPDATABLE -> {
                    installButton.apply {
                        text = context.getString(R.string.update)
                        setTextColor(Color.WHITE)
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            installApplication(homeApp, appIcon)
                        }
                    }
                    progressBarInstall.visibility = View.GONE
                }
                Status.UNAVAILABLE -> {
                    installButton.apply {
                        if (homeApp.isFree) {
                            isEnabled = true
                            text = context.getString(R.string.install)
                            progressBarInstall.visibility = View.GONE
                        } else {
                            isEnabled = false
                            text = ""
                            progressBarInstall.visibility = View.VISIBLE
                            appInfoFetchViewModel.isAppPurchased(homeApp).observe(lifecycleOwner) {
                                isEnabled = true
                                progressBarInstall.visibility = View.GONE
                                text = if (it) context.getString(R.string.install) else homeApp.price
                            }
                        }
                        setTextColor(context.getColor(R.color.colorAccent))
                        backgroundTintList = ContextCompat.getColorStateList(
                            view.context,
                            android.R.color.transparent
                        )
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            if (homeApp.isFree) {
                                installApplication(homeApp, appIcon)
                            } else {
                                paidAppHandler?.invoke(homeApp)
                            }
                        }
                    }
                }
                Status.QUEUED, Status.AWAITING, Status.DOWNLOADING -> {
                    installButton.apply {
                        text = context.getString(R.string.cancel)
                        setTextColor(context.getColor(R.color.colorAccent))
                        backgroundTintList = ContextCompat.getColorStateList(
                            view.context,
                            android.R.color.transparent
                        )
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)

                        setOnClickListener {
                            cancelDownload(homeApp)
                        }
                    }
                    progressBarInstall.visibility = View.GONE
                }
                Status.INSTALLING, Status.UNINSTALLING -> {
                    installButton.apply {
                        isEnabled = false
                        setTextColor(context.getColor(R.color.light_grey))
                        text = context.getString(R.string.installing)
                        backgroundTintList = ContextCompat.getColorStateList(
                            view.context,
                            android.R.color.transparent
                        )
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.light_grey)
                    }
                    progressBarInstall.visibility = View.GONE
                }
                Status.BLOCKED -> {
                    installButton.setOnClickListener {
                        val errorMsg = when (user) {
                            User.ANONYMOUS,
                            User.UNAVAILABLE -> view.context.getString(R.string.install_blocked_anonymous)
                            User.GOOGLE -> view.context.getString(R.string.install_blocked_google)
                        }
                        if (errorMsg.isNotBlank()) {
                            Snackbar.make(view, errorMsg, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    progressBarInstall.visibility = View.GONE
                }
                Status.INSTALLATION_ISSUE -> {
                    installButton.apply {
                        text = view.context.getString(R.string.retry)
                        setTextColor(context.getColor(R.color.colorAccent))
                        backgroundTintList = ContextCompat.getColorStateList(
                            view.context,
                            android.R.color.transparent
                        )
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            installApplication(homeApp, appIcon)
                        }
                    }
                    progressBarInstall.visibility = View.GONE
                }
            }
        }
    }

    fun setData(newList: List<FusedApp>) {
        this.submitList(newList.map { it.copy() })
    }

    private fun installApplication(homeApp: FusedApp, appIcon: ImageView) {
        fusedAPIInterface.getApplication(homeApp, appIcon)
    }

    private fun cancelDownload(homeApp: FusedApp) {
        fusedAPIInterface.cancelDownload(homeApp)
    }
}
