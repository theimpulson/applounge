package io.eelo.appinstaller.categories

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentStatePagerAdapter

class CategoriesViewPagerAdapter(fragmentManager: FragmentManager, private val numberOfTabs: Int) :
        FragmentStatePagerAdapter(fragmentManager) {
    private val applicationsFragment = ApplicationsFragment()
    private val gamesFragment = GamesFragment()

    override fun getItem(position: Int): Fragment? {
        when (position) {
            0 -> {
                return applicationsFragment
            }
            1 -> {
                return gamesFragment
            }
        }
        return null
    }

    override fun getCount(): Int {
        return numberOfTabs
    }
}
