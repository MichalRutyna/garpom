package mm.zamiec.garpom.ui.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mm.zamiec.garpom.ui.navigation.Destination

@Composable
fun InitialLoginScreen(
    onClose: () -> Unit,
) {
    val backStack = rememberNavBackStack(Destination.Home)

    NavDisplay(
        entryDecorators = listOf(
            rememberSceneSetupNavEntryDecorator(),
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        backStack = backStack,
        onBack = { onClose() },
        entryProvider = entryProvider {
            entry<LoginRoutes.PhoneRoute> {
                Text("Phone here")
            }
        }
    )

}