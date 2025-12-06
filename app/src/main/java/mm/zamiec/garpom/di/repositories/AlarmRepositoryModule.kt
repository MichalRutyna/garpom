package mm.zamiec.garpom.di.repositories

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mm.zamiec.garpom.data.dataRepositories.AlarmConditionRepository
import mm.zamiec.garpom.data.dataRepositories.AlarmRepository
import mm.zamiec.garpom.data.interfaces.IAlarmRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AlarmRepositoryModule {

    @Provides
    @Singleton
    fun provideAlarmRepository(
        alarmConditionRepository: AlarmConditionRepository
    ): IAlarmRepository = AlarmRepository(alarmConditionRepository)
}