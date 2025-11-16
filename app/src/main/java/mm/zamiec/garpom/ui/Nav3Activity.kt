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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
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
import mm.zamiec.garpom.ui.navigation.AlarmConfig
import mm.zamiec.garpom.ui.navigation.AppNavigationBar
import mm.zamiec.garpom.ui.navigation.Auth
import mm.zamiec.garpom.ui.navigation.BottomNavDestination
import mm.zamiec.garpom.ui.navigation.MeasurementScreen
import mm.zamiec.garpom.ui.navigation.Station
import mm.zamiec.garpom.ui.navigation.StationConfig
import mm.zamiec.garpom.ui.screens.alarm_config.AlarmConfigScreen
import mm.zamiec.garpom.ui.screens.alarm_config.alarmConfigScaffoldElements
import mm.zamiec.garpom.ui.screens.alarms.AlarmsScreen
import mm.zamiec.garpom.ui.screens.alarms.alarmsScreenScaffoldElements
import mm.zamiec.garpom.ui.screens.configure.ConfigureScreen
import mm.zamiec.garpom.ui.screens.auth.AuthRouteController
import mm.zamiec.garpom.ui.screens.home.HomeScreen
import mm.zamiec.garpom.ui.screens.measurement.MeasurementScreen
import mm.zamiec.garpom.ui.screens.measurement.measurementScreenScaffoldElements
import mm.zamiec.garpom.ui.screens.profile.ProfileScreen
import mm.zamiec.garpom.ui.screens.station.StationScreen
import mm.zamiec.garpom.ui.screens.station.stationScaffoldElements
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
        val currentEntry = backStack.lastOrNull()

        // Needed for properly handling back gesture when in sub-navdisplays
        val isInSubNavigation = remember { mutableStateOf(false) }

        // Used for hoisting scaffold elements from specific destinations
        var scaffoldElements by remember { mutableStateOf(ScaffoldElements()) }

        Scaffold(
            topBar = { scaffoldElements.topBar?.invoke() },
            floatingActionButton = { scaffoldElements.fab?.invoke() },
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
                            scaffoldElements = ScaffoldElements()
                            HomeScreen(
                                onStationSummaryClicked = { stationId ->
                                    backStack.add(Station(stationId))
                                },
                                onRecentAlarmOccurrenceClicked = { measurementId ->
                                    backStack.add(MeasurementScreen(measurementId))
                                }
                            )
                        }
                        entry<BottomNavDestination.Alarms> {
                            scaffoldElements = alarmsScreenScaffoldElements(
                                    onCreateAlarmClicked = { backStack.add(AlarmConfig("")) })
                            AlarmsScreen(
                                onRecentAlarmOccurrenceClicked = { measurementId ->
                                    backStack.add(MeasurementScreen(measurementId))
                                },
                                onAlarmClicked = { alarmId ->
                                    backStack.add(AlarmConfig(alarmId))
                                },

                                onStationClicked = { stationId ->
                                    backStack.add(Station(stationId))
                                }
                            )
                        }
                        entry<AlarmConfig> { key ->
                            scaffoldElements = alarmConfigScaffoldElements(key.id, onBack = { backStack.removeLastOrNull() })
                            AlarmConfigScreen(
                                alarmId = key.id,
                                onBack = { backStack.removeLastOrNull() }
                            )
                        }
                        entry<BottomNavDestination.Configure> {
                            scaffoldElements = ScaffoldElements()
                            ConfigureScreen(
                                onUnableToConfigure = {
                                    backStack.clear()
                                    backStack.add(BottomNavDestination.Home)
                                }
                            )
                        }
                        entry<StationConfig> { key ->
                            // TODO station config
                        }
                        entry<BottomNavDestination.Profile> {
                            scaffoldElements = ScaffoldElements()
                            ProfileScreen(onNavigateToAuth = {
                                backStack.add(Auth)
                            })
                        }
                        entry<Auth> {
                            scaffoldElements = ScaffoldElements()
                            AuthRouteController(onAuthSuccess = {
                                backStack.removeLastOrNull()
                                Log.d("Main", "AUTH SUCCESS")
                            },
                            isInSubNavigation = isInSubNavigation)
                        }
                        entry<Station> { key ->
                            scaffoldElements = stationScaffoldElements(key.id, onBack = { backStack.removeLastOrNull() })
                            StationScreen(
                                stationId = key.id,
                                onMeasurementClicked = { measurementId ->
                                    backStack.add(MeasurementScreen(measurementId))
                                },
                                onErrorClicked = {
                                    // TODO go to station
                                },
                                onBack = {
                                    backStack.removeLastOrNull()
                                }
                            )
                        }
                        entry<MeasurementScreen> { key ->
                            scaffoldElements = measurementScreenScaffoldElements(key.id, onBack = { backStack.removeLastOrNull() })
                            MeasurementScreen(
                                measurementId = key.id,
                                onAlarmClick = {
                                    // TODO go to alarm config
                                },
                                onBack = {
                                    backStack.removeLastOrNull()
                                }
                            )
                        }
                    },
//                    transitionSpec =
//                        {
//                            slideInHorizontally(initialOffsetX = { it }) togetherWith
//                                    slideOutHorizontally(targetOffsetX = { it })
//                        },
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