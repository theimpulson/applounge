package foundation.e.apps.api.fdroid

import foundation.e.apps.api.fdroid.models.FdroidEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FdroidRepository @Inject constructor(
    private val fdroidApi: FdroidApiInterface,
    private val fdroidDao: FdroidDao,
) {

    /**
     * Get Fdroid entity from DB is present.
     * If not present then make an API call, store the fetched result and return the result.
     *
     * Result may be null.
     */
    suspend fun getFdroidInfo(packageName: String): FdroidEntity? {
        return fdroidDao.getFdroidEntityFromPackageName(packageName)
            ?: fdroidApi.getFdroidInfoForPackage(packageName)?.let {
                FdroidEntity(packageName, it.authorName).also {
                    fdroidDao.saveFdroidEntity(it)
                }
            }
    }
}
