package io.eelo.appinstaller.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.Application
import kotlinx.android.synthetic.main.application_list_item.view.*

class ApplicationListAdapter(private val appList: ArrayList<Application>) :
        RecyclerView.Adapter<ApplicationListAdapter.AppViewHolder>() {

    // Define the view holder
    class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.app_icon
        val title: TextView = view.app_title
        val author: TextView = view.app_author
        val ratingBar: RatingBar = view.app_rating_bar
        val rating: TextView = view.app_rating
        val privacyScore: TextView = view.app_privacy_score
    }

    // Create a view holder for the list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false)
        return AppViewHolder(listItemContainer)
    }

    // Set properties of views in the view holder
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.title.text = appList[position].data.name
        holder.author.text = appList[position].data.author
        holder.ratingBar.rating = appList[position].data.stars
        holder.rating.text = appList[position].data.stars.toString()
        holder.privacyScore.text = appList[position].data.privacyScore.toString() + "%"
    }

    // Size of the app list
    override fun getItemCount() = appList.size
}
