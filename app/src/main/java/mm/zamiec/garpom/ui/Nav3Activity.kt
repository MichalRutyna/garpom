package mm.zamiec.garpom.ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import dagger.hilt.android.AndroidEntryPoint
import mm.zamiec.garpom.auth.AuthRepository
import mm.zamiec.garpom.auth.AuthViewModel
import mm.zamiec.garpom.bluetooth.BluetoothViewModel
import mm.zamiec.garpom.firebase.FirebaseMessagingViewModel
import mm.zamiec.garpom.permissions.NotificationPermissionViewModel
import mm.zamiec.garpom.ui.navigation.AppNavigationBar
import mm.zamiec.garpom.ui.navigation.Destination
import mm.zamiec.garpom.ui.screens.alarms.AlarmsScreen
import mm.zamiec.garpom.ui.screens.configure.ConfigureScreen
import mm.zamiec.garpom.ui.screens.profile.ProfileScreen
import mm.zamiec.garpom.ui.ui.theme.GarPomTheme
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class Nav3Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GarPomTheme {
                NavExample()
            }
        }
    }


    @Composable
    fun NavExample(
        notificationViewModel: NotificationPermissionViewModel = viewModel(),
        bluetoothViewModel: BluetoothViewModel = viewModel(),
        firebaseMessagingViewModel: FirebaseMessagingViewModel = viewModel(),
        authViewModel: AuthViewModel = viewModel(),
        authRepository: AuthRepository = AuthRepository(Firebase.auth),
    ) {
        val backStack = rememberNavBackStack(Destination.Home)

        Scaffold(
            bottomBar = {
                AppNavigationBar(
                    backStack.lastOrNull() as? Destination,
                    onClick = { screen ->
                        backStack.clear()
                        backStack.add(screen)
                    }
                )
            }
        ) { innerPadding ->
                NavDisplay(
                    modifier = Modifier.padding(innerPadding),
                    entryDecorators = listOf(
                        rememberSceneSetupNavEntryDecorator(),
                        rememberSavedStateNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator()
                    ),
                    backStack = backStack,
                    onBack = { backStack.removeLastOrNull() },
                    entryProvider = entryProvider {
                        entry<Destination.Home> {
                            Column {
                                Text("Home")
                            }
                        }
                        entry<Destination.Alarms> {
                            AlarmsScreen(
                                notificationViewModel,
                                firebaseMessagingViewModel
                            )
                        }
                        entry<Destination.Configure> {
                            ConfigureScreen(
                                bluetoothViewModel,
                                onUnableToConfigure = {
                                    backStack.clear()
                                    backStack.add(Destination.Home)
                                }
                            )
                        }
                        entry<Destination.Profile> {
                            ProfileScreen()
                        }
                    }
                )
        }
    }
}