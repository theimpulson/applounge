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

package foundation.e.apps.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.os.LocaleListCompat
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.KeyDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import foundation.e.apps.categories.model.Category
import java.net.URL
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt

object Common {

    val EXECUTOR = Executors.newCachedThreadPool()!!

    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    fun toMiB(bytes: Int): Double {
        val inMiB = bytes.div(1048576.0)
        return inMiB.times(10.0).roundToInt().div(10.0)
    }

    fun createConnection(url: String, requestMethod: String): HttpsURLConnection {
        val preferredLanguage =getAcceptedLanguageHeaderValue()
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.setRequestProperty("Accept-Language", preferredLanguage)
        connection.requestMethod = requestMethod
        connection.connectTimeout = Constants.CONNECT_TIMEOUT
        connection.readTimeout = Constants.READ_TIMEOUT
        return connection
    }

    /*
     * Checks if the given [packageName] is a system app or not
     * @param packageManager current PackageManager
     * @param packageName package to verify
     * @return true if the app is system app. false otherwise
     */
    fun isSystemApp(packageManager: PackageManager, packageName: String?): Boolean {
        if (packageName != null) {
            return try {
                val info = packageManager.getPackageInfo(packageName, 0)
                (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (exception: Exception) {
                false
            }
        }
        return false
    }

    fun appHasLaunchActivity(context: Context, packageName: String?): Boolean {
        return (packageName?.let { context.packageManager.getLaunchIntentForPackage(it) } != null)
    }

    fun getObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        var simpleModule =  SimpleModule()
        simpleModule.addKeyDeserializer(Category::class.java,keyDeserializer())
        objectMapper.registerModule(simpleModule);
        objectMapper.registerKotlinModule()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerKotlinModule()
        return objectMapper
    }

    fun getAcceptedLanguageHeaderValue(): String {
        var weight = 1.0F
        return getPreferredLocaleList()
                .map { it.toLanguageTag() }
                .reduce { accumulator, languageTag ->
                    weight -= 0.1F
                    "$accumulator,$languageTag;q=$weight"
                }
    }

    fun getPreferredLocaleList(): List<Locale> {
        val adjustedLocaleListCompat = LocaleListCompat.getAdjustedDefault()
        val preferredLocaleList = mutableListOf<Locale>()
        for (index in 0 until adjustedLocaleListCompat.size()) {
            preferredLocaleList.add(adjustedLocaleListCompat.get(index))
        }
        return preferredLocaleList
    }
}

class keyDeserializer : KeyDeserializer() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun deserializeKey(p0: String?, p1: DeserializationContext?): Any? {
        return Paths.get(p0)
    }


}
