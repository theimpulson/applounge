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
import foundation.e.apps.databinding.SmallApplicationListItemBinding
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Common.toMiB
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

class SmallApplicationViewHolder(private val activity: Activity, binding: SmallApplicationListItemBinding) :
    RecyclerView.ViewHolder(binding.root),
    ApplicationStateListener,
    Downloader.DownloadProgressCallback,
    BasicData.IconLoaderCallback,
    PwasBasicData.IconLoaderCallback {

    private val view = binding.root
    private val icon = binding.appIcon
    private val title = binding.appTitle
    private val installButton = binding.simpleInstallButtonLayout.appInstall
    private var application: Application? = null

    private val applicationViewModel = ApplicationViewModel()
    private var downloader: Downloader? = null
    private val accentColorOS = Common.getAccentColor(activity)

    init {
        view.setOnClickListener {
            if (application != null) {
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
            } else {
                application?.buttonClicked(activity, activity)
            }
        }
    }
    fun createApplicationView(app: Application) {
        if (app.basicData != null) {
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_app_default))
            application!!.loadIcon(this)
            application!!.addListener(this)
            title.text = application!!.basicData!!.name
        } else if (app.pwabasicdata != null) {
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(AppCompatResources.getDrawable(activity, R.drawable.ic_app_default))
            application!!.PwaloadIcon(this)
            application!!.addListener(this)
            title.text = application!!.pwabasicdata!!.name
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
