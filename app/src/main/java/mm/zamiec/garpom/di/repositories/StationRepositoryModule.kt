package mm.zamiec.garpom.di.repositories

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mm.zamiec.garpom.data.dataRepositories.StationRepository
import mm.zamiec.garpom.data.interfaces.IStationRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StationRepositoryModule {

    @Provides
    @Singleton
    fun provideStationRepository(): IStationRepository = StationRepository()
}
