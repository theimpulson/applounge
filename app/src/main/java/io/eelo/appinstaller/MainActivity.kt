package io.eelo.appinstaller

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.support.design.internal.BottomNavigationItemView
import android.support.design.internal.BottomNavigationMenuView
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.InstallManagerService
import io.eelo.appinstaller.categories.CategoriesFragment
import io.eelo.appinstaller.home.HomeFragment
import io.eelo.appinstaller.search.SearchFragment
import io.eelo.appinstaller.settings.SettingsFragment
import io.eelo.appinstaller.updates.UpdatesFragment
import io.eelo.appinstaller.utils.Constants.STORAGE_PERMISSION_REQUEST_CODE
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {
    private var currentFragment: Fragment? = null
    private val homeFragment = HomeFragment()
    private val categoriesFragment = CategoriesFragment()
    private val searchFragment = SearchFragment()
    private val updatesFragment = UpdatesFragment()
    private val settingsFragment = SettingsFragment()
    private lateinit var serviceConnection: ServiceConnection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread {
            val installManager = createInstallManager()
            initialiseFragments(installManager)
            // Show the home fragment by default
            showFragment(homeFragment)
        }.start()

        bottom_navigation_view.setOnNavigationItemSelectedListener(this)

        // Disable shifting of nav bar items
        removeShiftMode(bottom_navigation_view)
    }

    private fun initialiseFragments(installManager: InstallManager) {
        homeFragment.installManager = installManager
        searchFragment.initialise(installManager)
        updatesFragment.initialise(installManager)
    }

    private fun createInstallManager(): InstallManager {
        startService(Intent(this, InstallManagerService::class.java))
        val blocker = Object()
        var installManager: InstallManager? = null
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                Messenger(service).send(Message.obtain(null, 0, { result: InstallManager ->
                    installManager = result
                    synchronized(blocker) {
                        blocker.notify()
                    }
                }))
            }

            override fun onServiceDisconnected(name: ComponentName) {}
        }
        bindService(Intent(this, InstallManagerService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
        synchronized(blocker) {
            blocker.wait()
        }
        return installManager!!
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, resources.getString(R.string.error_storage_permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }
}
