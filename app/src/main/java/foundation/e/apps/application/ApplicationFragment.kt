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

package foundation.e.apps.application

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.application.model.ApplicationScreenshotsRVAdapter
import foundation.e.apps.databinding.FragmentApplicationBinding
import foundation.e.apps.utils.pkg.PkgManagerModule
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationFragment : Fragment(R.layout.fragment_application) {

    private val args: ApplicationFragmentArgs by navArgs()
    private val TAG = ApplicationFragment::class.java.simpleName

    private var _binding: FragmentApplicationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    private val applicationViewModel: ApplicationViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApplicationBinding.bind(view)

        mainActivityViewModel.authData.observe(viewLifecycleOwner, {
            applicationViewModel.getApplicationDetails(
                args.id,
                args.packageName,
                it,
                args.origin
            )
        })

        if (args.origin == Origin.GPLAY) {
            binding.toolbar.inflateMenu(R.menu.application_menu)
        }

        val startDestination = findNavController().graph.startDestination
        if (startDestination == R.id.applicationFragment) {
            binding.toolbar.setNavigationOnClickListener {
                val action = ApplicationFragmentDirections.actionApplicationFragmentToHomeFragment()
                view.findNavController().navigate(action)
            }
        } else {
            binding.toolbar.setNavigationOnClickListener {
                view.findNavController().navigateUp()
            }
        }

        val notAvailable = getString(R.string.not_available)

        val screenshotsRVAdapter = ApplicationScreenshotsRVAdapter(args.origin)
        binding.recyclerView.apply {
            adapter = screenshotsRVAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        binding.applicationLayout.visibility = View.INVISIBLE

        applicationViewModel.appStatus.observe(viewLifecycleOwner, { status ->
            val installButton = binding.downloadInclude.installButton
            val downloadPB = binding.downloadInclude.appInstallPB
            val appSize = binding.downloadInclude.appSize
            val fusedApp = applicationViewModel.fusedApp.value

            fusedApp?.let {
                when (status) {
                    Status.INSTALLED -> {
                        installButton.text = getString(R.string.open)
                        installButton.setOnClickListener { _ ->
                            startActivity(pkgManagerModule.getLaunchIntent(it.package_name))
                        }
                    }
                    Status.UPDATABLE -> {
                        installButton.text = getString(R.string.update)
                        installButton.setOnClickListener { _ ->
                            mainActivityViewModel.authData.value?.let { data ->
                                applicationViewModel.getApplication(data, it, args.origin)
                            }
                        }
                    }
                    Status.UNAVAILABLE -> {
                        installButton.setOnClickListener { _ ->
                            mainActivityViewModel.authData.value?.let { data ->
                                applicationViewModel.getApplication(data, it, args.origin)
                            }
                        }
                    }
                    Status.DOWNLOADING -> {
                        installButton.text = getString(R.string.cancel)
                        downloadPB.visibility = View.VISIBLE
                        appSize.visibility = View.GONE
                    }
                    Status.INSTALLING, Status.UNINSTALLING -> {
                        downloadPB.visibility = View.GONE
                        appSize.visibility = View.VISIBLE
                        installButton.text = getString(R.string.cancel)
                        installButton.isEnabled = false
                    }
                }
            }
        })

        applicationViewModel.fusedApp.observe(viewLifecycleOwner, {
            applicationViewModel.appStatus.value = it.status
            screenshotsRVAdapter.setData(it.other_images_path)

            if (it.shareUrl.isNotBlank()) {
                binding.toolbar.setOnMenuItemClickListener { menu ->
                    when (menu.itemId) {
                        R.id.share -> {
                            val intent = Intent.createChooser(shareApp(it.name, it.shareUrl), null)
                            startActivity(intent)
                            true
                        }
                        else -> false
                    }
                }
            }

            // Title widgets
            binding.titleInclude.apply {
                appName.text = it.name
                appAuthor.text = it.author
                categoryTitle.text = it.category

                if (args.origin == Origin.CLEANAPK) {
                    appIcon.load(CleanAPKInterface.ASSET_URL + it.icon_image_path)
                } else {
                    appIcon.load(it.icon_image_path)
                }
            }

            // Ratings widgets
            binding.ratingsInclude.apply {
                if (it.ratings.usageQualityScore != -1.0) {
                    appRating.text =
                        getString(R.string.rating_out_of, it.ratings.usageQualityScore.toString())
                }
                appRatingLayout.setOnClickListener {
                    ApplicationDialogFragment(
                        R.drawable.ic_star,
                        getString(R.string.rating),
                        getString(R.string.rating_description)
                    ).show(childFragmentManager, TAG)
                }

                if (it.ratings.privacyScore != -1.0) {
                    appPrivacyScore.text = getString(
                        R.string.privacy_rating_out_of,
                        it.ratings.privacyScore.toString()
                    )
                }
                appPrivacyScoreLayout.setOnClickListener {
                    ApplicationDialogFragment(
                        R.drawable.ic_lock,
                        getString(R.string.privacy),
                        getString(R.string.privacy_description)
                    ).show(childFragmentManager, TAG)
                }

                appEnergyRatingLayout.setOnClickListener {
                    ApplicationDialogFragment(
                        R.drawable.ic_energy,
                        getString(R.string.energy),
                        getString(R.string.energy_description)
                    ).show(childFragmentManager, TAG)
                }
            }

            binding.appDescription.text =
                Html.fromHtml(it.description, Html.FROM_HTML_MODE_COMPACT)

            // Information widgets
            binding.infoInclude.apply {
                appUpdatedOn.text = getString(
                    R.string.updated_on,
                    if (args.origin == Origin.CLEANAPK) it.last_modified.split(" ")[0] else it.last_modified
                )
                appRequires.text = getString(R.string.min_android_version, notAvailable)
                appVersion.text = getString(
                    R.string.version,
                    if (it.latest_version_number == "-1") notAvailable else it.latest_version_number
                )
                appLicense.text = getString(
                    R.string.license,
                    if (it.licence.isBlank() or (it.licence == "unknown")) notAvailable else it.licence
                )
                appPackageName.text = getString(R.string.package_name, it.package_name)
            }

            // Privacy widgets
            binding.privacyInclude.apply {
                appPermissions.setOnClickListener { _ ->
                    ApplicationDialogFragment(
                        R.drawable.ic_perm,
                        getString(R.string.permissions),
                        it.perms
                    ).show(childFragmentManager, TAG)
                }
                appTrackers.setOnClickListener {
                    ApplicationDialogFragment(
                        R.drawable.ic_tracker,
                        getString(R.string.trackers),
                        getString(R.string.trackers_description, "")
                    ).show(childFragmentManager, TAG)
                }
            }

            binding.applicationLayout.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun shareApp(name: String, shareUrl: String): Intent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_intent, name, shareUrl))
            type = "text/plain"
        }
        return shareIntent
    }
}
