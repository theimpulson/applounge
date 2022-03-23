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
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import com.google.android.material.snackbar.Snackbar
import foundation.e.apps.AppProgressViewModel
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
import kotlinx.coroutines.launch

class HomeChildRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val pkgManagerModule: PkgManagerModule,
    private val user: User,
    private val appProgressViewModel: AppProgressViewModel
) : ListAdapter<FusedApp, HomeChildRVAdapter.ViewHolder>(HomeChildFusedAppDiffUtil()) {

    private val shimmer = Shimmer.ColorHighlightBuilder()
        .setDuration(500)
        .setBaseAlpha(0.7f)
        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
        .setHighlightAlpha(0.6f)
        .setAutoStart(true)
        .build()

    inner class ViewHolder(val binding: HomeChildListItemBinding) :
        RecyclerView.ViewHolder(binding.root), LifecycleOwner {
        private val lifecycleRegistry = LifecycleRegistry(this)

        init {
            lifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
        }

        fun onAppear() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        fun onDisappear() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }

        override fun getLifecycle(): Lifecycle {
            return lifecycleRegistry
        }
    }

    override fun onViewAttachedToWindow(holder: HomeChildRVAdapter.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        holder.onAppear()
    }

    override fun onViewDetachedFromWindow(holder: HomeChildRVAdapter.ViewHolder) {
        holder.onDisappear()
        super.onViewDetachedFromWindow(holder)
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        for (i in 0..recyclerView.childCount) {
            val view = recyclerView.getChildAt(i)
            if (view != null) {
                val holder = recyclerView.getChildViewHolder(view)
                holder?.let {
                    appProgressViewModel.downloadProgress.removeObservers(holder as LifecycleOwner)
                    (holder as HomeChildRVAdapter.ViewHolder).onDisappear()
                }
            }
        }
        super.onDetachedFromRecyclerView(recyclerView)
    }

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
                    appProgressViewModel.downloadProgress.removeObservers(holder)
                    installButton.apply {
                        isEnabled = true
                        text = context.getString(R.string.open)
                        setTextColor(Color.WHITE)
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            context.startActivity(pkgManagerModule.getLaunchIntent(homeApp.package_name))
                        }
                    }
                }
                Status.UPDATABLE -> {
                    appProgressViewModel.downloadProgress.removeObservers(holder)
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
                }
                Status.UNAVAILABLE -> {
                    appProgressViewModel.downloadProgress.removeObservers(holder)
                    installButton.apply {
                        text = context.getString(R.string.install)
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

                        appProgressViewModel.downloadProgress.observe(holder) {
                            appProgressViewModel.viewModelScope.launch {
                                val progress = appProgressViewModel.calculateProgress(homeApp, it)
                                if (progress.second > 0 && progress.second <= progress.first) {
                                    text = "${((progress.second / progress.first.toDouble()) * 100).toInt()}%"
                                }
                            }
                        }

                        setOnClickListener {
                            cancelDownload(homeApp)
                        }
                    }
                }
                Status.INSTALLING, Status.UNINSTALLING -> {
                    appProgressViewModel.downloadProgress.removeObservers(holder)
                    installButton.apply {
                        isEnabled = false
                        setTextColor(context.getColor(R.color.light_grey))
                        backgroundTintList = ContextCompat.getColorStateList(
                            view.context,
                            android.R.color.transparent
                        )
                        strokeColor =
                            ContextCompat.getColorStateList(view.context, R.color.light_grey)
                    }
                }
                Status.BLOCKED -> {
                    appProgressViewModel.downloadProgress.removeObservers(holder)
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
                }
                Status.INSTALLATION_ISSUE -> {
                    appProgressViewModel.downloadProgress.removeObservers(holder)
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
