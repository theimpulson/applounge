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
import android.text.format.Formatter
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.aurora.gplayapi.data.models.AuthData
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.AppInfoFetchViewModel
import foundation.e.apps.MainActivity
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.PrivacyInfoViewModel
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.application.model.ApplicationScreenshotsRVAdapter
import foundation.e.apps.application.subFrags.ApplicationDialogFragment
import foundation.e.apps.databinding.FragmentApplicationBinding
import foundation.e.apps.manager.download.data.DownloadProgress
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.ResultStatus
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.User
import foundation.e.apps.utils.interfaces.TimeoutFragmentInterface
import foundation.e.apps.utils.modules.CommonUtilsModule.LIST_OF_NULL
import foundation.e.apps.utils.modules.PWAManagerModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationFragment : Fragment(R.layout.fragment_application), TimeoutFragmentInterface {

    private val args: ApplicationFragmentArgs by navArgs()
    private val TAG = ApplicationFragment::class.java.simpleName

    private var _binding: FragmentApplicationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    @Inject
    lateinit var pwaManagerModule: PWAManagerModule

    private val applicationViewModel: ApplicationViewModel by viewModels()
    private val privacyInfoViewModel: PrivacyInfoViewModel by viewModels()
    private val appInfoFetchViewModel: AppInfoFetchViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    private var applicationIcon: ImageView? = null

    companion object {
        private const val PRIVACY_SCORE_SOURCE_CODE_URL =
            "https://gitlab.e.foundation/e/os/apps/-/blob/main/app/src/main/java/foundation/e/apps/PrivacyInfoViewModel.kt#L78"
        private const val EXODUS_URL = "https://exodus-privacy.eu.org"
        private const val PRIVACY_GUIDELINE_URL = "https://doc.e.foundation/privacy_score"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApplicationBinding.bind(view)

        /*
         * Explanation of double observers in HomeFragment.kt
         */

        mainActivityViewModel.internetConnection.observe(viewLifecycleOwner) {
            refreshDataOrRefreshToken(mainActivityViewModel)
        }
        mainActivityViewModel.authData.observe(viewLifecycleOwner) {
            refreshDataOrRefreshToken(mainActivityViewModel)
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
            applicationViewModel.updateApplicationStatus(list)
        }

        applicationViewModel.fusedApp.observe(viewLifecycleOwner) { resultPair ->
            if (resultPair.second != ResultStatus.OK) {
                onTimeout()
                return@observe
            }

            /*
             * Previously fusedApp only had instance of FusedApp.
             * As such previously all reference was simply using "it", the default variable in
             * the scope. But now "it" is Pair(FusedApp, ResultStatus), not an instance of FusedApp.
             *
             * Avoid Git diffs by using a variable named "it".
             *
             * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5413
             */
            val it = resultPair.first

            mainActivityViewModel.dismissTimeoutDialog()

            if (applicationViewModel.appStatus.value == null) {
                applicationViewModel.appStatus.value = it.status
            }
            screenshotsRVAdapter.setData(it.other_images_path)

            // Title widgets
            binding.titleInclude.apply {
                applicationIcon = appIcon
                appName.text = it.name
                appAuthor.text = it.author
                appInfoFetchViewModel.setAuthorNameIfNeeded(appAuthor, it)
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
                appPermissions.setOnClickListener { _ ->
                    ApplicationDialogFragment(
                        R.drawable.ic_perm,
                        getString(R.string.permissions),
                        getPermissionListString()
                    ).show(childFragmentManager, TAG)
                }
                appTrackers.setOnClickListener {
                    val fusedApp = applicationViewModel.fusedApp.value?.first
                    var trackers =
                        privacyInfoViewModel.getTrackerListText(fusedApp)

                    if (fusedApp?.trackers == LIST_OF_NULL) {
                        trackers = getString(R.string.tracker_information_not_found)
                    } else if (trackers.isNotEmpty()) {
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

            observeDownloadStatus(view)
            fetchAppTracker(it)
        }

        applicationViewModel.errorMessageLiveData.observe(viewLifecycleOwner) {
            (requireActivity() as MainActivity).showSnackbarMessage(getString(it))
        }
    }

    override fun onTimeout() {
        if (!mainActivityViewModel.isTimeoutDialogDisplayed()) {
            mainActivityViewModel.displayTimeoutAlertDialog(
                activity = requireActivity(),
                message = getString(R.string.timeout_desc_cleanapk),
                positiveButtonText = getString(R.string.retry),
                positiveButtonBlock = {
                    showLoadingUI()
                    mainActivityViewModel.retryFetchingTokenAfterTimeout()
                },
                negativeButtonText = getString(android.R.string.ok),
                negativeButtonBlock = {
                    requireActivity().onBackPressed()
                },
                allowCancel = false,
            )
        }
    }

    override fun refreshData(authData: AuthData) {
        applicationViewModel.getApplicationDetails(
            args.id,
            args.packageName,
            authData,
            args.origin
        )
    }

    private fun observeDownloadStatus(view: View) {
        applicationViewModel.appStatus.observe(viewLifecycleOwner) { status ->
            val installButton = binding.downloadInclude.installButton
            val downloadPB = binding.downloadInclude.progressLayout
            val appSize = binding.downloadInclude.appSize
            val fusedApp = applicationViewModel.fusedApp.value?.first ?: FusedApp()

            when (status) {
                Status.INSTALLED -> handleInstalled(
                    installButton,
                    view,
                    fusedApp,
                    downloadPB,
                    appSize
                )
                Status.UPDATABLE -> handleUpdatable(
                    installButton,
                    view,
                    fusedApp,
                    downloadPB,
                    appSize
                )
                Status.UNAVAILABLE -> handleUnavaiable(installButton, fusedApp, downloadPB, appSize)
                Status.QUEUED, Status.AWAITING -> handleQueued(
                    installButton,
                    fusedApp,
                    downloadPB,
                    appSize
                )
                Status.DOWNLOADING -> handleDownloading(
                    installButton,
                    fusedApp,
                    downloadPB,
                    appSize
                )
                Status.INSTALLING, Status.UNINSTALLING -> handleInstallingUninstalling(
                    installButton,
                    downloadPB,
                    appSize
                )
                Status.BLOCKED -> handleBlocked(installButton, view)
                Status.INSTALLATION_ISSUE -> handleInstallingIssue(
                    installButton,
                    fusedApp,
                    downloadPB,
                    appSize
                )
                else -> {
                    Log.d(TAG, "Unknown status: $status")
                }
            }
        }
    }

    private fun handleInstallingIssue(
        installButton: MaterialButton,
        fusedApp: FusedApp,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        installButton.apply {
            text = getString(R.string.retry)
            setOnClickListener {
                applicationIcon?.let {
                    mainActivityViewModel.getApplication(fusedApp, it)
                }
            }
        }
        downloadPB.visibility = View.GONE
        appSize.visibility = View.VISIBLE
    }

    private fun handleBlocked(
        installButton: MaterialButton,
        view: View
    ) {
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

    private fun handleInstallingUninstalling(
        installButton: MaterialButton,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        installButton.isEnabled = false
        downloadPB.visibility = View.GONE
        appSize.visibility = View.VISIBLE
    }

    private fun handleDownloading(
        installButton: MaterialButton,
        fusedApp: FusedApp,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        installButton.apply {
            text = getString(R.string.cancel)
            setOnClickListener {
                mainActivityViewModel.cancelDownload(fusedApp)
            }
        }
        downloadPB.visibility = View.VISIBLE
        appSize.visibility = View.GONE
        applicationViewModel.downloadProgress.observe(viewLifecycleOwner) {
            lifecycleScope.launch(Dispatchers.Main) {
                updateProgress(it)
            }
        }
    }

    private fun handleQueued(
        installButton: MaterialButton,
        fusedApp: FusedApp,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        downloadPB.visibility = View.GONE
        appSize.visibility = View.VISIBLE
        installButton.apply {
            text = getString(R.string.cancel)
            setOnClickListener {
                mainActivityViewModel.cancelDownload(fusedApp)
            }
        }
    }

    private fun handleUnavaiable(
        installButton: MaterialButton,
        fusedApp: FusedApp,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        installButton.apply {
            text = when {
                mainActivityViewModel.checkUnsupportedApplication(fusedApp) ->
                    getString(R.string.not_available)
                fusedApp.isFree -> getString(R.string.install)
                else -> fusedApp.price
            }
            setOnClickListener {
                if (mainActivityViewModel.checkUnsupportedApplication(fusedApp, activity)) {
                    return@setOnClickListener
                }
                applicationIcon?.let {
                    if (fusedApp.isFree) {
                        mainActivityViewModel.getApplication(fusedApp, it)
                    } else {
                        if (!mainActivityViewModel.shouldShowPaidAppsSnackBar(fusedApp)) {
                            ApplicationDialogFragment(
                                title = getString(R.string.dialog_title_paid_app, fusedApp.name),
                                message = getString(R.string.dialog_paidapp_message, fusedApp.name, fusedApp.price),
                                positiveButtonText = getString(R.string.dialog_confirm),
                                positiveButtonAction = {
                                    mainActivityViewModel.getApplication(fusedApp, it)
                                },
                                cancelButtonText = getString(R.string.dialog_cancel),
                            ).show(childFragmentManager, "ApplicationFragment")
                        }
                    }
                }
            }
        }
        downloadPB.visibility = View.GONE
        appSize.visibility = View.VISIBLE
    }

    private fun handleUpdatable(
        installButton: MaterialButton,
        view: View,
        fusedApp: FusedApp,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        installButton.apply {
            text = if (mainActivityViewModel.checkUnsupportedApplication(fusedApp))
                getString(R.string.not_available)
            else getString(R.string.update)
            setTextColor(Color.WHITE)
            backgroundTintList =
                ContextCompat.getColorStateList(view.context, R.color.colorAccent)
            setOnClickListener {
                if (mainActivityViewModel.checkUnsupportedApplication(fusedApp, activity)) {
                    return@setOnClickListener
                }
                applicationIcon?.let {
                    mainActivityViewModel.getApplication(fusedApp, it)
                }
            }
        }
        downloadPB.visibility = View.GONE
        appSize.visibility = View.VISIBLE
    }

    private fun handleInstalled(
        installButton: MaterialButton,
        view: View,
        fusedApp: FusedApp,
        downloadPB: RelativeLayout,
        appSize: MaterialTextView
    ) {
        downloadPB.visibility = View.GONE
        appSize.visibility = View.VISIBLE
        installButton.apply {
            isEnabled = true
            text = getString(R.string.open)
            setTextColor(Color.WHITE)
            backgroundTintList =
                ContextCompat.getColorStateList(view.context, R.color.colorAccent)
            setOnClickListener {
                if (fusedApp.is_pwa) {
                    pwaManagerModule.launchPwa(fusedApp)
                } else {
                    startActivity(pkgManagerModule.getLaunchIntent(fusedApp.package_name))
                }
            }
        }
    }

    private suspend fun updateProgress(
        downloadProgress: DownloadProgress,
    ) {
        val progressResult = applicationViewModel.calculateProgress(downloadProgress)
        if (progressResult.first < 1) {
            return
        }
        val downloadedSize = "${
        Formatter.formatFileSize(requireContext(), progressResult.second).substringBefore(" MB")
        }/${Formatter.formatFileSize(requireContext(), progressResult.first)}"
        val progressPercentage =
            ((progressResult.second / progressResult.first.toDouble()) * 100f).toInt()
        binding.downloadInclude.appInstallPB.progress = progressPercentage
        binding.downloadInclude.percentage.text = "$progressPercentage%"
        binding.downloadInclude.downloadedSize.text = downloadedSize
    }

    private fun getPermissionListString(): String {
        var permission =
            applicationViewModel.transformPermsToString()
        if (permission.isEmpty()) {
            permission = getString(
                R.string.no_permission_found
            )
        } else {
            permission += "<br />" + getString(
                R.string.privacy_computed_using_text,
                EXODUS_URL
            )
        }
        return permission
    }

    private fun fetchAppTracker(fusedApp: FusedApp) {
        privacyInfoViewModel.getAppPrivacyInfoLiveData(fusedApp).observe(viewLifecycleOwner) {
            updatePrivacyScore()
            stopLoadingUI()
        }
    }

    private fun showLoadingUI() {
        binding.applicationLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun stopLoadingUI() {
        binding.applicationLayout.visibility = View.VISIBLE
        binding.progressBar.visibility = View.GONE
    }

    private fun updatePrivacyScore() {
        val privacyScore = privacyInfoViewModel.getPrivacyScore(applicationViewModel.fusedApp.value?.first)
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
