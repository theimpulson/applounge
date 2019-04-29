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
