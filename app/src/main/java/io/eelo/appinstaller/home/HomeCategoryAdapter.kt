package io.eelo.appinstaller.home

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.common.SmallApplicationListAdapter
import kotlinx.android.synthetic.main.home_category_list_item.view.*

class HomeCategoryAdapter(private val activity: Activity, private val categoryHashMap: HashMap<Category, ArrayList<Application>>) : RecyclerView.Adapter<HomeCategoryAdapter.HomeCategoryViewHolder>() {

    private val categoryList = categoryHashMap.keys.toCollection(ArrayList())

    class HomeCategoryViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.category_title
        val applicationList: RecyclerView = view.application_list
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeCategoryViewHolder {
        val listItemContainer = LayoutInflater.from(parent.context).inflate(R.layout.home_category_list_item, parent, false)
        return HomeCategoryViewHolder(listItemContainer)
    }

    override fun onBindViewHolder(holder: HomeCategoryViewHolder, position: Int) {
        holder.title.text = categoryList[position].title
        holder.applicationList.layoutManager = LinearLayoutManager(holder.view.context, LinearLayoutManager.HORIZONTAL, false)
        holder.applicationList.adapter = SmallApplicationListAdapter(activity, categoryHashMap[categoryList[position]]!!.toList())
    }

    override fun getItemCount() = categoryHashMap.size
}
