package foundation.e.apps.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import foundation.e.apps.domain.repositories.IApplicationsRepository
import foundation.e.apps.repositories.ApplicationRepositoryImpl

@InstallIn(ViewModelComponent::class)
@Module
interface RepositoryModule {
    @Binds
    fun getApplicationsRepository(applicationRepositoryImpl: ApplicationRepositoryImpl): IApplicationsRepository
}