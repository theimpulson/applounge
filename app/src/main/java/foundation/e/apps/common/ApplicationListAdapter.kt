package foundation.e.apps.common

import android.app.Activity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import foundation.e.apps.R
import foundation.e.apps.application.ApplicationViewHolder
import foundation.e.apps.application.model.Application

class ApplicationListAdapter(private val activity: Activity, private val applicationList: List<Application>) : RecyclerView.Adapter<ApplicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.application_list_item, parent, false)
        return ApplicationViewHolder(activity, listItemContainer)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.createApplicationView(applicationList[position])
    }

    override fun getItemCount() = applicationList.size

    override fun onViewRecycled(holder: ApplicationViewHolder) {
        holder.onViewRecycled()
    }
}
