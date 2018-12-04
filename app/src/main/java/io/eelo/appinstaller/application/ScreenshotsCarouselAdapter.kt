package io.eelo.appinstaller.application

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import io.eelo.appinstaller.R
import kotlinx.android.synthetic.main.screenshots_carousel_item.view.*

class ScreenshotsCarouselAdapter(context: Context, private val screenshots: List<Bitmap>) : PagerAdapter() {

    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj as LinearLayout
    }

    override fun getCount(): Int {
        return screenshots.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = layoutInflater.inflate(R.layout.screenshots_carousel_item, container, false)

        view.image.setImageBitmap(screenshots[position])

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as LinearLayout)
    }
}
