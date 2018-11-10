package io.eelo.appinstaller.categories

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import io.eelo.appinstaller.R
import io.eelo.appinstaller.categories.viewmodel.CategoriesViewModel
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

        applicationsCategoriesList = view.findViewById(R.id.applications_categories_list)
        gamesCategoriesList = view.findViewById(R.id.games_categories_list)

        categoriesViewModel = ViewModelProviders.of(activity!!).get(CategoriesViewModel::class.java)
        categoriesViewModel.loadCategories()

        categoriesContainer = view.findViewById(R.id.categories_container)
        categoriesContainer.visibility = View.GONE
        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.visibility = View.VISIBLE

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
            textView.layoutParams = itemParams
            textView.width = itemWidth
            textView.setPadding(itemPadding, itemPadding, itemPadding, itemPadding)
            textView.text = it.title
            textView.textSize = 16.0f
            textView.gravity = Gravity.CENTER
            textView.maxLines = 1
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.isClickable = true
            textView.foreground = activity!!.getDrawable(R.drawable.app_category_border)
            val outValue = TypedValue()
            context!!.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            textView.setBackgroundResource(outValue.resourceId)
            applicationsCategoriesList.addView(textView)
            textView.setOnClickListener { _ ->
                categoriesViewModel.onCategoryClick(context!!, it, false)
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
            textView.foreground = activity!!.getDrawable(R.drawable.app_category_border)
            val outValue = TypedValue()
            context!!.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            textView.setBackgroundResource(outValue.resourceId)
            gamesCategoriesList.addView(textView)
            textView.setOnClickListener { _ ->
                categoriesViewModel.onCategoryClick(context!!, it, true)
            }
            categoriesContainer.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
    }
}
