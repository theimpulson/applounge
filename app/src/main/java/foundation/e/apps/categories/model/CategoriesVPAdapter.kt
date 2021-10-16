package foundation.e.apps.categories.model

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import foundation.e.apps.categories.AppsFragment
import foundation.e.apps.categories.GamesFragment

class CategoriesVPAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> AppsFragment()
            1 -> GamesFragment()
        }
        return AppsFragment()
    }
}
