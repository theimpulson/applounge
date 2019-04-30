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

package foundation.e.apps.home

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import foundation.e.apps.R
import foundation.e.apps.categories.model.Category
import kotlinx.android.synthetic.main.home_category_list_item.view.*

class HomeCategory(context: Context, category: Category) : LinearLayout(context) {

    init {
        View.inflate(context, R.layout.home_category_list_item, this)
        category_title.text = category.getTitle()
    }
}
