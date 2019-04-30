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

package foundation.e.apps.categories.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import android.content.Intent
import foundation.e.apps.categories.category.CategoryActivity
import foundation.e.apps.categories.model.CategoriesModel
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class CategoriesViewModel : ViewModel(), CategoriesViewModelInterface {
    private val categoriesModel = CategoriesModel()

    override fun getApplicationsCategories(): MutableLiveData<ArrayList<Category>> {
        return categoriesModel.applicationsCategoriesList
    }

    override fun getGamesCategories(): MutableLiveData<ArrayList<Category>> {
        return categoriesModel.gamesCategoriesList
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return categoriesModel.screenError
    }

    override fun loadCategories(context: Context) {
        categoriesModel.screenError.value = null
        categoriesModel.loadCategories(context)
    }

    override fun onCategoryClick(context: Context, category: Category) {
        val intent = Intent(context, CategoryActivity::class.java)
        intent.putExtra(Constants.CATEGORY_KEY, category)
        context.startActivity(intent)
    }
}
