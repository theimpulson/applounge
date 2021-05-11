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

package foundation.e.apps.settings

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import foundation.e.apps.R
import foundation.e.apps.databinding.ActivityAppRequestBinding
import foundation.e.apps.settings.viewmodel.AppRequestViewModel
import foundation.e.apps.utils.Error

class AppRequestActivity : AppCompatActivity(), TextWatcher {
    private lateinit var binding: ActivityAppRequestBinding
    private lateinit var viewModel: AppRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityAppRequestBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialise toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProvider(this).get(AppRequestViewModel::class.java)

        // Activity Variables
        val progressBar = binding.progressBar
        val appRequestErrorTextView = binding.appRequestErrorTextView
        val packageNameEditText = binding.packageNameEditText
        val submitButton = binding.submitButton

        progressBar.visibility = View.GONE
        appRequestErrorTextView.visibility = View.GONE
        packageNameEditText.addTextChangedListener(this)
        submitButton.setOnClickListener {
            packageNameEditText.visibility = View.GONE
            submitButton.visibility = View.GONE
            progressBar.visibility = View.VISIBLE
            appRequestErrorTextView.visibility = View.GONE
            viewModel.onSubmit(this)
        }

        // Bind enabled state of submit button to value in view model
        viewModel.isSubmitButtonEnabled.observe(this, Observer {
            if (it != null) {
                submitButton.isEnabled = it
            }
        })

        // Bind to the screen error
        viewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                progressBar.visibility = View.GONE
                if (it == Error.NO_ERROR) {
                    packageNameEditText.setText("")
                    packageNameEditText.visibility = View.VISIBLE
                    submitButton.visibility = View.VISIBLE
                    appRequestErrorTextView.visibility = View.VISIBLE
                    appRequestErrorTextView.text =
                            getString(R.string.app_request_successful_text)
                    appRequestErrorTextView.background = getDrawable(R.drawable.success_border)
                } else {
                    packageNameEditText.visibility = View.VISIBLE
                    submitButton.visibility = View.VISIBLE
                    appRequestErrorTextView.visibility = View.VISIBLE
                    appRequestErrorTextView.text = getString(it.description)
                    appRequestErrorTextView.background = getDrawable(R.drawable.error_border)
                }
                binding.scrollView.scrollTo(0, 0)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home ->
                finish()
        }
        return true
    }

    override fun afterTextChanged(p0: Editable?) {
        // Unused
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        // Unused
    }

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        viewModel.onPackageNameChanged(p0.toString())
    }
}
