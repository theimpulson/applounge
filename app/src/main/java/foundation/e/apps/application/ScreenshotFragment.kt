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

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.R
import foundation.e.apps.application.model.ScreenshotRVAdapter
import foundation.e.apps.databinding.FragmentScreenshotBinding

@AndroidEntryPoint
class ScreenshotFragment : Fragment(R.layout.fragment_screenshot) {

    private var _binding: FragmentScreenshotBinding? = null
    private val binding get() = _binding!!

    private val args: ScreenshotFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentScreenshotBinding.bind(view)

        val window = activity?.window

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= 30) {
            window?.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }

        val screenshotRVAdapter = ScreenshotRVAdapter(args.list.toList(), args.origin)
        binding.viewPager.apply {
            adapter = screenshotRVAdapter
            currentItem = args.position
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onDetach() {
        val window = activity?.window

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= 30) {
            window?.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
        super.onDetach()
    }

}