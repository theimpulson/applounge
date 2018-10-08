package io.eelo.appinstaller.common

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.*
import kotlinx.android.synthetic.main.application_list_item.view.*

class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view), ApplicationStateListener {

    private val icon: ImageView = view.app_icon
    private val title: TextView = view.app_title
    private val author: TextView = view.app_author
    private val ratingBar: RatingBar = view.app_rating_bar
    private val rating: TextView = view.app_rating
    private val privacyScore: TextView = view.app_privacy_score
    private val installButton: Button = view.app_install
    private var app: Application? = null

    init {
        view.setOnClickListener {
            //TODO show the application's activity
        }
        installButton.setOnClickListener { app?.buttonClicked() }
    }

    fun changeApp(app: Application, context: Context) {
        this.app?.removeListener(this)
        this.app = app
        app.addListener(this)
        title.text = app.data.name
        author.text = app.data.author
        ratingBar.rating = app.data.stars
        rating.text = app.data.stars.toString()
        ratingBar.progressBackgroundTintList = ColorStateList.valueOf(findStarsColor(app.data.stars, context))
        privacyScore.text = app.data.privacyScore.toString()
        privacyScore.setTextColor(findPrivacyColor(app.data.privacyScore, context))
        installButton.text = app.state.buttonText
    }

    private fun findStarsColor(stars: Float, context: Context): Int {
        return context.resources.getColor(when {
            stars >= 4 -> R.color.colorRatingGood
            stars >= 3 -> R.color.colorRatingNeutral
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
        installButton.text = state.buttonText
    }

    override fun downloading(downloader: Downloader) {
        // TODO
    }

    override fun anErrorHasOccurred() {
        // TODO alert the user of the error (while downloading)
    }
}