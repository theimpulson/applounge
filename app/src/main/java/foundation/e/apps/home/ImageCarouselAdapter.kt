/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.home

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import foundation.e.apps.R
import foundation.e.apps.application.viewmodel.ApplicationViewModel
import foundation.e.apps.home.model.BannerApplication
import kotlinx.android.synthetic.main.image_carousel_item.view.*

class ImageCarouselAdapter(context: Context, private val bannerApplications: ArrayList<BannerApplication>) : PagerAdapter() {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val applicationViewModel = ApplicationViewModel()

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj as LinearLayout
    }

    override fun getCount(): Int {
        return bannerApplications.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.image_carousel_item, container, false)
        val wideImage = view.image

        wideImage.setImageBitmap(bannerApplications[position].image)
        wideImage.setOnClickListener {
            applicationViewModel.onApplicationClick(view.context, bannerApplications[position].application)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }
}
