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
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.application.model.ApplicationScreenshotsRVAdapter
import foundation.e.apps.application.subFrags.ApplicationDialogFragment
import foundation.e.apps.databinding.FragmentApplicationBinding
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
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

    private var applicationIcon: ImageView? = null

    companion object {
        private const val PRIVACY_SCORE_SOURCE_CODE_URL = "https://gitlab.e.foundation/e/apps/apps/-/blob/main/app/src/main/java/foundation/e/apps/application/ApplicationViewModel.kt#L131"
        private const val EXODUS_URL = "https://exodus-privacy.eu.org"
        private const val PRIVACY_GUIDELINE_URL = "https://doc.e.foundation/privacy_score"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApplicationBinding.bind(view)

        mainActivityViewModel.internetConnection.observe(viewLifecycleOwner) { hasInternet ->
            mainActivityViewModel.authData.observe(viewLifecycleOwner) { authData ->
                if (hasInternet) {
                    applicationViewModel.getApplicationDetails(
                        args.id,
                        args.packageName,
                        authData,
                        args.origin
                    )
                }
            }
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

        mainActivityViewModel.downloadList.observe(viewLifecycleOwner) { list ->
            list.forEach {
                if (it.origin == args.origin && (it.package_name == args.packageName || it.id == args.id)) {
                    applicationViewModel.appStatus.value = it.status
                }
            }
        }

        applicationViewModel.appStatus.observe(viewLifecycleOwner) { status ->
            val installButton = binding.downloadInclude.installButton
            val downloadPB = binding.downloadInclude.appInstallPB
            val appSize = binding.downloadInclude.appSize
            val fusedApp = applicationViewModel.fusedApp.value ?: FusedApp()

            when (status) {
                Status.INSTALLED -> {
                    installButton.apply {
                        isEnabled = true
                        text = getString(R.string.open)
                        setTextColor(Color.WHITE)
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            startActivity(pkgManagerModule.getLaunchIntent(fusedApp.package_name))
                        }
                    }
                }
                Status.UPDATABLE -> {
                    installButton.apply {
                        text = getString(R.string.update)
                        setTextColor(Color.WHITE)
                        backgroundTintList =
                            ContextCompat.getColorStateList(view.context, R.color.colorAccent)
                        setOnClickListener {
                            applicationIcon?.let {
                                mainActivityViewModel.getApplication(fusedApp, it)
                            }
                        }
                    }
                    downloadPB.visibility = View.GONE
                    appSize.visibility = View.VISIBLE
                }
                Status.UNAVAILABLE -> {
                    installButton.apply {
                        text = getString(R.string.install)
                        setOnClickListener {
                            applicationIcon?.let {
                                mainActivityViewModel.getApplication(fusedApp, it)
                            }
                        }
                    }
                    downloadPB.visibility = View.GONE
                    appSize.visibility = View.VISIBLE
                }
                Status.QUEUED -> {
                    installButton.apply {
                        text = getString(R.string.cancel)
                        setOnClickListener {
                            mainActivityViewModel.cancelDownload(fusedApp)
                        }
                    }
                }
                Status.DOWNLOADING -> {
                    installButton.apply {
                        text = getString(R.string.cancel)
                        setOnClickListener {
                            mainActivityViewModel.cancelDownload(fusedApp)
                        }
                    }
                    downloadPB.visibility = View.VISIBLE
                    appSize.visibility = View.GONE
                    applicationViewModel.downloadProgress.observe(viewLifecycleOwner) {
                        downloadPB.max = it.totalSizeBytes.values.sum().toInt()
                        downloadPB.progress = it.bytesDownloadedSoFar.values.sum().toInt()
                    }
                }
                Status.INSTALLING, Status.UNINSTALLING -> {
                    installButton.isEnabled = false
                    downloadPB.visibility = View.GONE
                    appSize.visibility = View.VISIBLE
                }
                Status.BLOCKED -> {
                    installButton.setOnClickListener {
                        val errorMsg = when (
                            User.valueOf(
                                mainActivityViewModel.userType.value ?: User.UNAVAILABLE.name
                            )
                        ) {
                            User.ANONYMOUS,
                            User.UNAVAILABLE -> getString(R.string.install_blocked_anonymous)
                            User.GOOGLE -> getString(R.string.install_blocked_google)
                        }
                        if (errorMsg.isNotBlank()) {
                            Snackbar.make(view, errorMsg, Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "Unknown status: $status")
                }
            }
        }

        applicationViewModel.fusedApp.observe(viewLifecycleOwner) {
            if (applicationViewModel.appStatus.value == null) {
                applicationViewModel.appStatus.value = it.status
            }
            screenshotsRVAdapter.setData(it.other_images_path)

            // Title widgets
            binding.titleInclude.apply {
                applicationIcon = appIcon
                appName.text = it.name
                appAuthor.text = it.author
                categoryTitle.text = it.category
                if (args.origin == Origin.CLEANAPK) {
                    appIcon.load(CleanAPKInterface.ASSET_URL + it.icon_image_path)
                } else {
                    appIcon.load(it.icon_image_path)
                }
            }

            binding.downloadInclude.appSize.text = it.appSize

            // Ratings widgets
            binding.ratingsInclude.apply {
                if (it.ratings.usageQualityScore != -1.0) {
                    val rating =
                        applicationViewModel.handleRatingFormat(it.ratings.usageQualityScore)
                    appRating.text =
                        getString(
                            R.string.rating_out_of, rating
                        )

                    appRating.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, getRatingDrawable(rating), null
                    )
                    appRating.compoundDrawablePadding = 15
                }
                appRatingLayout.setOnClickListener {
                    ApplicationDialogFragment(
                        R.drawable.ic_star,
                        getString(R.string.rating),
                        getString(R.string.rating_description)
                    ).show(childFragmentManager, TAG)
                }

                appPrivacyScoreLayout.setOnClickListener {
                    ApplicationDialogFragment(
                        R.drawable.ic_lock,
                        getString(R.string.privacy_score),
                        getString(
                            R.string.privacy_description,
                            PRIVACY_SCORE_SOURCE_CODE_URL,
                            EXODUS_URL,
                            PRIVACY_GUIDELINE_URL
                        )
                    ).show(childFragmentManager, TAG)
                }
            }

            binding.appDescription.text =
                Html.fromHtml(it.description, Html.FROM_HTML_MODE_COMPACT)

            binding.appDescriptionMore.setOnClickListener { view ->
                val action =
                    ApplicationFragmentDirections.actionApplicationFragmentToDescriptionFragment(it.description)
                view.findNavController().navigate(action)
            }

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
                var permission =
                    applicationViewModel.transformPermsToString(it.perms.toMutableList())
                if (permission.isEmpty()) {
                    permission = getString(
                        R.string.no_permission_found
                    )
                }
                appPermissions.setOnClickListener { _ ->
                    ApplicationDialogFragment(
                        R.drawable.ic_perm,
                        getString(R.string.permissions),
                        permission
                    ).show(childFragmentManager, TAG)
                }
                appTrackers.setOnClickListener {
                    var trackers = applicationViewModel.getTrackerListText()

                    if (trackers.isNotEmpty()) {
                        trackers += "<br /> <br />" + getString(
                            R.string.privacy_computed_using_text,
                            EXODUS_URL
                        )
                    } else {
                        trackers = getString(R.string.no_tracker_found)
                    }

                    ApplicationDialogFragment(
                        R.drawable.ic_tracker,
                        getString(R.string.trackers_title),
                        trackers
                    ).show(childFragmentManager, TAG)
                }
            }

            fetchAppTracker()
        }
    }

    private fun fetchAppTracker() {
        applicationViewModel.fetchTrackerData().observe(viewLifecycleOwner) {
            updatePrivacyScore()
            binding.applicationLayout.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updatePrivacyScore() {
        val privacyScore = applicationViewModel.getPrivacyScore()
        if (privacyScore != -1) {
            val appPrivacyScore = binding.ratingsInclude.appPrivacyScore
            appPrivacyScore.text = getString(
                R.string.privacy_rating_out_of,
                privacyScore.toString()
            )

            appPrivacyScore.setCompoundDrawablesRelativeWithIntrinsicBounds(
                null, null, getPrivacyDrawable(privacyScore.toString()), null
            )
            appPrivacyScore.compoundDrawablePadding = 15
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        applicationIcon = null
    }

    private fun shareApp(name: String, shareUrl: String): Intent {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_intent, name, shareUrl))
            type = "text/plain"
        }
        return shareIntent
    }

    private fun getPrivacyDrawable(privacyRating: String): Drawable? {
        val rating = privacyRating.toInt()

        var dotColor = ContextCompat.getColor(requireContext(), R.color.colorGreen)
        if (rating <= 3) {
            dotColor = ContextCompat.getColor(requireContext(), R.color.colorRed)
        } else if (rating <= 6) {
            dotColor = ContextCompat.getColor(requireContext(), R.color.colorYellow)
        }

        return applyDotAccent(dotColor)
    }

    private fun getRatingDrawable(reviewRating: String): Drawable? {
        val rating = reviewRating.toDouble()

        var dotColor = ContextCompat.getColor(requireContext(), R.color.colorGreen)
        if (rating <= 2.0) {
            dotColor = ContextCompat.getColor(requireContext(), R.color.colorRed)
        } else if (rating <= 3.4) {
            dotColor = ContextCompat.getColor(requireContext(), R.color.colorYellow)
        }

        return applyDotAccent(dotColor)
    }

    private fun applyDotAccent(dotColor: Int): Drawable? {
        val circleDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.ic_rating_privacy_circle)

        circleDrawable?.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
            dotColor,
            BlendModeCompat.SRC_ATOP
        )

        return circleDrawable
    }
}
