/*
 * Copyright (C) 2019-2021  E FOUNDATION
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

package foundation.e.apps.application

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.ApplicationStateListener
import foundation.e.apps.application.model.Downloader
import foundation.e.apps.application.model.State
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.application.viewmodel.ApplicationViewModel
import foundation.e.apps.databinding.ApplicationListItemBinding
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Common.toMiB
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

class ApplicationViewHolder(private val activity: Activity, binding: ApplicationListItemBinding, private val accentColorOS: Int) :
    RecyclerView.ViewHolder(binding.root),
    ApplicationStateListener,
    Downloader.DownloadProgressCallback,
    BasicData.IconLoaderCallback,
    PwasBasicData.IconLoaderCallback {

    private val view = binding.root
    private val icon = binding.appIcon
    private val title = binding.appTitle
    private val pwaSympol = binding.pwaSympol
    private val author = binding.appAuthor
    private val ratingBar = binding.appRatingBar
    private val rating = binding.appRating
    private val privacyScore = binding.appPrivacyScore
    private var installButton = binding.simpleInstallButtonLayout.appInstall
    private var application: Application? = null
    private val applicationViewModel = ApplicationViewModel()
    private var downloader: Downloader? = null
    init {
        pwaSympol.visibility = View.GONE
        view.setOnClickListener {
            if (application != null) {
                if (application!!.packageName != Constants.MICROG_PACKAGE)
                    applicationViewModel.onApplicationClick(view.context, application!!)
            }
        }

        installButton.setOnClickListener {
            if (application?.fullData != null &&
                application!!.fullData!!.getLastVersion() == null
            ) {
                Snackbar.make(
                    view,
                    activity.getString(
                        Error.APK_UNAVAILABLE.description
                    ),
                    Snackbar.LENGTH_LONG
                ).show()
            } else if (application?.pwabasicdata != null) {
                application?.pwaInstall(activity)
            } else if (application?.searchAppsBasicData != null && application?.searchAppsBasicData!!.is_pwa) {
                application?.pwaInstall(activity)
            } else {
                application?.buttonClicked(activity, activity)
            }
        }
    }

    fun createApplicationView(app: Application) {

        pwaSympol.visibility = View.GONE
        this.application = app

        if (app.basicData != null) {
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_app_default))
            application!!.loadIcon(this)
            application!!.addListener(this)
            title.text = application!!.basicData!!.name
            author.text = application!!.basicData!!.author

            val drawable = ratingBar.progressDrawable
            drawable.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.SRC_IN)
            ratingBar.rating = application!!.basicData!!.ratings.rating!!

            if (application!!.basicData!!.ratings.rating != -1f) {
                rating.text = application!!.basicData!!.ratings.rating.toString()
            } else {
                rating.text = activity.getString(R.string.not_available)
            }
            if (application!!.basicData!!.privacyRating != null && application!!.basicData!!.privacyRating != -1f) {
                privacyScore.text = application!!.basicData!!.privacyRating.toString()
            } else {
                privacyScore.text = activity.getString(R.string.not_available)
            }
        } else {
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_app_default))
            application!!.addListener(this)
            if (application!!.searchAppsBasicData != null) {
                if (application!!.searchAppsBasicData!!.is_pwa) {
                    pwaSympol.visibility = View.VISIBLE
                }
                application!!.SearchAppsloadIcon(this)
                title.text = application!!.searchAppsBasicData!!.name
                author.text = application!!.searchAppsBasicData!!.author

                val drawable = ratingBar.progressDrawable
                drawable.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.SRC_IN)
                ratingBar.rating = application!!.searchAppsBasicData!!.ratings.rating!!

                if (application!!.searchAppsBasicData!!.ratings.rating != -1f) {
                    rating.text = application!!.searchAppsBasicData!!.ratings.rating.toString()
                } else {
                    rating.text = activity.getString(R.string.not_available)
                }
                if (application!!.searchAppsBasicData!!.ratings.privacyRating != null && application!!.searchAppsBasicData!!.ratings.privacyRating != -1f) {
                    privacyScore.text = application!!.searchAppsBasicData!!.ratings.privacyRating.toString()
                } else {
                    privacyScore.text = activity.getString(R.string.not_available)
                }
            } else {
                application!!.PwaloadIcon(this)
                title.text = application!!.pwabasicdata!!.name
            }
        }
        stateChanged(application!!.state)
    }

    override fun onIconLoaded(application: Application, bitmap: Bitmap) {
        if (this.application != null && application == this.application) {
            icon.setImageBitmap(bitmap)
        }
    }

    override fun stateChanged(state: State) {
        Execute(
            {},
            {
                installButton.setTextColor(accentColorOS)

                installButton.setBackgroundResource(R.drawable.app_installing_border_simple)
                val drawable = installButton.background as GradientDrawable
                drawable.setStroke(2, accentColorOS)

                installButton.text = activity.getString(state.installButtonTextId)
                installButton.clearAnimation()
                installButton.clearFocus()
                when (state) {
                    State.NOT_DOWNLOADED -> {
                        installButton.isEnabled = true
                    }
                    State.DOWNLOADING -> {
                        installButton.isEnabled = true
                        installButton.background.clearColorFilter()
                    }
                    State.INSTALLED -> {
                        installButton.isEnabled =
                            Common.appHasLaunchActivity(activity, application!!.packageName)
                        installButton.setTextColor(ContextCompat.getColor(activity, R.color.color_default_view_on_accent))
                        installButton.background.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.SRC_IN)
                    }
                    State.INSTALLING -> {
                        installButton.isEnabled = false
                        installingAnimation()
                    }
                    State.NOT_UPDATED -> {
                        installButton.isEnabled = true
                        installButton.setTextColor(ContextCompat.getColor(activity, R.color.color_default_view_on_accent))
                        installButton.background.colorFilter = PorterDuffColorFilter(accentColorOS, PorterDuff.Mode.SRC_IN)
                    }
                }
            }
        )
    }

    override fun downloading(downloader: Downloader) {
        this.downloader = downloader
        this.downloader!!.addListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun notifyDownloadProgress(count: Int, total: Int) {
        installButton.text = ((toMiB(count) / toMiB(total)) * 100).toInt().toString() + "%"
    }

    private fun installingAnimation() {
        val anim = AlphaAnimation(0.0f, 1.0f)
        anim.duration = 200 // You can manage the blinking time with this parameter
        anim.startOffset = 20
        anim.repeatMode = Animation.REVERSE
        anim.repeatCount = Animation.INFINITE
        installButton.startAnimation(anim)
    }
    override fun anErrorHasOccurred(error: Error) {
        Snackbar.make(
            activity.findViewById(R.id.container),
            activity.getString(error.description),
            Snackbar.LENGTH_LONG
        ).show()
    }

    fun onViewRecycled() {
        downloader?.removeListener(this)
        downloader = null
    }
}
