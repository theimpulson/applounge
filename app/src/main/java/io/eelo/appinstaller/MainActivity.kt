package io.eelo.appinstaller

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.app.Fragment
import android.view.MenuItem
import io.eelo.appinstaller.categories.CategoriesFragment
import io.eelo.appinstaller.home.HomeFragment
import io.eelo.appinstaller.search.SearchFragment
import io.eelo.appinstaller.settings.SettingsFragment
import io.eelo.appinstaller.updates.UpdatesFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var currentFragment: Fragment? = null
    private val homeFragment = HomeFragment()
    private val categoriesFragment = CategoriesFragment()
    private val searchFragment = SearchFragment()
    private val updatesFragment = UpdatesFragment()
    private val settingsFragment = SettingsFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show the home fragment by default
        showFragment(homeFragment)

        bottom_navigation_view.setOnNavigationItemSelectedListener(this)

        // Disable shifting of nav bar items
        removeShiftMode(bottom_navigation_view)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == R.id.menu_home -> {
                showFragment(homeFragment)
                return true
            }
            item.itemId == R.id.menu_categories -> {
                showFragment(categoriesFragment)
                return true
            }
            item.itemId == R.id.menu_search -> {
                showFragment(searchFragment)
                return true
            }
            item.itemId == R.id.menu_updates -> {
                showFragment(updatesFragment)
                return true
            }
            item.itemId == R.id.menu_settings -> {
                showFragment(settingsFragment)
                return true
            }
        }
        return false
    }

    private fun showFragment(fragment: Fragment) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, fragment)
                .commit()
        currentFragment = fragment
    }

    @SuppressLint("RestrictedApi")
    private fun removeShiftMode(bottomNavigationView: BottomNavigationView) {
        val menuView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        try {
            val mShiftingMode = menuView.javaClass.getDeclaredField("mShiftingMode")
            mShiftingMode.isAccessible = true
            mShiftingMode.setBoolean(menuView, false)
            mShiftingMode.isAccessible = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        for (i in 0 until menuView.childCount) {
            val itemView = menuView.getChildAt(i) as BottomNavigationItemView
            itemView.setShiftingMode(false)
            itemView.setChecked(itemView.itemData.isChecked)
        }
    }

}
