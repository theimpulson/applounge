package foundation.e.apps.utils

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.os.Environment
import android.os.StatFs
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import foundation.e.apps.R
import java.lang.reflect.Modifier
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonUtilsModule {

    /**
     * Check supported ABIs by device
     * @return An ordered list of ABIs supported by this device
     */
    @Singleton
    @Provides
    fun provideArchitecture(): Array<String> {
        return Build.SUPPORTED_ABIS
    }

    /**
     * Check system build type
     * @return Type of the system build, like "release" or "test"
     */
    @Singleton
    @Provides
    @Named("buildType")
    fun provideBuildType(): String {
        return Build.TAGS.split("-")[0]
    }

    /**
     * Path to application's internal cache directory
     * @param context [Context]
     * @return absolute path to cache directory or empty string if not available
     */
    @Singleton
    @Provides
    @Named("cacheDir")
    fun provideCacheDir(@ApplicationContext context: Context): String {
        return context.cacheDir?.absolutePath.let {
            if (it.isNullOrBlank()) "" else it
        }
    }

    /**
     * Available free space on the device in bytes
     * @return available bytes in the data directory
     */
    @Singleton
    @Provides
    fun provideAvailableMegaBytes(): Long {
        val stat = StatFs(Environment.getDataDirectory().path)
        return stat.availableBytes
    }

    /**
     * Provides an instance of [Gson] to work with
     * @return an instance of [Gson]
     */
    @Singleton
    @Provides
    fun provideGsonInstance(): Gson {
        return GsonBuilder()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .create()
    }
}
