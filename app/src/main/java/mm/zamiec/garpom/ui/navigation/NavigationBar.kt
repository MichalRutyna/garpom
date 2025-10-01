package mm.zamiec.garpom.ui.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun AppNavigationBar(
    selectedScreen : BottomNavDestination?,
    onClick: (BottomNavDestination) -> Unit
) {
    NavigationBar(windowInsets = NavigationBarDefaults.windowInsets) {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = screen.contentDescription
                    )
                },
                label = { Text(screen.label) },
                selected = selectedScreen == screen,
                onClick = { onClick(screen) }
            )

        }
    }
}