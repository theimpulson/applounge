package foundation.e.apps.categories.model

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import foundation.e.apps.categories.AppsFragment
import foundation.e.apps.categories.GamesFragment

class CategoriesVPAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> AppsFragment()
            1 -> GamesFragment()
            else -> AppsFragment()
        }
    }
}
