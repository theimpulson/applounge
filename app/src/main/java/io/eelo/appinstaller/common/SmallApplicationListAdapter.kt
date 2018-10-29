package io.eelo.appinstaller.common

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.SmallApplicationViewHolder
import io.eelo.appinstaller.application.model.Application

class SmallApplicationListAdapter(private val activity: Activity, private val applicationList: List<Application>) : RecyclerView.Adapter<SmallApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmallApplicationViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.small_application_list_item, parent, false)
        return SmallApplicationViewHolder(activity, listItemContainer)
    }

    override fun onBindViewHolder(holder: SmallApplicationViewHolder, position: Int) {
        holder.createApplicationView(applicationList[position])
    }

    override fun getItemCount() = applicationList.size
}
