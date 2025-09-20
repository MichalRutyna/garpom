package mm.zamiec.garpom.ui.screens.profile

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface AuthRoutes : NavKey {

    @Serializable
    data object PhoneInput : AuthRoutes

    @Serializable
    data object CodeInput : AuthRoutes


}