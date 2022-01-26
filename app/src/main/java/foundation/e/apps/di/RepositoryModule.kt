package foundation.e.apps.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import foundation.e.apps.api.exodus.repositories.ITrackerRepository
import foundation.e.apps.api.exodus.repositories.TrackerRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {
    @Singleton
    @Binds
    fun getRepositoryModule(trackerRepositoryImpl: TrackerRepositoryImpl): ITrackerRepository
}