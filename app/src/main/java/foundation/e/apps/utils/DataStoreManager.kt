package foundation.e.apps.utils

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.api.gplay.token.data.Token
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class DataStoreManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val preferenceDataStoreName = "Settings"
    private val Context.dataStore by preferencesDataStore(preferenceDataStoreName)

    private val EMAIL = stringPreferencesKey("email")
    private val AASTOKEN = stringPreferencesKey("aastoken")

    val gplayAPIEmail = context.dataStore.data.map { it[EMAIL] }
    val gplayAPIAASToken = context.dataStore.data.map { it[AASTOKEN] }

    /**
     * Allows to save gplay API token data into datastore
     */
    suspend fun saveCredentials(token: Token) {
        context.dataStore.edit {
            it[EMAIL] = token.email
            it[AASTOKEN] = token.authToken
        }
    }

}