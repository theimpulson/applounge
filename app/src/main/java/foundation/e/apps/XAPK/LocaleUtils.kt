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

package foundation.e.apps.XAPK

import android.os.Build
import android.os.LocaleList
import android.text.TextUtils
import java.util.*

class LocaleUtils {

//    val appLocal: Locale
//        get() {
//            val localValue = Settings.languageValue
//            return if (TextUtils.equals(localValue, MainActivity.mActivity.getString(R.string.language_auto_value))) {
//                systemLocal
//            } else {
//                forLanguageTag(localValue)
//            }
//        }

    val systemLocal: Locale
        get() {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                LocaleList.getDefault().get(0)
            } else {
                Locale.getDefault()
            }
        }



//    val appLocalTag: String
//        get() {
//            return toLanguageTag(appLocal)
//        }

    private fun forLanguageTag(languageTag: String): Locale {
        return if (Build.VERSION.SDK_INT >= 21) {
            Locale.forLanguageTag(languageTag)
        } else {
            val parts = languageTag.split("-".toRegex()).toTypedArray()
            if (parts.size == 1)
                Locale(parts[0])
            else if (parts.size == 2 || parts.size == 3 && parts[2].startsWith("#"))
                Locale(parts[0], parts[1])
            else
                Locale(parts[0], parts[1], parts[2])
        }
    }

    private fun toLanguageTag(locale: Locale): String {
        val language = locale.language
        val country = locale.country
        val variant = locale.variant
        return when {
            TextUtils.isEmpty(country) -> language
            TextUtils.isEmpty(variant) -> "$language-$country"
            else -> "$language-$country-$variant"
        }
    }
}
