/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.settings

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import foundation.e.apps.R
import foundation.e.apps.settings.viewmodel.AppRequestViewModel
import foundation.e.apps.utils.Error
import kotlinx.android.synthetic.main.activity_app_request.*

class AppRequestActivity : AppCompatActivity(), TextWatcher {
    private lateinit var viewModel: AppRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_request)

        // Initialise toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewModel = ViewModelProviders.of(this).get(AppRequestViewModel::class.java)
        progress_bar.visibility = View.GONE
        app_request_error_text_view.visibility = View.GONE
        package_name_edit_text.addTextChangedListener(this)
        submit_button.setOnClickListener {
            package_name_edit_text.visibility = View.GONE
            submit_button.visibility = View.GONE
            progress_bar.visibility = View.VISIBLE
            app_request_error_text_view.visibility = View.GONE
            viewModel.onSubmit(this)
        }

        // Bind enabled state of submit button to value in view model
        viewModel.isSubmitButtonEnabled.observe(this, Observer {
            if (it != null) {
                submit_button.isEnabled = it
            }
        })

        // Bind to the screen error
        viewModel.getScreenError().observe(this, Observer {
            if (it != null) {
                progress_bar.visibility = View.GONE
                if (it == Error.NO_ERROR) {
                    package_name_edit_text.setText("")
                    package_name_edit_text.visibility = View.VISIBLE
                    submit_button.visibility = View.VISIBLE
                    app_request_error_text_view.visibility = View.VISIBLE
                    app_request_error_text_view.text =
                            getString(R.string.app_request_successful_text)
                    app_request_error_text_view.background = getDrawable(R.drawable.success_border)
                } else {
                    package_name_edit_text.visibility = View.VISIBLE
                    submit_button.visibility = View.VISIBLE
                    app_request_error_text_view.visibility = View.VISIBLE
                    app_request_error_text_view.text = getString(it.description)
                    app_request_error_text_view.background = getDrawable(R.drawable.error_border)
                }
                scroll_view.scrollTo(0, 0)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
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
