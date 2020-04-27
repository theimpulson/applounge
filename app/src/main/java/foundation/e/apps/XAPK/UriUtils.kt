package foundation.e.apps.XAPK

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import foundation.e.apps.BuildConfig
import java.io.File

object UriUtils {
    private val fileProviderPath by lazy { "${BuildConfig.APPLICATION_ID}.fileprovider" }

    fun fromFileProvider(mContext: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(mContext, fileProviderPath, file)
        } else {
            Uri.fromFile(file)
        }
    }
}
