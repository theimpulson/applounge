package foundation.e.apps.application

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.load
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.fused.FusedAPIInterface
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.databinding.FragmentApplicationBinding
import javax.inject.Inject

@AndroidEntryPoint
class ApplicationFragment : Fragment(R.layout.fragment_application), FusedAPIInterface {

    private val args: ApplicationFragmentArgs by navArgs()

    private var _binding: FragmentApplicationBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var gson: Gson

    private val applicationViewModel: ApplicationViewModel by viewModels()
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentApplicationBinding.bind(view)

        val data = mainActivityViewModel.authData.value?.let {
            gson.fromJson(it, AuthData::class.java)
        }
        val notAvailable = getString(R.string.not_available)

        val circularProgressDrawable = CircularProgressDrawable(view.context)
        circularProgressDrawable.strokeWidth = 10f
        circularProgressDrawable.centerRadius = 50f
        circularProgressDrawable.colorFilter = PorterDuffColorFilter(
            view.context.getColor(R.color.colorAccent),
            PorterDuff.Mode.SRC_IN
        )

        binding.toolbar.apply {
            setNavigationOnClickListener {
                view.findNavController().navigateUp()
            }
        }

        data?.let {
            applicationViewModel.getApplicationDetails(
                args.id,
                args.packageName,
                it,
                args.origin
            )
        }
        applicationViewModel.fusedApp.observe(viewLifecycleOwner, { fusedApp ->
            fusedApp?.let {
                binding.appName.text = it.name
                binding.appAuthor.text = it.author
                binding.categoryTitle.text = it.category
                if (it.ratings.usageQualityScore != -1.0) {
                    binding.appRating.text = it.ratings.usageQualityScore.toString()
                }
                if (it.ratings.privacyScore != -1.0) {
                    binding.appPrivacyScore.text = it.ratings.privacyScore.toString()
                }
                if (args.origin == Origin.CLEANAPK) {
                    binding.appIcon.load(CleanAPKInterface.ASSET_URL + it.icon_image_path) {
                        placeholder(circularProgressDrawable)
                    }
                } else {
                    binding.appIcon.load(it.icon_image_path) {
                        placeholder(circularProgressDrawable)
                    }
                }
                binding.appDescription.text =
                    Html.fromHtml(it.description, Html.FROM_HTML_MODE_COMPACT)
                binding.appUpdatedOn.text = getString(
                    R.string.updated_on,
                    if (args.origin == Origin.CLEANAPK) it.last_modified.split(" ")[0] else it.last_modified
                )
                binding.appRequires.text = getString(R.string.min_android_version, notAvailable)
                binding.appVersion.text = getString(R.string.version, if (it.latest_version_number == "-1") notAvailable else it.latest_version_number)
                binding.appLicense.text = getString(
                    R.string.license,
                    if (it.licence.isBlank() or (it.licence == "unknown")) notAvailable else it.licence
                )
                binding.appPackageName.text = getString(R.string.package_name, it.package_name)
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int?,
        origin: Origin?
    ) {
        val data = mainActivityViewModel.authData.value?.let {
            gson.fromJson(it, AuthData::class.java)
        }
        val offer = offerType ?: 0
        val org = origin ?: Origin.CLEANAPK
        data?.let {
            applicationViewModel.getApplication(
                id,
                name,
                packageName,
                versionCode,
                offer,
                it,
                org
            )
        }
    }
}
