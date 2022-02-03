package foundation.e.apps.utils

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.manager.database.fused.FusedDownload
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PWAManagerModule @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val URL = "URL"
        private const val SHORTCUT_ID = "SHORTCUT_ID"
        private const val TITLE = "TITLE"
        private const val ICON = "ICON"

        private const val PWA_NAME = "PWA_NAME"
        private const val PWA_ID = "PWA_ID"

        private const val PWA_PLAYER = "content://foundation.e.pwaplayer.provider/pwa"
        private const val VIEW_PWA = "foundation.e.blisslauncher.VIEW_PWA"
    }

    fun installPWAApp(fusedDownload: FusedDownload) {
        // Get bitmap and byteArray for icon
        val iconByteArray = Base64.decode(fusedDownload.iconByteArray, Base64.DEFAULT)
        val iconBitmap = BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.size)

        val values = ContentValues()
        values.apply {
            put(URL, fusedDownload.downloadLink)
            put(SHORTCUT_ID, fusedDownload.id)
            put(TITLE, fusedDownload.name)
            put(ICON, iconByteArray)
        }

        context.contentResolver.insert(Uri.parse(PWA_PLAYER), values)?.let {
            val databaseID = ContentUris.parseId(it)
            publishShortcut(fusedDownload, iconBitmap, databaseID)
        }
    }

    private fun publishShortcut(fusedDownload: FusedDownload, bitmap: Bitmap, databaseID: Long) {
        val intent = Intent().apply {
            action = VIEW_PWA
            data = Uri.parse(fusedDownload.downloadLink)
            putExtra(PWA_NAME, fusedDownload.name)
            putExtra(PWA_ID, databaseID)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS)
        }

        val shortcutInfo = ShortcutInfoCompat.Builder(context, fusedDownload.id)
            .setShortLabel(fusedDownload.name)
            .setIcon(IconCompat.createWithBitmap(bitmap))
            .setIntent(intent)
            .build()
        ShortcutManagerCompat.requestPinShortcut(context, shortcutInfo, null)
    }
}
