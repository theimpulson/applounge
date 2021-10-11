package foundation.e.apps.api.gplay.token

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {

    /**
     * Provides an instance of Retrofit to work with Token API
     * @return instance of [TokenInterface]
     */
    @Singleton
    @Provides
    fun provideTokenInterface(): TokenInterface {
        return Retrofit.Builder()
            .baseUrl(TokenInterface.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TokenInterface::class.java)
    }
}