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

package foundation.e.apps.application

import android.os.Bundle
import android.text.util.Linkify
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import foundation.e.apps.R
import foundation.e.apps.databinding.ActivityApplicationDescriptionBinding
import foundation.e.apps.utils.Constants.APPLICATION_DESCRIPTION_KEY

class ApplicationDescriptionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityApplicationDescriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityApplicationDescriptionBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.application_description_activity_title)

        if (intent != null) {
            binding.appDescription.text = intent.getStringExtra(APPLICATION_DESCRIPTION_KEY)
            Linkify.addLinks(binding.appDescription, Linkify.ALL)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when {
            item.itemId == android.R.id.home -> {
                finish()
            }
            else -> {
                return false
            }
        }
        return true
    }
}
