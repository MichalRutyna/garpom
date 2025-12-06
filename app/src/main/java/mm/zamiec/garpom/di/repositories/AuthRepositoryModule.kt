package mm.zamiec.garpom.di.repositories

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mm.zamiec.garpom.data.auth.AppUserRepository
import mm.zamiec.garpom.data.auth.AuthRepository
import mm.zamiec.garpom.data.interfaces.IAuthRepository
import mm.zamiec.garpom.domain.usecase.TokenUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthRepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        serverInteractor: TokenUseCase,
        appUserRepository: AppUserRepository
    ): IAuthRepository = AuthRepository(firebaseAuth, serverInteractor, appUserRepository)
}
