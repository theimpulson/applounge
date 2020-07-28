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

package foundation.e.apps.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import java.net.URL
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection
import kotlin.math.roundToInt
import com.fasterxml.jackson.module.kotlin.registerKotlinModule


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
        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.requestMethod = requestMethod
        connection.connectTimeout = Constants.CONNECT_TIMEOUT
        connection.readTimeout = Constants.READ_TIMEOUT
        return connection
    }

    fun isSystemApp(packageManager: PackageManager, packageName: String?): Boolean {
        try {
            // Get package information for the app
            val appPackageInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES)
            // Get application information for the app
            val appInfo = packageManager.getApplicationInfo(
                    packageName, 0)
            // Get package information for the Android system
            val systemPackageInfo = packageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES)

            // Compare app and Android system signatures
            if (appPackageInfo.signatures.isNotEmpty() &&
                    systemPackageInfo.signatures.isNotEmpty() &&
                    appPackageInfo.signatures[0] == systemPackageInfo.signatures[0]) {
                return true
            } else if (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or
                            ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }

    fun appHasLaunchActivity(context: Context, packageName: String?): Boolean {
        return (context.packageManager.getLaunchIntentForPackage(packageName) != null)
    }

    fun getObjectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        objectMapper.registerKotlinModule()
        return objectMapper
    }
}
