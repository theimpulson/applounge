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
import foundation.e.apps.databinding.ActivityMainBinding
import foundation.e.apps.updates.UpdatesNotifier
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
import foundation.e.apps.utils.modules.CommonUtilsModule

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

        var hasInternet = true

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

        viewModel.internetConnection.observe(this) { isInternetAvailable ->
            hasInternet = isInternetAvailable
            if (isInternetAvailable) {
                binding.noInternet.visibility = View.GONE
                binding.fragment.visibility = View.VISIBLE

                viewModel.userType.observe(this) { user ->
                    if (user.isNotBlank() && viewModel.tocStatus.value == true) {
                        when (User.valueOf(user)) {
                            User.ANONYMOUS -> {
                                if (viewModel.authDataJson.value.isNullOrEmpty() && !viewModel.authRequestRunning) {
                                    Log.d(TAG, "Fetching new authentication data")
                                    viewModel.getAuthData()
                                }
                            }
                            User.UNAVAILABLE -> {
                                viewModel.destroyCredentials()
                                navController.navigate(R.id.signInFragment, null, navOptions)
                            }
                            User.GOOGLE -> {
                            }
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
            if (!hasInternet) {
                showNoInternet()
            }

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

        if (intent.hasExtra(UpdatesNotifier.UPDATES_NOTIFICATION_CLICK_EXTRA)) {
            bottomNavigationView.selectedItemId = R.id.updatesFragment
        }

        // Create notification channel on post-nougat devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            viewModel.createNotificationChannels()
        }

        // Observe and handle downloads
        viewModel.downloadList.observe(this) { list ->
            val shouldDownload = list.any {
                it.status == Status.INSTALLING || it.status == Status.DOWNLOADING
            }
            if (!shouldDownload && list.isNotEmpty()) {
                for (item in list) {
                    if (item.status == Status.QUEUED) {
                        viewModel.downloadApp(item)
                        break
                    }
                }
            }
        }

        if (!CommonUtilsModule.isNetworkAvailable(this)) {
            showNoInternet()
        }
    }

    private fun showNoInternet() {
        binding.noInternet.visibility = View.VISIBLE
        binding.fragment.visibility = View.GONE
    }
}
