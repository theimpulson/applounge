package foundation.e.apps

import android.os.Bundle
import android.util.Log
import android.view.View
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

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            when (destination.id) {
                R.id.applicationFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    window.navigationBarColor = this.getColor(R.color.colorWhite)
                }
                R.id.categoriesFragment -> {
                    window.statusBarColor = this.getColor(R.color.colorAccent95)
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    window.statusBarColor = this.getColor(R.color.colorWhite)
                    window.navigationBarColor = this.getColor(R.color.colorAccent95)
                }
            }
        }
    }
}
