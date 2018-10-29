package io.eelo.appinstaller.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.eelo.appinstaller.R
import io.eelo.appinstaller.home.viewmodel.HomeViewModel
import android.widget.Scroller

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var imageCarousel: ViewPager
    private val imagesList = ArrayList<Bitmap>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
        imageCarousel = view.findViewById(R.id.image_carousel)
        imageCarousel.visibility = View.GONE
        val imageCarouselAdapter = ImageCarouselAdapter(context!!, imagesList)
        val categoryList = view.findViewById<RecyclerView>(R.id.category_list)

        // Initialise the image carousel
        val scroller = ViewPager::class.java.getDeclaredField("mScroller")
        scroller.isAccessible = true
        val imageCarouselScroller = ImageCarouselScroller(context!!)
        scroller.set(imageCarousel, imageCarouselScroller)
        imageCarousel.adapter = imageCarouselAdapter
        homeViewModel.loadCarouselImages()

        // Initialise category list
        categoryList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        categoryList.adapter = HomeCategoryAdapter(activity!!, homeViewModel.getApplications().value!!)
        homeViewModel.loadApplications()

        // Bind to the list of images for the carousel
        homeViewModel.getCarouselImages().observe(this, Observer {
            imagesList.clear()
            imagesList.addAll(homeViewModel.getCarouselImages().value!!)
            imageCarouselAdapter.notifyDataSetChanged()
            imageCarousel.visibility = View.VISIBLE
            imageCarousel.setCurrentItem(0, false)
            ImageCarouselTimer(((imageCarouselAdapter.count - 1) * 4000).toLong(), 4000).start()
        })

        // Bind to the list of applications
        homeViewModel.getApplications().observe(this, Observer {
            categoryList.adapter = HomeCategoryAdapter(activity!!, homeViewModel.getApplications().value!!)
        })

        return view
    }

    private inner class ImageCarouselTimer(private var millisInFuture: Long, private val countDownInterval: Long) {
        fun start() {
            val handler = Handler()
            val counter = object : Runnable {
                override fun run() {
                    if (millisInFuture <= 0) {
                        imageCarousel.setCurrentItem(0, true)
                    } else {
                        imageCarousel.setCurrentItem(imageCarousel.currentItem + 1, true)
                        millisInFuture -= countDownInterval
                        handler.postDelayed(this, countDownInterval)
                    }
                }
            }
            handler.postDelayed(counter, countDownInterval)
        }
    }

    // Custom scroller to reduce scrolling animation speed of view pager
    private inner class ImageCarouselScroller(context: Context) : Scroller(context) {

        private val mDuration = 2000

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
            super.startScroll(startX, startY, dx, dy, mDuration)
        }

        override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int) {
            super.startScroll(startX, startY, dx, dy, mDuration)
        }
    }
}
