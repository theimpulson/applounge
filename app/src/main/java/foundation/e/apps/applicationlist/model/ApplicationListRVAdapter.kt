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

package foundation.e.apps.applicationlist.model

import android.graphics.Color
import android.util.Log
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
import com.facebook.shimmer.Shimmer.Direction.LEFT_TO_RIGHT
import com.facebook.shimmer.ShimmerDrawable
import com.google.android.material.snackbar.Snackbar
import foundation.e.apps.PrivacyInfoViewModel
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.applicationlist.ApplicationListFragmentDirections
import foundation.e.apps.databinding.ApplicationListItemBinding
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.search.SearchFragmentDirections
import foundation.e.apps.updates.UpdatesFragmentDirections
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
import javax.inject.Singleton

@Singleton
class ApplicationListRVAdapter(
    private val fusedAPIInterface: FusedAPIInterface,
    private val privacyInfoViewModel: PrivacyInfoViewModel,
    private val currentDestinationId: Int,
    private val pkgManagerModule: PkgManagerModule,
    private val user: User,
    private val lifecycleOwner: LifecycleOwner
) : ListAdapter<FusedApp, ApplicationListRVAdapter.ViewHolder>(ApplicationDiffUtil()) {

    private val TAG = ApplicationListRVAdapter::class.java.simpleName

    private val shimmer = Shimmer.ColorHighlightBuilder()
        .setDuration(500)
        .setBaseAlpha(0.7f)
        .setDirection(LEFT_TO_RIGHT)
        .setHighlightAlpha(0.6f)
        .setAutoStart(true)
        .build()

    inner class ViewHolder(val binding: ApplicationListItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ApplicationListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val view = holder.itemView
        val searchApp = getItem(position)
        val shimmerDrawable = ShimmerDrawable().apply { setShimmer(shimmer) }

        holder.binding.apply {
            if (searchApp.privacyScore == -1) {
                hidePrivacyScore()
            }
            applicationList.setOnClickListener {
                val action = when (currentDestinationId) {
                    R.id.applicationListFragment -> {
                        ApplicationListFragmentDirections.actionApplicationListFragmentToApplicationFragment(
                            searchApp._id,
                            searchApp.package_name,
                            searchApp.origin
                        )
                    }
                    R.id.searchFragment -> {
                        SearchFragmentDirections.actionSearchFragmentToApplicationFragment(
                            searchApp._id,
                            searchApp.package_name,
                            searchApp.origin
                        )
                    }
                    R.id.updatesFragment -> {
                        UpdatesFragmentDirections.actionUpdatesFragmentToApplicationFragment(
                            searchApp._id,
                            searchApp.package_name,
                            searchApp.origin
                        )
                    }
                    else -> null
                }
                action?.let { direction -> view.findNavController().navigate(direction) }
            }
            appTitle.text = searchApp.name
            appAuthor.text = searchApp.author
            if (searchApp.ratings.usageQualityScore != -1.0) {
                appRating.text = searchApp.ratings.usageQualityScore.toString()
                appRatingBar.rating = searchApp.ratings.usageQualityScore.toFloat()
            }
            if (searchApp.ratings.privacyScore != -1.0) {
                appPrivacyScore.text = view.context.getString(
                    R.string.privacy_rating_out_of,
                    searchApp.ratings.privacyScore.toInt().toString()
                )
            }

            if (searchApp.source.isEmpty()) {
                sourceTag.visibility = View.INVISIBLE
            } else {
                sourceTag.visibility = View.VISIBLE
            }
            sourceTag.text = searchApp.source

            when (searchApp.origin) {
                Origin.GPLAY -> {
                    appIcon.load(searchApp.icon_image_path) {
                        placeholder(shimmerDrawable)
                    }
                }
                Origin.CLEANAPK -> {
                    appIcon.load(CleanAPKInterface.ASSET_URL + searchApp.icon_image_path) {
                        placeholder(shimmerDrawable)
                    }
                }
                else -> Log.wtf(TAG, "${searchApp.package_name} is from an unknown origin")
            }
            when (searchApp.status) {
                Status.INSTALLED -> {
                    installButton.apply {
                        isEnabled = true
                        text = context.getString(R.string.open)
                        setTextColor(Color.WHITE)
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            context.startActivity(pkgManagerModule.getLaunchIntent(searchApp.package_name))
                        }
                    }
                }
                Status.UPDATABLE -> {
                    installButton.apply {
                        text = context.getString(R.string.update)
                        setTextColor(Color.WHITE)
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            installApplication(searchApp, appIcon)
                        }
                    }
                }
                Status.UNAVAILABLE -> {
                    installButton.apply {
                        text = context.getString(R.string.install)
                        setOnClickListener {
                            installApplication(searchApp, appIcon)
                        }
                    }
                }
                Status.QUEUED, Status.AWAITING, Status.DOWNLOADING -> {
                    installButton.apply {
                        text = context.getString(R.string.cancel)
                        setOnClickListener {
                            cancelDownload(searchApp)
                        }
                    }
                }
                Status.INSTALLING, Status.UNINSTALLING -> {
                    installButton.isEnabled = false
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
                }
                Status.INSTALLATION_ISSUE -> {
                    installButton.apply {
                        text = view.context.getString(R.string.retry)
                        setOnClickListener {
                            installApplication(searchApp, appIcon)
                        }
                    }
                }
            }

            showCalculatedPrivacyScoreData(searchApp, view)
        }
    }

    private fun ApplicationListItemBinding.showCalculatedPrivacyScoreData(
        searchApp: FusedApp,
        view: View
    ) {
        if (searchApp.privacyScore > -1) {
            showPrivacyScoreOnAvailableData(searchApp, view)
        } else {
            showPrivacyScoreAfterFetching(searchApp, view)
        }
    }

    private fun ApplicationListItemBinding.showPrivacyScoreOnAvailableData(
        searchApp: FusedApp,
        view: View
    ) {
        showPrivacyScore()
        appPrivacyScore.text = view.context.getString(
            R.string.privacy_rating_out_of,
            searchApp.privacyScore.toString()
        )
    }

    private fun ApplicationListItemBinding.showPrivacyScoreAfterFetching(
        searchApp: FusedApp,
        view: View
    ) {
        privacyInfoViewModel.getAppPrivacyInfoLiveData(searchApp).observe(lifecycleOwner) {
            showPrivacyScore()
            if (it.isSuccess()) {
                searchApp.privacyScore = privacyInfoViewModel.calculatePrivacyScore(searchApp)
                appPrivacyScore.text = view.context.getString(
                    R.string.privacy_rating_out_of,
                    searchApp.privacyScore.toString()
                )
            } else {
                appPrivacyScore.text = view.context.getString(R.string.not_available)
            }
        }
    }

    private fun ApplicationListItemBinding.hidePrivacyScore() {
        progressBar.visibility = View.VISIBLE
        appPrivacyScore.visibility = View.GONE
    }

    private fun ApplicationListItemBinding.showPrivacyScore() {
        progressBar.visibility = View.GONE
        appPrivacyScore.visibility = View.VISIBLE
    }

    fun setData(newList: List<FusedApp>) {
        this.submitList(newList)
    }

    private fun installApplication(searchApp: FusedApp, appIcon: ImageView) {
        fusedAPIInterface.getApplication(searchApp, appIcon)
    }

    private fun cancelDownload(searchApp: FusedApp) {
        fusedAPIInterface.cancelDownload(searchApp)
    }
}
