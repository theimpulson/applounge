package foundation.e.apps.PWA

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Browser
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.MainActivity.Companion.sharedPreferences
import foundation.e.apps.R
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.utils.Constants
import java.io.FileNotFoundException
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection

class PwaInstaller : AppCompatActivity() {

    var icon : Bitmap?=null
    private val sharedPrefFile = "kotlinsharedpreference"
    var scaledBitmap :Bitmap?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_application)
        sharedPreferences= this.getSharedPreferences(sharedPrefFile,Context.MODE_PRIVATE)


        val extras = intent.extras
        val name = extras.getString("NAME")
        val Url = Uri.parse(extras.getString("URL"))
        installer(name,Url)
    }

    override fun onResume() {
        super.onResume()
        finish()
    }

    fun setBooleanConfig(key:String?,flag:Boolean){
        val editor:SharedPreferences.Editor =  sharedPreferences!!.edit()
        editor.putBoolean(key,flag)
        editor.apply()
    }


    private fun installer(name: String?, myUrl: Uri) {
        setBooleanConfig(name,true)
        Thread{
            run {
                    Looper.prepare();//Call looper.prepare()
                try {
                    var uri = PwasBasicData.thisActivity!!.uri
                    val url = URL(Constants.BASE_URL + "media/" + uri)
                    val urlConnection = url.openConnection() as HttpsURLConnection
                    urlConnection.requestMethod = Constants.REQUEST_METHOD_GET
                    urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
                    urlConnection.readTimeout = Constants.READ_TIMEOUT
                    icon = BitmapFactory.decodeStream(urlConnection.inputStream)
                    scaledBitmap = Bitmap.createScaledBitmap(icon, 128, 128, true)
                }catch (e: FileNotFoundException) {
                    val x = R.drawable.pwa_default_icon
                    val icon = BitmapFactory.decodeResource(mActivity.getResources(),
                            x)
                    scaledBitmap = Bitmap.createScaledBitmap(icon, 128, 128, true)
                }
                    val intent = Intent()
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.setAction(Intent.ACTION_VIEW)
                    intent.putExtra(Browser.EXTRA_APPLICATION_ID, Long.toString())
                    intent.setData(myUrl)

                    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
                        val shortcutManager = mActivity.getSystemService(ShortcutManager::class.java)


                        if (isExistShortcutInfo(name)) {
                            Toast.makeText(this, "Shortcut already exist", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Shortcut created", Toast.LENGTH_SHORT).show()
                            val shortcut = ShortcutInfo.Builder(mActivity, java.lang.Long.toString(Random().nextLong()))
                                    .setShortLabel(name.toString())
                                    .setIcon(Icon.createWithAdaptiveBitmap(scaledBitmap))
                                    .setIntent(intent)
                                    .build()
                            @Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
                            shortcutManager.requestPinShortcut(shortcut, null)
                        }
                    } else {
                        val installer = Intent()
                        installer.putExtra("android.intent.extra.shortcut.INTENT", intent)
                        installer.putExtra("android.intent.extra.shortcut.NAME", name)
                        installer.putExtra(Intent.EXTRA_SHORTCUT_ICON, scaledBitmap)
                        installer.putExtra("duplicate", false);
                        installer.setAction("com.android.launcher.action.INSTALL_SHORTCUT")
                        mActivity.sendBroadcast(installer)
                    }
                Looper.loop();
            }
        }.start()
    }

    companion object {

        @SuppressLint("NewApi")
        fun isExistShortcutInfo(shortcutId: String?): Boolean {
            val shortcutManager = mActivity.getSystemService(ShortcutManager::class.java)

            val shortcutInfoList = shortcutManager!!.getPinnedShortcuts()

            for (info in shortcutInfoList) {
                if (info.getId() == shortcutId) {

                }
                if (info.getShortLabel() == shortcutId) {
                    return true
                }
            }
            return false
        }
    }
}
