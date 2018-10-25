package io.eelo.appinstaller.application

import android.annotation.SuppressLint
import android.os.AsyncTask
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
import io.eelo.appinstaller.common.ProxyBitmap
import kotlinx.android.synthetic.main.application_list_item.view.*
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ApplicationViewHolder(private val view: View) : RecyclerView.ViewHolder(view), ApplicationStateListener {

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
        installButton.setOnClickListener { application?.buttonClicked() }
    }

    fun createApplicationView(app: Application) {
        if (app.data.iconImage != null) {
            icon.setImageBitmap((app.data.iconImage as ProxyBitmap).getBitmap())
        } else {
            icon.setImageDrawable(view.context.resources.getDrawable(R.drawable.ic_app_default))
            ImageDownloader {
                icon.setImageBitmap(it)
                app.data.iconImage = ProxyBitmap(it)

            }.execute(app.data.icon)
        }
        this.application?.removeListener(this)
        this.application = app
        app.addListener(this)
        title.text = app.data.name
        author.text = app.data.author
        ratingBar.rating = app.data.stars
        val decimalFormat = DecimalFormat("##.0")
        rating.text = decimalFormat.format(app.data.stars).toString()
        rating.setTextColor(findStarsColor(app.data.stars))
        privacyScore.text = app.data.privacyScore.toString()
        privacyScore.setTextColor(findPrivacyColor(app.data.privacyScore))
        stateChanged(app.state)
    }

    private fun findStarsColor(stars: Float): Int {
        return view.context.resources.getColor(when {
            stars >= 4.0f -> R.color.colorRatingGood
            stars >= 3.0f -> R.color.colorRatingNeutral
            else -> R.color.colorRatingBad
        })
    }

    private fun findPrivacyColor(privacyScore: Int): Int {
        return view.context.resources.getColor(when {
            privacyScore >= 7 -> R.color.colorRatingGood
            privacyScore >= 4 -> R.color.colorRatingNeutral
            else -> R.color.colorRatingBad
        })
    }

    override fun stateChanged(state: State) {
        var installButtonText = R.string.action_install
        var isInstallButtonEnabled = true
        when (state) {
            State.DOWNLOADING -> {
                installButtonText = R.string.state_downloading
                isInstallButtonEnabled = false
            }
            State.INSTALLING -> {
                installButtonText = R.string.state_installing
                isInstallButtonEnabled = false
            }
            State.INSTALLED -> {
                installButtonText = R.string.action_launch
            }
            State.NOT_UPDATED -> {
                installButtonText = R.string.action_update
            }
        }
        object : AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                return null
            }

            override fun onPostExecute(result: Void?) {
                installButton.text = view.context.resources.getString(installButtonText)
                installButton.isEnabled = isInstallButtonEnabled
            }
        }.execute()
    }

    @SuppressLint("SetTextI18n")
    override fun downloading(downloader: Downloader) {
        downloader.addListener { count, total ->
            installButton.text = "${toMiB(count)}/${toMiB(total)} MiB"
        }
    }

    private fun toMiB(length: Int): Double {
        val inMiB = length.div(1048576)
        return inMiB.times(100.0).roundToInt().div(100.0)
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }
}