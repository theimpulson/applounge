package io.eelo.appinstaller.common

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.ApplicationViewHolder
import io.eelo.appinstaller.application.model.Application

class ApplicationListAdapter(private val context: Context, private val applicationList: List<Application>) : RecyclerView.Adapter<ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false)
        return ApplicationViewHolder(listItemContainer)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.createApplicationView(applicationList[position], context)
    }

    override fun getItemCount() = applicationList.size
}
