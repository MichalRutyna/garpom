package mm.zamiec.garpom.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import mm.zamiec.garpom.ui.navigation.AppNavigationBar
import mm.zamiec.garpom.ui.navigation.Destination
import mm.zamiec.garpom.ui.screens.alarms.AlarmsScreen
import mm.zamiec.garpom.ui.screens.configure.ConfigureScreen
import mm.zamiec.garpom.ui.screens.profile.AuthRouteController
import mm.zamiec.garpom.ui.screens.profile.ProfileScreen
import mm.zamiec.garpom.ui.ui.theme.GarPomTheme

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
    fun NavExample() {
        val backStack = rememberNavBackStack(Destination.Home)

        var isInSubNavigation = remember { mutableStateOf(false) }

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
                Text(text = "backstack: " +backStack.toList())
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
                            AlarmsScreen()
                        }
                        entry<Destination.Configure> {
                            ConfigureScreen(
                                onUnableToConfigure = {
                                    backStack.clear()
                                    backStack.add(Destination.Home)
                                }
                            )
                        }
                        entry<Destination.Profile> {
                            ProfileScreen(onNavigateToAuth = {
                                backStack.add(Destination.Auth)
                            })
                        }
                        entry<Destination.Auth> {
                            AuthRouteController(onAuthSuccess = {
                                backStack.removeLastOrNull()
                                Log.d("Main", "AUTH SUCCESS")
                            },
                            isInSubNavigation = isInSubNavigation)
                        }
                    },
                    predictivePopTransitionSpec =
                        if (!isInSubNavigation.value) {
                            NavDisplay.defaultPredictivePopTransitionSpec
                        } else {
                            {
                                slideInHorizontally(initialOffsetX = { 0 }) togetherWith
                                        slideOutHorizontally(targetOffsetX = { 0 })
                            }
                        }
                )
        }
    }
}