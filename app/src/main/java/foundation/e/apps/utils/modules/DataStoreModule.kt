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

package foundation.e.apps.utils.modules

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.utils.enums.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreModule @Inject constructor(
    @ApplicationContext
    private val context: Context,
    private val gson: Gson
) {

    private val preferenceDataStoreName = "Settings"
    private val Context.dataStore by preferencesDataStore(preferenceDataStoreName)

    private val AUTHDATA = stringPreferencesKey("authData")
    private val EMAIL = stringPreferencesKey("email")
    private val OAUTHTOKEN = stringPreferencesKey("oauthtoken")
    private val USERTYPE = stringPreferencesKey("userType")
    private val TOCSTATUS = booleanPreferencesKey("tocStatus")

    val authData = context.dataStore.data.map { it[AUTHDATA] ?: "" }
    val emailData = context.dataStore.data.map { it[EMAIL] ?: "" }
    val aasToken = context.dataStore.data.map { it[OAUTHTOKEN] ?: "" }
    val userType = context.dataStore.data.map { it[USERTYPE] ?: "" }
    val tocStatus = context.dataStore.data.map { it[TOCSTATUS] ?: false }

    /**
     * Allows to save gplay API token data into datastore
     */
    suspend fun saveCredentials(authData: AuthData) {
        context.dataStore.edit {
            it[AUTHDATA] = gson.toJson(authData)
        }
    }

    suspend fun destroyCredentials() {
        context.dataStore.edit {
            it[AUTHDATA] = ""
        }
    }

    /**
     * TOC status
     */
    suspend fun saveTOCStatus(status: Boolean) {
        context.dataStore.edit {
            it[TOCSTATUS] = status
        }
    }

    /**
     * User auth type
     */
    suspend fun saveUserType(user: User) {
        context.dataStore.edit {
            it[USERTYPE] = user.name
        }
    }

    fun getAuthDataSync(): String {
        return runBlocking {
            authData.first()
        }
    }

    suspend fun saveEmail(email: String, token: String) {
        context.dataStore.edit {
            it[EMAIL] = email
            it[OAUTHTOKEN] = token
        }
    }

    suspend fun getEmail(): String {
        return emailData.first()
    }

    suspend fun getAASToken(): String {
        return aasToken.first()
    }
}
