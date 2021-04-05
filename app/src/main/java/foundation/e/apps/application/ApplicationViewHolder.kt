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
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
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
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Common.toMiB
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import kotlinx.android.synthetic.main.application_list_item.view.*
import kotlinx.android.synthetic.main.install_button_layout.view.*


class ApplicationViewHolder(private val activity: Activity, private val view: View, accentColorOS: Int) :
        RecyclerView.ViewHolder(view),
        ApplicationStateListener,
        Downloader.DownloadProgressCallback,
        BasicData.IconLoaderCallback,
        PwasBasicData.IconLoaderCallback {


    private val icon: ImageView = view.app_icon
    private val title: TextView = view.app_title
    private val pwa_icon: TextView = view.pwa_sympol
    private val author: TextView = view.app_author
    private val ratingBar: RatingBar = view.app_rating_bar
    private val rating: TextView = view.app_rating
    private val privacyScore: TextView = view.app_privacy_score
    private var installButton: Button = view.app_install
    private var application: Application? = null
    private val applicationViewModel = ApplicationViewModel()
    private var downloader: Downloader? = null
    var accentColorOS = accentColorOS;
    init {
        pwa_icon.visibility = View.GONE
        view.setOnClickListener {
            if (application != null) {
                if (application!!.packageName != Constants.MICROG_PACKAGE)
                    applicationViewModel.onApplicationClick(view.context, application!!)
            }
        }


        installButton.setTextColor(Color.parseColor("#ffffff"))
        if (0 != this.accentColorOS) {
            installButton.setBackgroundColor(this.accentColorOS)
        }
        installButton.setOnClickListener {
            if (application?.fullData != null &&
                    application!!.fullData!!.getLastVersion() == null) {
                Snackbar.make(view, activity.getString(
                        Error.APK_UNAVAILABLE.description),
                        Snackbar.LENGTH_LONG).show()
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

        pwa_icon.visibility = View.GONE
        this.application = app

        if (app.basicData != null) {
            this.application?.removeListener(this)
            this.application = app
            icon.setImageDrawable(view.context.resources.getDrawable(R.drawable.ic_app_default))
            application!!.loadIcon(this)
            application!!.addListener(this)
            title.text = application!!.basicData!!.name
            author.text = application!!.basicData!!.author
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
            icon.setImageDrawable(view.context.resources.getDrawable(R.drawable.ic_app_default))
            application!!.addListener(this)
            if (application!!.searchAppsBasicData != null) {
                if (application!!.searchAppsBasicData!!.is_pwa) {
                    pwa_icon.visibility = View.VISIBLE
                }
                application!!.SearchAppsloadIcon(this)
                title.text = application!!.searchAppsBasicData!!.name
                author.text = application!!.searchAppsBasicData!!.author
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
        Execute({}, {

            // installButton.setBackgroundResource(R.drawable.app_install_border_simple)
            installButton.text = activity.getString(state.installButtonTextId)

            when (state) {

                State.NOT_DOWNLOADED -> {
                    if (0 != this.accentColorOS) {
                        installButton.setTextColor(this.accentColorOS)
                    } else {

                        installButton.setTextColor(Color.parseColor("#0088ED"))
                    }
                    installButton.setBackgroundResource(R.drawable.app_install_border_simple)
                    installButton.isEnabled = true
                }

                State.INSTALLED -> {

                    installButton.isEnabled =
                            Common.appHasLaunchActivity(activity, application!!.packageName)
                    if (0 != this.accentColorOS) {
                        installButton.setBackgroundColor(this.accentColorOS)
                    } else {
                        installButton.setBackgroundResource(R.drawable.app_install_border)
                    }
                    installButton.setTextColor(Color.parseColor("#FAFAFA"))

                }
                State.INSTALLING -> {
                    installButton.isEnabled = false
                }
                State.NOT_UPDATED -> {
                        installButton.setTextColor(Color.parseColor("#FAFAFA"))
                        if (0 != this.accentColorOS) {
                            installButton.setBackgroundColor(this.accentColorOS)
                        } else {
                            installButton.setBackgroundResource(R.drawable.app_install_border)
                        }

                    installButton.isEnabled = true
                }
                else -> {
                    installButton.setTextColor(Color.parseColor("#0088ED"))
                    installButton.isEnabled = true
                }
            }

        })
    }

    override fun downloading(downloader: Downloader) {
        this.downloader = downloader
        this.downloader!!.addListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun notifyDownloadProgress(count: Int, total: Int) {
        installButton.setGravity(Gravity.CENTER)
        installButton.text = ((toMiB(count) / toMiB(total)) * 100).toInt().toString() + "%"
        installButton.setTextColor(Color.parseColor("#0088ED"))
        installButton.setBackgroundResource(R.drawable.app_installing_border_simple)
    }

    override fun anErrorHasOccurred(error: Error) {
        Snackbar.make(activity.findViewById(R.id.container),
                activity.getString(error.description),
                Snackbar.LENGTH_LONG).show()
    }

    fun onViewRecycled() {
        downloader?.removeListener(this)
        downloader = null
    }

}
