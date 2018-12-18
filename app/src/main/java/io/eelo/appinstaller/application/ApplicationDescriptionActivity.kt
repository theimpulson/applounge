package io.eelo.appinstaller.application

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.text.util.Linkify
import android.view.MenuItem
import io.eelo.appinstaller.R
import io.eelo.appinstaller.utils.Constants.APPLICATION_DESCRIPTION_KEY
import kotlinx.android.synthetic.main.activity_application_description.*

class ApplicationDescriptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application_description)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setTitle(R.string.application_description_activity_title)

        if (intent != null) {
            app_description.text = intent.getStringExtra(APPLICATION_DESCRIPTION_KEY)
            Linkify.addLinks(app_description, Linkify.ALL)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when {
            item?.itemId == android.R.id.home -> {
                finish()
            }
            else -> {
                return false
            }
        }
        return true
    }
}
