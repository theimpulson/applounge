package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationStateListener
import io.eelo.appinstaller.application.model.Downloader
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.application.viewmodel.ApplicationViewModel
import kotlinx.android.synthetic.main.application_list_item.view.*
import java.text.DecimalFormat

class ApplicationViewHolder(val view: View) : RecyclerView.ViewHolder(view), ApplicationStateListener {

    private val icon: ImageView = view.app_icon
    private val title: TextView = view.app_title
    private val author: TextView = view.app_author
    private val ratingBar: RatingBar = view.app_rating_bar
    private val rating: TextView = view.app_rating
    private val privacyScore: TextView = view.app_privacy_score
    private val installButton: Button = view.app_install
    private var application: Application? = null
    private var context: Context? = null

    private val applicationViewModel = ApplicationViewModel()

    init {
        view.setOnClickListener {
            if (context != null && application != null) {
                applicationViewModel.onApplicationClick(context!!, application!!)
            }
        }
        installButton.setOnClickListener { application?.buttonClicked() }
    }

    fun createApplicationView(app: Application, context: Context) {
        this.application?.removeListener(this)
        this.application = app
        this.context = context
        app.addListener(this)
        title.text = app.data.name
        author.text = app.data.author
        ratingBar.rating = app.data.stars
        val decimalFormat = DecimalFormat("##.0")
        rating.text = decimalFormat.format(app.data.stars).toString()
        rating.setTextColor(findStarsColor(app.data.stars, context))
        privacyScore.text = app.data.privacyScore.toString()
        privacyScore.setTextColor(findPrivacyColor(app.data.privacyScore, context))
        stateChanged(app.state)
    }

    private fun findStarsColor(stars: Float, context: Context): Int {
        return context.resources.getColor(when {
            stars >= 4.0f -> R.color.colorRatingGood
            stars >= 3.0f -> R.color.colorRatingNeutral
            else -> R.color.colorRatingBad
        })
    }

    private fun findPrivacyColor(privacyScore: Int, context: Context): Int {
        return context.resources.getColor(when {
            privacyScore >= 7 -> R.color.colorRatingGood
            privacyScore >= 4 -> R.color.colorRatingNeutral
            else -> R.color.colorRatingBad
        })
    }

    override fun stateChanged(state: State) {
        var installButtonText = context!!.resources.getString(R.string.action_install)
        var isInstallButtonEnabled = true
        when (state) {
            State.DOWNLOADING -> {
                installButtonText = context!!.resources.getString(R.string.state_downloading)
                isInstallButtonEnabled = false
            }
            State.INSTALLING -> {
                installButtonText = context!!.resources.getString(R.string.state_installing)
                isInstallButtonEnabled = false
            }
            State.INSTALLED -> {
                installButtonText = context!!.resources.getString(R.string.action_launch)
                isInstallButtonEnabled = true
            }
            State.NOT_UPDATED -> {
                installButtonText = context!!.resources.getString(R.string.action_update)
                isInstallButtonEnabled = true
            }
        }

        installButton.text = installButtonText
        installButton.isEnabled = isInstallButtonEnabled
    }

    @SuppressLint("SetTextI18n")
    override fun downloading(downloader: Downloader) {
        downloader.addListener { count, total ->
            installButton.text = "${toMiB(count)}/${toMiB(total)} MiB"
        }
    }

    private fun toMiB(length: Int): Double {
        return length.div(10486).div(100.0)
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }
}