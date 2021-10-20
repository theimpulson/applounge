package foundation.e.apps

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
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

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        val viewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)

        // Watch and refresh authentication data
        viewModel.authData.observe(this, {
            if (it.isNullOrEmpty()) {
                Log.d(TAG, "Fetching new authentication data")
                viewModel.getAuthData()
            } else {
                Log.d(TAG, "Authentication data is available!")
            }
        })

//        val url = "https://apk.cleanapk.org/any_721fbaf1be08d0a2e1927d28eadca70c_com.whatsapp.w4b.apk"
//        val packageName = "com.whatsapp.w4b"
//        val name = "WhatsApp Business"
//        viewModel.downloadApp(name, packageName, url)

    }
}
