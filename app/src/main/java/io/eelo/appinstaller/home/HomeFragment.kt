package io.eelo.appinstaller.home

import  android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.home.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var imageCarousel: ViewPager
    lateinit var installManager: InstallManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
        imageCarousel = view.findViewById(R.id.image_carousel)
        imageCarousel.visibility = View.GONE
        reduceCarouselsScrollingAnimationSpeed()

        val categoryList = view.findViewById<RecyclerView>(R.id.category_list)
        categoryList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        homeViewModel.load({
            createImageCarousel()
            createCategoryList(categoryList)
        }, context!!, installManager)

        return view
    }

    private fun reduceCarouselsScrollingAnimationSpeed() {
        val scroller = ViewPager::class.java.getDeclaredField("mScroller")
        scroller.isAccessible = true
        scroller.set(imageCarousel, ImageCarouselScroller(context!!))
    }

    private fun createImageCarousel() {
        val bannerImages = homeViewModel.getCarouselImages()

        imageCarousel.visibility = View.VISIBLE
        imageCarousel.adapter = ImageCarouselAdapter(context!!, bannerImages)
        imageCarousel.setCurrentItem(0, false)
        ImageCarouselSwitcher(bannerImages.size, imageCarousel).start()
    }

    private fun createCategoryList(categoryList: RecyclerView) {
        val apps = homeViewModel.getApplications()
        categoryList.adapter = HomeCategoryAdapter(activity!!, apps)
    }

}
