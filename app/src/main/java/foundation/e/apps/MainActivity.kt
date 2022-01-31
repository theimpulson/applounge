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

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.databinding.ActivityMainBinding
import foundation.e.apps.utils.USER

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        val viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        // navOptions and activityNavController for TOS and SignIn Fragments
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.navigation_resource, true)
            .build()
        navOptions.shouldLaunchSingleTop()

        viewModel.tocStatus.observe(this) {
            if (it != true) {
                navController.navigate(R.id.TOSFragment, null, navOptions)
            }
        }

        viewModel.userType.observe(this) { user ->
            if (user.isNotBlank() && viewModel.tocStatus.value == true) {
                when (USER.valueOf(user)) {
                    USER.ANONYMOUS -> {
                        if (viewModel.authDataJson.value.isNullOrEmpty() && !viewModel.authRequestRunning) {
                            Log.d(TAG, "Fetching new authentication data")
                            viewModel.getAuthData()
                        }
                    }
                    USER.UNAVAILABLE -> {
                        viewModel.destroyCredentials()
                        navController.navigate(R.id.signInFragment, null, navOptions)
                    }
                    USER.GOOGLE -> {}
                }
            }
        }

        // Watch and refresh authentication data
        viewModel.authDataJson.observe(this) {
            if (!it.isNullOrEmpty()) {
                viewModel.generateAuthData()
                Log.d(TAG, "Authentication data is available!")
            }
        }

        viewModel.authValidity.observe(this) {
            if (it != true) {
                Log.d(TAG, "Authentication data validation failed!")
                viewModel.destroyCredentials()
            } else {
                Log.d(TAG, "Authentication data is valid!")
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.applicationFragment,
                R.id.applicationListFragment,
                R.id.screenshotFragment,
                R.id.descriptionFragment,
                R.id.TOSFragment,
                R.id.googleSignInFragment,
                R.id.signInFragment -> {
                    bottomNavigationView.visibility = View.GONE
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }

        // Create notification channel on post-nougat devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.createNotificationChannels()
        }

        // Observe and handle downloads
        viewModel.downloadList.observe(this) {
            val installInProgress = it.any { app ->
                app.status == Status.DOWNLOADING || app.status == Status.INSTALLING
            }
            if (!installInProgress && it.isNotEmpty()) {
                for (item in it) {
                    if (item.status == Status.QUEUED) {
                        viewModel.downloadAndInstallApp(item)
                        break
                    }
                }
            }
        }
    }
}
