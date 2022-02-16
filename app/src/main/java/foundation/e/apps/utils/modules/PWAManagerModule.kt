package foundation.e.apps.utils.modules

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
import foundation.e.apps.manager.database.DatabaseRepository
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.utils.enums.Status
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PWAManagerModule @Inject constructor(
    @ApplicationContext private val context: Context,
    private val databaseRepository: DatabaseRepository
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

    suspend fun installPWAApp(fusedDownload: FusedDownload) {
        // Update status
        fusedDownload.status = Status.DOWNLOADING
        databaseRepository.updateDownload(fusedDownload)

        // Get bitmap and byteArray for icon
        val iconByteArray = Base64.decode(fusedDownload.iconByteArray, Base64.DEFAULT)
        val iconBitmap = BitmapFactory.decodeByteArray(iconByteArray, 0, iconByteArray.size)

        val values = ContentValues()
        values.apply {
            put(URL, fusedDownload.downloadURLList[0])
            put(SHORTCUT_ID, fusedDownload.id)
            put(TITLE, fusedDownload.name)
            put(ICON, iconByteArray)
        }

        context.contentResolver.insert(Uri.parse(PWA_PLAYER), values)?.let {
            val databaseID = ContentUris.parseId(it)
            publishShortcut(fusedDownload, iconBitmap, databaseID)
        }
    }

    private suspend fun publishShortcut(fusedDownload: FusedDownload, bitmap: Bitmap, databaseID: Long) {
        // Update status
        fusedDownload.status = Status.INSTALLING
        databaseRepository.updateDownload(fusedDownload)

        val intent = Intent().apply {
            action = VIEW_PWA
            data = Uri.parse(fusedDownload.downloadURLList[0])
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

        // Update status
        fusedDownload.status = Status.INSTALLED
        databaseRepository.updateDownload(fusedDownload)

        databaseRepository.deleteDownload(fusedDownload)
    }
}
