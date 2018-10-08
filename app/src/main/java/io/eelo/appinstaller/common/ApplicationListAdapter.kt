package io.eelo.appinstaller.common

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.Application

class ApplicationListAdapter(private val context: Context, private val apps: List<Application>) : RecyclerView.Adapter<AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false)
        return AppViewHolder(listItemContainer)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.changeApp(apps[position], context)
    }

    override fun getItemCount() = apps.size
}
