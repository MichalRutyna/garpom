package mm.zamiec.garpom.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppNavigationBar(
    selectedScreen : BottomNavDestination?,
    onClick: (BottomNavDestination) -> Unit
) {
    Column {
        HorizontalDivider()
        NavigationBar(
            windowInsets = NavigationBarDefaults.windowInsets
        ) {
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
}

@Preview
@Composable
private fun Preview() {
    AppNavigationBar(null, {})
}