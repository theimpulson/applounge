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

//import java.awt.font.ShapeGraphicAttribute.STROKE
//import java.awt.AlphaComposite.SRC_IN
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.common.SmallApplicationListAdapter
import foundation.e.apps.home.viewmodel.HomeViewModel


class HomeFragment : Fragment() {
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var imageCarousel: ViewPager
    private lateinit var divider: View
    private lateinit var categoryList: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var applicationManager: ApplicationManager? = null
    var accentColorOS=0;

    fun initialise(applicationManager: ApplicationManager, accentColorOS: Int) {
        this.applicationManager = applicationManager
        this.accentColorOS=accentColorOS;
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (applicationManager == null) {
            return null

        }

        val view = inflater.inflate(R.layout.fragment_home, container, false)
       /* if(accentColorOS!=0){

            view.findViewById<TextView>(R.id.tv_featured).setTextColor(accentColorOS);
        }*/

        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        imageCarousel = view.findViewById(R.id.image_carousel)
        divider = view.findViewById(R.id.divider)
        categoryList = view.findViewById(R.id.category_list)
        progressBar = view.findViewById(R.id.progress_bar)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)
        //set accent color to Error button (Retry )

        view.findViewById<TextView>(R.id.error_resolve).setTextColor(Color.parseColor("#ffffff"))
        view.findViewById<TextView>(R.id.error_resolve).setBackgroundColor(accentColorOS)

        // Initialise UI elements
        homeViewModel.initialise(applicationManager!!)
        setCustomScroller()
        imageCarousel.visibility = View.INVISIBLE
        divider.visibility = View.INVISIBLE
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
                imageCarousel.clipToPadding = false;
                imageCarousel.setPadding(170, 10, 170, 10);
                imageCarousel.pageMargin =50
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
                divider.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        })

        // Bind to the screen error
        homeViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = activity!!.getString(it.description)
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

    private fun showCategories(categories: LinkedHashMap<Category, ArrayList<Application>>) {
        categoryList.removeAllViews()
        categories.forEach {
            val homeCategory = HomeCategory(context!!, it.key)
            val applicationList = homeCategory.findViewById<RecyclerView>(R.id.application_list)
            applicationList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            applicationList.adapter = SmallApplicationListAdapter(activity!!, it.value)
            categoryList.addView(homeCategory)
        }
    }

    override fun onResume() {
        super.onResume()
        if (::homeViewModel.isInitialized) {
            homeViewModel.getCategories().value!!.values.forEach {
                it.forEach { application ->
                    application.checkForStateUpdate(context!!)
                }
            }
        }
    }

    fun decrementApplicationUses() {
        if (::homeViewModel.isInitialized) {
            homeViewModel.getCategories().value!!.forEach {
                it.value.forEach { application ->
                    application.decrementUses()
                }
            }
            homeViewModel.getBannerApplications().value!!.forEach {
                if(it.application!=null)
                    it.application.decrementUses()
            }
        }
    }
}
