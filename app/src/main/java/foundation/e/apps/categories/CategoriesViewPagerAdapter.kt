/*
 * Copyright (C) 2019-2021  E FOUNDATION
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

package foundation.e.apps.categories

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class CategoriesViewPagerAdapter(fragmentManager: FragmentManager, private val numberOfTabs: Int, color: Int?) :
        FragmentStatePagerAdapter(fragmentManager) {

    private val applicationsFragment = ApplicationsFragment.newInstance(color)
    private val gamesFragment = GamesFragment.newInstance(color)
    private val pwasFragment = PwasFragment()


    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return applicationsFragment
            }
            1 -> {
                return gamesFragment
            }
            2 -> {
                return pwasFragment
            }
        }
        return pwasFragment
    }

    override fun getCount(): Int {
        return numberOfTabs
    }
}
