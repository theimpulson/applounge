/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
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
import android.os.Build
import android.os.Environment
import android.os.StatFs
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
     * Path to application's external cache directory
     * @param context [Context]
     * @return absolute path to cache directory or empty string if not available
     */
    @Singleton
    @Provides
    @Named("cacheDir")
    fun provideCacheDir(@ApplicationContext context: Context): String {
        return context.externalCacheDir?.absolutePath.let {
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
