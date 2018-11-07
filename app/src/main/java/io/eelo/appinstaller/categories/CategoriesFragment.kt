package io.eelo.appinstaller.categories

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.categories.viewModel.CategoriesViewModel
import android.util.TypedValue
import android.widget.ProgressBar
import io.eelo.appinstaller.application.model.InstallManager

class CategoriesFragment : Fragment() {

    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var applicationsCategoriesList: LinearLayout
    private lateinit var gamesCategoriesList: LinearLayout
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private var installManager: InstallManager? = null

    fun initialise(installManager: InstallManager) {
        this.installManager = installManager
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        applicationsCategoriesList = view.findViewById(R.id.applications_categories_list)
        gamesCategoriesList = view.findViewById(R.id.games_categories_list)

        categoriesViewModel = ViewModelProviders.of(activity!!).get(CategoriesViewModel::class.java)
        categoriesViewModel.loadCategories()

        categoriesContainer = view.findViewById(R.id.categories_container)
        categoriesContainer.visibility = View.GONE
        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

        // Bind to the list of applications categories
        categoriesViewModel.getApplicationsCategories().observe(this, Observer {
            showApplicationsCategories()
        })

        // Bind to the list of games categories
        categoriesViewModel.getGamesCategories().observe(this, Observer {
            showGamesCategories()
        })

        return view
    }

    private fun showApplicationsCategories() {
        applicationsCategoriesList.removeAllViews()
        categoriesViewModel.getApplicationsCategories().value!!.forEach {
            val textView = TextView(context)
            textView.text = it.title
            val scale = resources.displayMetrics.density
            val medium = (8 * scale + 0.5f).toInt()
            val large = (16 * scale + 0.5f).toInt()
            textView.setPadding(medium, large, medium, large)
            textView.textSize = 16.0f
            textView.isClickable = true
            val outValue = TypedValue()
            context!!.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            textView.setBackgroundResource(outValue.resourceId)
            applicationsCategoriesList.addView(textView)
            textView.setOnClickListener { _ ->
                categoriesViewModel.onCategoryClick(context!!, it)
            }
            categoriesContainer.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }

    private fun showGamesCategories() {
        gamesCategoriesList.removeAllViews()
        categoriesViewModel.getGamesCategories().value!!.forEach {
            val textView = TextView(context)
            textView.text = it.title
            val scale = resources.displayMetrics.density
            val medium = (8 * scale + 0.5f).toInt()
            val large = (16 * scale + 0.5f).toInt()
            textView.setPadding(medium, large, medium, large)
            textView.textSize = 16.0f
            textView.isClickable = true
            val outValue = TypedValue()
            context!!.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            textView.setBackgroundResource(outValue.resourceId)
            gamesCategoriesList.addView(textView)
            textView.setOnClickListener { _ ->
                categoriesViewModel.onCategoryClick(context!!, it)
            }
            categoriesContainer.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}
