package io.eelo.appinstaller.common

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.Application
import kotlinx.android.synthetic.main.application_list_item.view.*

class ApplicationListAdapter(private val context: Context, private val applicationList: ArrayList<Application>) :
        RecyclerView.Adapter<ApplicationListAdapter.AppViewHolder>() {
    companion object {
        var clickListener: AdapterClickListener? = null
    }

    // Define the view holder
    class AppViewHolder(val view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        val icon: ImageView = view.app_icon
        val title: TextView = view.app_title
        val author: TextView = view.app_author
        val ratingBar: RatingBar = view.app_rating_bar
        val rating: TextView = view.app_rating
        val privacyScore: TextView = view.app_privacy_score
        val installButton: Button = view.app_install

        init {
            view.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            clickListener?.onItemClick(adapterPosition)
        }
    }

    fun setOnItemClickListener(adapterClickListener: AdapterClickListener) {
        clickListener = adapterClickListener
    }

    interface AdapterClickListener {
        fun onItemClick(position: Int)
    }

    // Create a view holder for the list item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false)
        return AppViewHolder(listItemContainer)
    }

    // Set properties of views in the view holder
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.title.text = applicationList[position].data.name
        holder.author.text = applicationList[position].data.author
        holder.ratingBar.rating = applicationList[position].data.stars
        holder.rating.text = applicationList[position].data.stars.toString()
        when {
            applicationList[position].data.stars >= 4.0f -> {
                holder.ratingBar.progressTintList = ColorStateList.valueOf(context.resources.getColor(R.color.colorRatingGood))
                holder.rating.setTextColor(context.resources.getColor(R.color.colorRatingGood))
            }
            applicationList[position].data.stars >= 3.0f -> {
                holder.ratingBar.progressTintList = ColorStateList.valueOf(context.resources.getColor(R.color.colorRatingNeutral))
                holder.rating.setTextColor(context.resources.getColor(R.color.colorRatingNeutral))
            }
            else -> {
                holder.ratingBar.progressTintList = ColorStateList.valueOf(context.resources.getColor(R.color.colorRatingBad))
                holder.rating.setTextColor(context.resources.getColor(R.color.colorRatingBad))
            }
        }
        holder.privacyScore.text = applicationList[position].data.privacyScore.toString()
        when {
            applicationList[position].data.privacyScore >= 7 -> {
                holder.privacyScore.setTextColor(context.resources.getColor(R.color.colorRatingGood))
            }
            applicationList[position].data.privacyScore >= 4 -> {
                holder.privacyScore.setTextColor(context.resources.getColor(R.color.colorRatingNeutral))
            }
            else -> {
                holder.privacyScore.setTextColor(context.resources.getColor(R.color.colorRatingBad))
            }
        }
        when {
            applicationList[position].isInstalled -> {
                holder.installButton.text = context.getString(R.string.action_launch)
            }
            else -> {
                holder.installButton.text = context.getString(R.string.action_install)
            }
        }
    }

    // Size of the app list
    override fun getItemCount() = applicationList.size
}
