/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.databinding.ActivityMainBinding
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent?.data

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (data != null && data.path == "/store/apps/details") {
            val pkgName = data.toString().split("?id=")[1]

            val bundle = Bundle()
            bundle.putString("id", "")
            bundle.putString("packageName", pkgName)
            bundle.putSerializable("origin", Origin.GPLAY)

            val navGraph = navController.navInflater.inflate(R.navigation.navigation_resource)
            navGraph.startDestination = R.id.applicationFragment
            navController.setGraph(navGraph, bundle)
        } else {
            navController.setGraph(R.navigation.navigation_resource)
        }

        bottomNavigationView.setupWithNavController(navController)

        val viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        // Watch and refresh authentication data
        viewModel.authDataJson.observe(this, {
            if (it.isNullOrEmpty()) {
                Log.d(TAG, "Fetching new authentication data")
                viewModel.getAuthData()
            } else {
                viewModel.generateAuthData()
                Log.d(TAG, "Authentication data is available!")
            }
        })

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.applicationFragment, R.id.applicationListFragment, R.id.screenshotFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }
}
