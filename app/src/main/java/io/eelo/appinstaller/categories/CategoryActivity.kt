package io.eelo.appinstaller.categories

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import io.eelo.appinstaller.R
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utlis.Constants.CATEGORY_KEY

class CategoryActivity : AppCompatActivity() {

    private lateinit var category: Category

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        intent?.let {
            category = intent.getSerializableExtra(CATEGORY_KEY) as Category
            supportActionBar?.setTitle(category.title)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            android.R.id.home ->
                finish()
        }
        return true
    }
}
