package mm.zamiec.garpom.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {
    val label: String
    val icon: ImageVector
    val contentDescription: String

    @Serializable
    data object Home : Destination {
        override val label = "Home"
        override val icon = Icons.Filled.Home
        override val contentDescription = "Main screen"
    }

    @Serializable
    data object Alarms : Destination {
        override val label = "Alarms"
        override val icon = Icons.Filled.Notifications
        override val contentDescription = "List of alarms"
    }

    @Serializable
    data object Configure : Destination {
        override val label = "Configure"
        override val icon = Icons.Filled.Settings
        override val contentDescription = "Configure a station"
    }

    @Serializable
    data object Profile : Destination {
        override val label = "Profile"
        override val icon = Icons.Filled.AccountCircle
        override val contentDescription = "Configure your profile"
    }
}

val bottomNavItems = listOf(
    Destination.Home,
    Destination.Alarms,
    Destination.Configure,
    Destination.Profile
)