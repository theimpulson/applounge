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

package foundation.e.apps.pwa

import android.annotation.SuppressLint
import android.content.*
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.MainActivity.Companion.sharedPreferences
import foundation.e.apps.R
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.utils.Constants
import java.io.FileNotFoundException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

private const val TAG = "PwaInstaller"

class PwaInstaller : AppCompatActivity() {

    lateinit var icon: Bitmap
    private val sharedPrefFile = "kotlinsharedpreference"
    var scaledBitmap: Bitmap? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)
        sharedPreferences = this.getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        val extras = intent.extras
        val name = extras?.getString("NAME")
        val url = Uri.parse(extras?.getString("URL"))
        if (name == null || url == null) {
            Log.d(TAG, "Name or Url must not be null")
            finish()
        } else {
            installer(name, url)
        }
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    private fun setBooleanConfig(key: String) {
        val editor: SharedPreferences.Editor = sharedPreferences!!.edit()
        editor.putBoolean(key, true)
        editor.apply()
    }


    private fun installer(name: String, pwaUrl: Uri) {
        //TODO: Add in content provider first and then make a shortcut
        setBooleanConfig(name)
        val contentResolver = contentResolver
        Thread {
            run {
                Looper.prepare() //Call looper.prepare()
                try {
                    val uri = PwasBasicData.thisActivity!!.uri
                    val url = URL(Constants.BASE_URL + "media/" + uri)
                    val urlConnection = url.openConnection() as HttpsURLConnection
                    urlConnection.requestMethod = Constants.REQUEST_METHOD_GET
                    urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
                    urlConnection.readTimeout = Constants.READ_TIMEOUT
                    icon = BitmapFactory.decodeStream(urlConnection.inputStream)
                    //scaledBitmap = Bitmap.createScaledBitmap(icon, 128, 128, true)
                } catch (e: FileNotFoundException) {
                    val x = R.drawable.pwa_default_icon
                    icon = BitmapFactory.decodeResource(mActivity.getResources(),
                            x)
                    //scaledBitmap = Bitmap.createScaledBitmap(icon, 128, 128, true)
                }
                val intent = Intent()
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)
                intent.action = "foundation.e.blisslauncher.VIEW_PWA"
                intent.data = pwaUrl
                intent.putExtra("PWA_NAME", name)

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                    val shortcutManager = mActivity.getSystemService(ShortcutManager::class.java)
                    if (isExistShortcutInfo(pwaUrl.toString())) {
                        Toast.makeText(this, "Shortcut already exist", Toast.LENGTH_SHORT).show()
                    } else {
                        val values = ContentValues()
                        val id = pwaUrl.toString()
                        writeToValues(id, name, pwaUrl, icon, values)
                        val uri = contentResolver.insert(Uri.parse("content://foundation.e.pwaplayer.provider/pwa"), values)
                        if (uri == null) {
                            Toast.makeText(this, "Can't install the pwa", Toast.LENGTH_SHORT).show()
                            return@Thread
                        }
                        val db_id = ContentUris.parseId(uri)
                        intent.putExtra("PWA_ID", db_id)
                        Toast.makeText(this, "Shortcut created", Toast.LENGTH_SHORT).show()
                        val shortcut = ShortcutInfo.Builder(mActivity, pwaUrl.toString())
                                .setShortLabel(name)
                                .setIcon(Icon.createWithBitmap(icon))
                                .setIntent(intent)
                                .build()
                        shortcutManager.requestPinShortcut(shortcut, null)
                    }
                } else {
                    val values = ContentValues()
                    val id = pwaUrl.toString()
                    writeToValues(id, name, pwaUrl, icon, values)
                    val uri = contentResolver.insert(Uri.parse("content://foundation.e.pwaplayer.provider/pwa"), values)
                    if (uri == null) {
                        Toast.makeText(this, "Can't install the pwa", Toast.LENGTH_SHORT).show()
                        return@Thread
                    }
                    val db_id = ContentUris.parseId(uri)
                    intent.putExtra("PWA_ID", db_id)
                    val installer = Intent()
                    installer.putExtra("android.intent.extra.shortcut.INTENT", intent)
                    installer.putExtra("android.intent.extra.shortcut.NAME", name)
                    installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, icon)
                    installer.putExtra("duplicate", false)
                    installer.action = "com.android.launcher.action.INSTALL_SHORTCUT"
                    mActivity.sendBroadcast(installer)
                }
                Looper.loop()
            }
        }.start()
    }

    private fun writeToValues(id: String, name: String, pwaUrl: Uri, icon: Bitmap, outValues: ContentValues) {
        outValues.put("URL", pwaUrl.toString())
        outValues.put("SHORTCUT_ID", id)
        outValues.put("TITLE", name)
        outValues.put("ICON", icon.toByteArray())
    }

    companion object {

        @SuppressLint("NewApi")
        fun isExistShortcutInfo(shortcutId: String): Boolean {
            val shortcutManager = mActivity.getSystemService(ShortcutManager::class.java)
            val shortcutInfoList = shortcutManager.pinnedShortcuts

            for (info in shortcutInfoList) {
                if (info.id == shortcutId) {
                    return true
                }
            }
            return false
        }
    }
}
