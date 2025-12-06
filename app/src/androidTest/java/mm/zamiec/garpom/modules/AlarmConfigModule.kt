package mm.zamiec.garpom.modules

import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import mm.zamiec.garpom.data.interfaces.IAlarmRepository
import mm.zamiec.garpom.di.repositories.AuthRepositoryModule
import mm.zamiec.garpom.data.interfaces.IAuthRepository
import mm.zamiec.garpom.data.interfaces.IStationRepository
import mm.zamiec.garpom.di.repositories.AlarmRepositoryModule
import mm.zamiec.garpom.di.repositories.StationRepositoryModule
import mm.zamiec.garpom.mocks.FakeAlarmRepository
import mm.zamiec.garpom.mocks.FakeAuthRepository
import mm.zamiec.garpom.mocks.FakeStationRepository

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AlarmRepositoryModule::class, AuthRepositoryModule::class, StationRepositoryModule::class]
)
abstract class TestRepositoryModule {
    @Binds
    abstract fun bindAuthRepository(fake: FakeAuthRepository): IAuthRepository

    @Binds
    abstract fun bindAlarmRepository(fake: FakeAlarmRepository): IAlarmRepository

    @Binds
    abstract fun bindStationRepository(fake: FakeStationRepository): IStationRepository
}