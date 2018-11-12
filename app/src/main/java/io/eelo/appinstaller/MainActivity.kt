package io.eelo.appinstaller

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.InstallManagerGetter
import io.eelo.appinstaller.categories.CategoriesFragment
import io.eelo.appinstaller.home.HomeFragment
import io.eelo.appinstaller.search.SearchFragment
import io.eelo.appinstaller.settings.SettingsFragment
import io.eelo.appinstaller.updates.UpdatesFragment
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants.CURRENTLY_SELECTED_FRAGMENT_KEY
import io.eelo.appinstaller.utils.Constants.STORAGE_PERMISSION_REQUEST_CODE
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var currentFragmentId = 0
    private val homeFragment = HomeFragment()
    private val categoriesFragment = CategoriesFragment()
    private val searchFragment = SearchFragment()
    private val updatesFragment = UpdatesFragment()
    private val settingsFragment = SettingsFragment()
    private val installManagerGetter = InstallManagerGetter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Common.EXECUTOR.submit {
            val installManager = installManagerGetter.connectAndGet(this)
            initialiseFragments(installManager)
            // Show the home fragment by default
            if (savedInstanceState != null) {
                if (selectFragment(savedInstanceState.getInt(CURRENTLY_SELECTED_FRAGMENT_KEY))) {
                    currentFragmentId = savedInstanceState.getInt(CURRENTLY_SELECTED_FRAGMENT_KEY)
                }
            } else {
                if (selectFragment(R.id.menu_home)) {
                    currentFragmentId = R.id.menu_home
                }
            }
        }

        bottom_navigation_view.setOnNavigationItemSelectedListener(this)
        disableShiftingOfNabBarItems()
    }

    private fun initialiseFragments(installManager: InstallManager) {
        homeFragment.initialise(installManager)
        searchFragment.initialise(installManager)
        updatesFragment.initialise(installManager)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        if (selectFragment(item.itemId)) {
            currentFragmentId = item.itemId
            return true
        }
        return false
    }

    private fun selectFragment(fragmentId: Int): Boolean {
        when (fragmentId) {
            R.id.menu_home -> {
                showFragment(homeFragment)
                return true
            }
            R.id.menu_categories -> {
                showFragment(categoriesFragment)
                return true
            }
            R.id.menu_search -> {
                showFragment(searchFragment)
                return true
            }
            R.id.menu_updates -> {
                showFragment(updatesFragment)
                return true
            }
            R.id.menu_settings -> {
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
    }

    @SuppressLint("RestrictedApi")
    private fun disableShiftingOfNabBarItems() {
        val menuView = bottom_navigation_view.getChildAt(0) as BottomNavigationMenuView
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, resources.getString(R.string.error_storage_permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(CURRENTLY_SELECTED_FRAGMENT_KEY, currentFragmentId)
    }

    override fun onDestroy() {
        super.onDestroy()
        installManagerGetter.disconnect(this)
    }
}
