package mm.zamiec.garpom.ui.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.state.HomeState
import mm.zamiec.garpom.domain.model.state.StationSummary
import mm.zamiec.garpom.ui.ui.theme.GarPomTheme

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    onStationSummaryClicked: (String) -> Unit,
    onRecentAlarmOccurrenceClicked: (String) -> Unit,
) {
    val uiState: HomeState by homeViewModel.uiState.collectAsState(HomeState())

    HomeScreenContent(uiState, onStationSummaryClicked, onRecentAlarmOccurrenceClicked)
}

@Composable
private fun HomeScreenContent(
    uiState: HomeState,
    onStationSummaryClicked: (String) -> Unit,
    onRecentAlarmOccurrenceClicked: (String) -> Unit,
) {
    Column {
        val text =
            if (uiState.isAnonymous) "Welcome!"
            else "Welcome, ${uiState.username}!"
        Text(
            text,
            Modifier.fillMaxWidth().padding(10.dp),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(Modifier.padding(horizontal = 10.dp))

        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(uiState.stations) { station ->
                Row (
                    Modifier.padding(10.dp).clickable(onClick = {
                        onStationSummaryClicked(station.stationId)
                    }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(station.name, Modifier.weight(1f))


                    if (station.hasNotification) {
                        Icon(
                            Icons.Filled.Notifications,
                            "You have a notification"
                        )
                    }
                    if (station.hasError) {
                        Icon(
                            Icons.Filled.Warning,
                            "This station has a problem"
                        )
                    }
                     Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        "Show details")


                }
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }
        }

        Spacer(Modifier.height(50.dp))

        Text(
            "Recent alarms:",
            Modifier.fillMaxWidth().padding(10.dp),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        LazyColumn {
            items(uiState.recentAlarmOccurrences) { alarm ->
                Row (
                    Modifier.padding(10.dp).clickable(onClick = {
                        onRecentAlarmOccurrenceClicked(alarm.measurementId)
                    }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(alarm.alarmName,
//                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))


                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        "Show details")


                }
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val uiState = HomeState(
        false,
        "preview",
        listOf(
            StationSummary(stationId = "1", name = "South station"),
            StationSummary(stationId = "2", name = "North station", hasError = true),
            StationSummary(stationId = "3", name = "East station", hasNotification = true),
            StationSummary(
                stationId = "4",
                name = "West station",
                hasError = true,
                hasNotification = true
            ),
        )
    )
    GarPomTheme {
        HomeScreenContent(uiState, {}, {})
    }
}