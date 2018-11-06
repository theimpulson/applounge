package io.eelo.appinstaller.home

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearLayoutManager.HORIZONTAL
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.RecyclerView.Adapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.common.SmallApplicationListAdapter
import io.eelo.appinstaller.home.HomeCategoryAdapter.HomeCategoryViewHolder
import kotlinx.android.synthetic.main.home_category_list_item.view.*

class HomeCategoryAdapter(private val activity: Activity, private val categoriesMap: LinkedHashMap<String, ArrayList<Application>>) : Adapter<HomeCategoryViewHolder>() {

    private val categories = categoriesMap.keys.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCategoryViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.home_category_list_item, parent, false)
        return HomeCategoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HomeCategoryViewHolder, position: Int) {
        val category = categories[position]
        val apps = categoriesMap[category]!!
        holder.bind(category, apps, activity)
    }

    override fun getItemCount() = categoriesMap.size

    class HomeCategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val titleView: TextView = view.category_title
        private val applicationList: RecyclerView = view.application_list

        init {
            applicationList.layoutManager = LinearLayoutManager(view.context, HORIZONTAL, false)
        }

        fun bind(title: String, apps: ArrayList<Application>, activity: Activity) {
            titleView.text = title
            applicationList.adapter = SmallApplicationListAdapter(activity, apps)
        }
    }
}