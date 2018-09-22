package io.eelo.appinstaller.search

import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import io.eelo.appinstaller.application.ApplicationManager
import io.eelo.appinstaller.application.ApplicationStateListener
import io.eelo.appinstaller.application.Downloader
import io.eelo.appinstaller.application.State
import kotlinx.android.synthetic.main.application_list_item.view.*

class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view), ApplicationStateListener {

    private val icon: ImageView = view.app_icon
    private val title: TextView = view.app_title
    private val author: TextView = view.app_author
    private val ratingBar: RatingBar = view.app_rating_bar
    private val rating: TextView = view.app_rating
    private val privacyScore: TextView = view.app_privacy_score
    private val app_install: Button = view.app_install

    var app: ApplicationManager? = null

    init {
        app_install.setOnClickListener { app?.buttonClicked() }
    }


    fun changeApp(app: ApplicationManager) {
        title.text = app.data.name
        author.text = app.data.author
        ratingBar.rating = app.data.stars
        rating.text = app.data.stars.toString()
        privacyScore.text = (app.data.privacyScore.toString() + "%")
        this.app?.setListener(ApplicationStateListener.EMPTY())
        this.app = app
        app.setListener(this)
//        TODO("change icon !")
    }

    override fun stateChanged(state: State) {
        app_install.text = state.buttonText
    }

    override fun downloading(downloader: Downloader) {
//        TODO("not implemented")
    }

    override fun anErrorHasOccurred() {
//        TODO("not implemented")
    }
}