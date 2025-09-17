package mm.zamiec.garpom.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import mm.zamiec.garpom.bluetooth.BluetoothViewModel
import mm.zamiec.garpom.firebase.log_token
import mm.zamiec.garpom.permissions.NotificationPermissionViewModel
import mm.zamiec.garpom.ui.navigation.AppNavigationBar
import mm.zamiec.garpom.ui.navigation.Destination

class Nav3Activity : ComponentActivity() {

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NavExample()

        }
    }


    @Composable
    fun NavExample(
        notificationViewModel: NotificationPermissionViewModel = viewModel(),
        bluetoothViewModel: BluetoothViewModel = viewModel()
    ) {
        val backStack = rememberNavBackStack(Destination.Home)

        val context = LocalContext.current
        val activity: Nav3Activity? = LocalActivity.current as Nav3Activity?

        val notificationPermissionGranted by notificationViewModel.isPermissionGranted.collectAsState()
        val notificationPreferenceEnabled by notificationViewModel.areNotificationsEnabled.collectAsState()

//        val launcher = rememberLauncherForActivityResult(
//            contract = ActivityResultContracts.RequestPermission()
//        ) { granted ->
//            notificationViewModel.updatePermission(granted)
//        }

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
                        Text("stack: ${backStack.toList() }}")
                        entry<Destination.Home> {
                            Column {
                                Text("Home")
                            }
                        }
                        entry<Destination.Alarms> {
                            Column {
                                Text("Alarms")
                                Button(
                                    onClick = {
                                        log_token(activity!!)
                                    }
                                ) {
                                    Text("Show token")
                                }
                                LifecycleResumeEffect(Unit) {
                                    notificationViewModel.checkPermissions()
                                    onPauseOrDispose {  }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Push notifications: ")
                                    Switch(
                                        checked = notificationPreferenceEnabled,
                                        onCheckedChange = { isChecked ->
                                            notificationViewModel.checkPermissions()
                                            if (isChecked) {
                                                if (notificationPermissionGranted) {
                                                    notificationViewModel.updatePreference(true)
                                                }
                                                else {
                                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                                    }
                                                    context.startActivity(intent)
                                                }
                                            } else {
                                                notificationViewModel.updatePreference(false)
                                            }
                                        }
                                    )
                                }

                                Button(onClick = {
                                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                    }
                                    context.startActivity(intent)
                                }) {
                                    Text("Go to settings")
                                }
                                Text("Permission: $notificationPermissionGranted")
                                Text("Preference: $notificationPreferenceEnabled")
                            }
                        }
                        entry<Destination.Configure> {
                            ConfigureScreen(bluetoothViewModel, onUnableToConfigure = {
                                backStack.clear()
                                backStack.add(Destination.Home)
                            })
                        }
                        entry<Destination.Profile> {
                            Column {
                                Text("Profile")
                            }
                        }
                    }
                )
        }
    }
}