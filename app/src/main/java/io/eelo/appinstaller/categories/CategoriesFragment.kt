package io.eelo.appinstaller.categories

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.Configuration
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import io.eelo.appinstaller.R
import io.eelo.appinstaller.categories.viewmodel.CategoriesViewModel
import io.eelo.appinstaller.utils.Common
import kotlin.math.roundToInt

class CategoriesFragment : Fragment() {

    private lateinit var categoriesViewModel: CategoriesViewModel
    private lateinit var applicationsCategoriesList: GridLayout
    private lateinit var gamesCategoriesList: GridLayout
    private lateinit var categoriesContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var itemParams: LinearLayout.LayoutParams
    private var itemWidth = 0
    private var itemPadding = 0
    private var itemMargin = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        categoriesViewModel = ViewModelProviders.of(activity!!).get(CategoriesViewModel::class.java)
        applicationsCategoriesList = view.findViewById(R.id.applications_categories_list)
        gamesCategoriesList = view.findViewById(R.id.games_categories_list)
        categoriesContainer = view.findViewById(R.id.categories_container)
        progressBar = view.findViewById(R.id.progress_bar)
        val errorContainer = view.findViewById<LinearLayout>(R.id.error_container)
        val errorDescription = view.findViewById<TextView>(R.id.error_description)

        // Initialise UI elements
        initialiseDimensions()
        handleDeviceOrientation()
        categoriesContainer.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        errorContainer.visibility = View.GONE
        view.findViewById<TextView>(R.id.error_resolve).setOnClickListener {
            progressBar.visibility = View.VISIBLE
            categoriesViewModel.loadCategories(context!!)
        }

        // Bind to the list of applications categories
        categoriesViewModel.getApplicationsCategories().observe(this, Observer {
            showApplicationsCategories()
        })

        // Bind to the list of games categories
        categoriesViewModel.getGamesCategories().observe(this, Observer {
            showGamesCategories()
        })

        // Bind to the screen error
        categoriesViewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                errorDescription.text = activity!!.getString(Common.getScreenErrorDescriptionId(it))
                errorContainer.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                errorContainer.visibility = View.GONE
            }
        })

        if (categoriesViewModel.getApplicationsCategories().value!!.isEmpty() ||
                categoriesViewModel.getGamesCategories().value!!.isEmpty()) {
            categoriesViewModel.loadCategories(context!!)
        }
        return view
    }

    private fun initialiseDimensions() {
        // Do some math and figure out item width, padding and margin
        val metrics = DisplayMetrics()
        activity!!.windowManager.defaultDisplay.getMetrics(metrics)
        val logicalDensity = metrics.density

        itemWidth = Math.ceil(160 * logicalDensity.toDouble()).roundToInt()
        itemPadding = Math.ceil(8 * logicalDensity.toDouble()).roundToInt()
        itemMargin = Math.ceil(4 * logicalDensity.toDouble()).roundToInt()

        itemParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        itemParams.topMargin = itemPadding
        itemParams.bottomMargin = itemPadding
        itemParams.marginStart = itemPadding
        itemParams.marginEnd = itemPadding
    }

    private fun handleDeviceOrientation() {
        // Check device orientation and increase/decrease number of columns
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // In landscape
            applicationsCategoriesList.columnCount = 3
            gamesCategoriesList.columnCount = 3
        } else {
            // In portrait
            applicationsCategoriesList.columnCount = 2
            gamesCategoriesList.columnCount = 2
        }
    }

    private fun showApplicationsCategories() {
        applicationsCategoriesList.removeAllViews()
        categoriesViewModel.getApplicationsCategories().value!!.forEach {
            val textView = TextView(context)
            textView.layoutParams = itemParams
            textView.width = itemWidth
            textView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding)
            textView.text = it.title
            textView.textSize = 16.0f
            textView.gravity = Gravity.CENTER
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.isClickable = true
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                textView.foreground = activity!!.getDrawable(R.drawable.app_category_border)
            }
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
            textView.layoutParams = itemParams
            textView.width = itemWidth
            textView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding)
            textView.text = it.title
            textView.textSize = 16.0f
            textView.gravity = Gravity.CENTER
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.isClickable = true
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                textView.foreground = activity!!.getDrawable(R.drawable.app_category_border)
            }
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
