package io.eelo.appinstaller.search

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.ApplicationManager

class ApplicationListAdapter(private val appList: List<ApplicationManager>) : RecyclerView.Adapter<AppViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false)
        return AppViewHolder(listItemContainer)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.changeApp(appList.get(position))
    }

    override fun getItemCount() = appList.size
}
