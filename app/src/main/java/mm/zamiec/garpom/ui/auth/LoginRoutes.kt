package mm.zamiec.garpom.ui.auth

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface LoginRoutes : NavKey {

    @Serializable
    data object PhoneRoute : LoginRoutes

}