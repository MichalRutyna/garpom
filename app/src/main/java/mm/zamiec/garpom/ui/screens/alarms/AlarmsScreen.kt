package mm.zamiec.garpom.ui.screens.alarms

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.screens.home.RecentAlarmOccurrenceItemUiState
import mm.zamiec.garpom.ui.ui.theme.GarPomTheme

@Composable
fun AlarmsScreen(
     alarmsScreenViewModel: AlarmsScreenViewModel = hiltViewModel(),
     onRecentAlarmOccurrenceClicked: (String) -> Unit,
     onAlarmClicked: (String) -> Unit,
     onStationClicked: (String) -> Unit,
) {
    val uiState: AlarmsUiState by alarmsScreenViewModel.uiState.collectAsState(AlarmsUiState())

    AlarmsScreenContent(uiState, onRecentAlarmOccurrenceClicked, onAlarmClicked, onStationClicked)
}

@Composable
private fun AlarmsScreenContent(
    uiState: AlarmsUiState,
    onRecentAlarmOccurrenceClicked: (String) -> Unit,
    onAlarmClicked: (String) -> Unit,
    onStationClicked: (String) -> Unit,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        item {
            AlarmTitle()
            HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        }
        uiState.stationAlarmsList.forEach {
            item {
                StationItem(onStationClicked, it)
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }
            items(it.alarmList) { alarm ->
                AlarmItem(onAlarmClicked, alarm)
                HorizontalDivider(Modifier.padding(horizontal = 20.dp))
            }
        }
        item {
            Spacer(Modifier.height(50.dp))
            RecentOccurrencesTitle()
            HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        }
        items(uiState.recentAlarmOccurrencesList) { alarmOccurrence ->
            RecentOccurrenceItem(onRecentAlarmOccurrenceClicked, alarmOccurrence)
            HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        }

        item {
            Spacer(Modifier.height(50.dp))
            AllOccurrencesTitle()
            HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        }
        items(uiState.allAlarmOccurrencesList) { alarmOccurrence ->
            AlarmOccurrenceItem(onRecentAlarmOccurrenceClicked, alarmOccurrence)
            HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        }
    }
}

@Composable
private fun AlarmOccurrenceItem(
    onRecentAlarmOccurrenceClicked: (String) -> Unit,
    alarmOccurrence: AlarmOccurrenceItemUiState
) {
    Row(
        Modifier
            .padding(10.dp)
            .clickable(onClick = {
                onRecentAlarmOccurrenceClicked(alarmOccurrence.measurementId)
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            alarmOccurrence.alarmName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            "Show details"
        )
    }
}

@Composable
private fun AllOccurrencesTitle() {
    Text(
        "All alarm occurrences:",
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(10.dp),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun RecentOccurrenceItem(
    onRecentAlarmOccurrenceClicked: (String) -> Unit,
    alarmOccurrence: RecentAlarmOccurrenceItemUiState
) {
    Row(
        Modifier
            .padding(10.dp)
            .clickable(onClick = {
                onRecentAlarmOccurrenceClicked(alarmOccurrence.measurementId)
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            alarmOccurrence.alarmName,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            "Show details"
        )

    }
}

@Composable
private fun RecentOccurrencesTitle() {
    Text(
        "Recent alarm occurrences (last 7 days):",
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(10.dp),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun AlarmItem(
    onAlarmClicked: (String) -> Unit,
    alarm: AlarmSummaryItemUiState
) {
    Row(
        Modifier
            .padding(top = 5.dp, bottom = 5.dp)
            .padding(start = 30.dp, end = 20.dp)
            .clickable(onClick = {
                onAlarmClicked(alarm.alarmId)
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            alarm.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Filled.Settings,
            "Show details",
            modifier = Modifier.scale(0.75f)
        )
    }
}

@Composable
private fun StationItem(
    onStationClicked: (String) -> Unit,
    state: StationAlarmsItemUiState
) {
    Row(
        Modifier
            .padding(10.dp)
            .padding(end = 10.dp)
            .clickable(onClick = {
                onStationClicked(state.stationId)
            }),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.Menu,
            "Station",
            modifier = Modifier.padding(end = 3.dp)
        )
        Text(
            state.stationName,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            "Show details"
        )
    }
}

@Composable
private fun AlarmTitle() {
    Text(
        "Your alarms:",
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(10.dp),
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val uiState = AlarmsUiState(
        listOf(
            StationAlarmsItemUiState(
                "Test station",
                alarmList = listOf(
                    AlarmSummaryItemUiState(
                        "",
                        "Test alarm",
                        true
                    ),
                    AlarmSummaryItemUiState(
                        "",
                        "Test alarm",
                        true
                    ),
                    AlarmSummaryItemUiState(
                        "",
                        "Test alarm",
                        true
                    ),
                )
            ),
            StationAlarmsItemUiState(
                "Test station",
                alarmList = listOf(
                    AlarmSummaryItemUiState(
                        "",
                        "Test alarm",
                        true
                    )
                )
            ),
        ),
        listOf(
            RecentAlarmOccurrenceItemUiState(
                "Test alarm",
                ""
            )
        ),
        listOf(
            AlarmOccurrenceItemUiState(
                "Test alarm",
                ""
            )
        )
    )
    GarPomTheme {
        AlarmsScreenContent(uiState, {}, {}, {})
    }
}