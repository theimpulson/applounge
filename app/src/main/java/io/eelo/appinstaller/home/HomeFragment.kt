package io.eelo.appinstaller.home

import android.arch.lifecycle.Observer
import  android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.common.SmallApplicationListAdapter
import io.eelo.appinstaller.home.viewmodel.HomeViewModel
import io.eelo.appinstaller.utils.Common

class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var imageCarousel: ViewPager
    private lateinit var categoryList: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var installManager: InstallManager? = null

    fun initialise(installManager: InstallManager) {
        this.installManager = installManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (installManager == null) {
            return null
        }

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        homeViewModel = ViewModelProviders.of(activity!!).get(HomeViewModel::class.java)
        imageCarousel = view.findViewById(R.id.image_carousel)
        categoryList = view.findViewById(R.id.category_list)
        progressBar = view.findViewById(R.id.progress_bar)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)

        // Initialise UI elements
        homeViewModel.initialise(installManager!!)
        setCustomScroller()
        imageCarousel.visibility = View.GONE
        categoryList.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            homeViewModel.loadCategories(context!!)
        }

        // Bind image carousel adapter to banner images in view model
        homeViewModel.getBannerApplications().observe(this, Observer {
            if (homeViewModel.getBannerApplications().value!!.isNotEmpty()) {
                imageCarousel.adapter = ImageCarouselAdapter(activity!!, homeViewModel.getBannerApplications().value!!)
                imageCarousel.setCurrentItem(0, false)
                imageCarousel.visibility = View.VISIBLE
                // Automatically switch between images for one round
                ImageCarouselSwitcher(homeViewModel.getBannerApplications().value!!.size, imageCarousel).start()
            }
        })

        // Bind categories adapter to categories in view model
        homeViewModel.getCategories().observe(this, Observer {
            if (homeViewModel.getCategories().value!!.isNotEmpty()) {
                showCategories(it!!)
                categoryList.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        })

        // Bind to the screen error
        homeViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = activity!!.getString(Common.getScreenErrorDescriptionId(it))
                errorContainer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        if (homeViewModel.getBannerApplications().value!!.isEmpty() ||
                homeViewModel.getCategories().value!!.isEmpty()) {
            homeViewModel.loadCategories(context!!)
        }
        return view
    }

    private fun setCustomScroller() {
        val scroller = ViewPager::class.java.getDeclaredField("mScroller")
        scroller.isAccessible = true
        scroller.set(imageCarousel, ImageCarouselScroller(context!!))
    }

    private fun showCategories(categories: LinkedHashMap<String, ArrayList<Application>>) {
        categoryList.removeAllViews()
        categories.forEach {
            val homeCategory = HomeCategory(context!!, it.key)
            val applicationList = homeCategory.findViewById<RecyclerView>(R.id.application_list)
            applicationList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            applicationList.adapter = SmallApplicationListAdapter(activity!!, it.value)
            categoryList.addView(homeCategory)
        }
    }

    override fun onDestroy() {
        if (::categoryList.isInitialized) {
            homeViewModel.getCategories().value!!.forEach {
                it.value.forEach { application ->
                    application.decrementUses()
                }
            }
        }
        super.onDestroy()
    }
}
