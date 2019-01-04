package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.app.Activity
import android.support.design.widget.Snackbar
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.*
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationStateListener
import io.eelo.appinstaller.application.model.Downloader
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.application.viewmodel.ApplicationViewModel
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Common.toMiB
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Execute
import kotlinx.android.synthetic.main.application_list_item.view.*
import kotlinx.android.synthetic.main.install_button_layout.view.*

class ApplicationViewHolder(private val activity: Activity, private val view: View) : RecyclerView.ViewHolder(view), ApplicationStateListener {

    private val icon: ImageView = view.app_icon
    private val title: TextView = view.app_title
    private val author: TextView = view.app_author
    private val ratingBar: RatingBar = view.app_rating_bar
    private val rating: TextView = view.app_rating
    private val privacyScore: TextView = view.app_privacy_score
    private val installButton: Button = view.app_install
    private var application: Application? = null
    private val applicationViewModel = ApplicationViewModel()

    init {
        view.setOnClickListener {
            if (application != null) {
                applicationViewModel.onApplicationClick(view.context, application!!)
            }
        }
        installButton.setOnClickListener {
            if (application?.fullData != null &&
                    application!!.fullData!!.getLastVersion() == null) {
                Snackbar.make(view, activity.getString(
                        Common.getScreenErrorDescriptionId(Error.APK_UNAVAILABLE)),
                        Snackbar.LENGTH_LONG).show()
            } else {
                application?.buttonClicked(activity)
            }
        }
    }

    fun createApplicationView(app: Application) {
        icon.setImageDrawable(view.context.resources.getDrawable(R.drawable.ic_app_default))
        app.loadIcon(icon)
        this.application?.removeListener(this)
        this.application = app
        app.addListener(this)
        title.text = app.basicData!!.name
        author.text = app.basicData!!.author
        ratingBar.rating = app.basicData!!.ratings.rating
        if (app.basicData!!.ratings.rating != -1f) {
            rating.text = app.basicData!!.ratings.rating.toString()
        } else {
            rating.text = activity.getString(R.string.not_available)
        }
        // TODO Show actual privacy rating
        privacyScore.text = activity.getString(R.string.not_available)
        stateChanged(app.state)
    }

    override fun stateChanged(state: State) {
        Execute({}, {
            installButton.text = activity.getString(state.installButtonTextId)
            when (state) {
                State.INSTALLING -> {
                    installButton.isEnabled = false
                }
                else -> {
                    installButton.isEnabled = true
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    override fun downloading(downloader: Downloader) {
        downloader.addListener { count, total ->
            installButton.text = "${toMiB(count)}/${toMiB(total)} MiB"
        }
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }
}