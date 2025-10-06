package mm.zamiec.garpom.ui

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.runtime.rememberSceneSetupNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import mm.zamiec.garpom.ui.navigation.AlarmConfig
import mm.zamiec.garpom.ui.navigation.AppNavigationBar
import mm.zamiec.garpom.ui.navigation.Auth
import mm.zamiec.garpom.ui.navigation.BottomNavDestination
import mm.zamiec.garpom.ui.navigation.Measurement
import mm.zamiec.garpom.ui.navigation.Station
import mm.zamiec.garpom.ui.navigation.StationConfig
import mm.zamiec.garpom.ui.screens.alarms.AlarmsScreen
import mm.zamiec.garpom.ui.screens.configure.ConfigureScreen
import mm.zamiec.garpom.ui.screens.auth.AuthRouteController
import mm.zamiec.garpom.ui.screens.home.HomeScreen
import mm.zamiec.garpom.ui.screens.measurement.MeasurementScreen
import mm.zamiec.garpom.ui.screens.profile.ProfileScreen
import mm.zamiec.garpom.ui.screens.station.StationScreen
import mm.zamiec.garpom.ui.ui.theme.GarPomTheme

@AndroidEntryPoint
class Nav3Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GarPomTheme {
                NavComponent()
            }
        }

    }


    @Composable
    fun NavComponent() {
        val backStack = rememberNavBackStack(BottomNavDestination.Home)

        // Needed for properly handling back gesture when in sub-navdisplays
        val isInSubNavigation = remember { mutableStateOf(false) }

        Scaffold(
            bottomBar = {
                AppNavigationBar(
                    backStack.lastOrNull() as? BottomNavDestination,
                    onClick = { screen ->
                        backStack.clear()
                        backStack.add(screen)
                    }
                )
            }
        ) { innerPadding ->
//                Text(text = "backstack: " +backStack.toList())
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
                        entry<BottomNavDestination.Home> {
                            HomeScreen(
                                onStationSummaryClicked = { stationId ->
                                    backStack.add(Station(stationId))
                                },
                                onRecentAlarmOccurrenceClicked = { measurementId ->
                                    backStack.add(Measurement(measurementId))
                                }
                            )
                        }
                        entry<BottomNavDestination.Alarms> {
                            AlarmsScreen(
                                onRecentAlarmOccurrenceClicked = { measurementId ->
                                    backStack.add(Measurement(measurementId))
                                },
                                onAlarmClicked = { alarmId ->
                                    // TODO
                                }
                            )
                        }
                        entry<AlarmConfig> { key ->
                            StationScreen(stationId = key.id)
                        }
                        entry<BottomNavDestination.Configure> {
                            ConfigureScreen(
                                onUnableToConfigure = {
                                    backStack.clear()
                                    backStack.add(BottomNavDestination.Home)
                                }
                            )
                        }
                        entry<StationConfig> { key ->
                            StationScreen(stationId = key.id)
                        }
                        entry<BottomNavDestination.Profile> {
                            ProfileScreen(onNavigateToAuth = {
                                backStack.add(Auth)
                            })
                        }
                        entry<Auth> {
                            AuthRouteController(onAuthSuccess = {
                                backStack.removeLastOrNull()
                                Log.d("Main", "AUTH SUCCESS")
                            },
                            isInSubNavigation = isInSubNavigation)
                        }
                        entry<Station> { key ->
                            StationScreen(stationId = key.id)
                        }
                        entry<Measurement> { key ->
                            MeasurementScreen(
                                measurementId = key.id,
                                onAlarmClick = {
//                                    backStack.add() TODO
                                },
                                onBack = {
                                    backStack.removeLastOrNull()
                                }
                            )
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