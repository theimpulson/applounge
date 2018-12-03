package io.eelo.appinstaller.home

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import io.eelo.appinstaller.R
import kotlinx.android.synthetic.main.home_category_list_item.view.*

class HomeCategory(context: Context, categoryTitle: String) : LinearLayout(context) {

    init {
        View.inflate(context, R.layout.home_category_list_item, this)
        category_title.text = categoryTitle
    }
}
