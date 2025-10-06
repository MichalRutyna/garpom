package mm.zamiec.garpom.ui.screens.alarms

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
import androidx.compose.material.icons.outlined.Menu
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
import mm.zamiec.garpom.domain.model.state.AlarmOccurrence
import mm.zamiec.garpom.domain.model.state.AlarmSummary
import mm.zamiec.garpom.domain.model.state.AlarmsScreenState
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.RecentAlarmOccurrence
import mm.zamiec.garpom.domain.model.state.StationAlarms
import mm.zamiec.garpom.ui.screens.measurement.MeasurementScreenViewModel
import mm.zamiec.garpom.ui.ui.theme.GarPomTheme

@Composable
fun AlarmsScreen(
     alarmsScreenViewModel: AlarmsScreenViewModel = hiltViewModel(),
     onRecentAlarmOccurrenceClicked: (String) -> Unit,
     onAlarmClicked: (String) -> Unit,
) {
    val uiState: AlarmsScreenState by alarmsScreenViewModel.uiState.collectAsState(AlarmsScreenState())

    AlarmsScreenContent(uiState, onRecentAlarmOccurrenceClicked, onAlarmClicked)
}

@Composable
private fun AlarmsScreenContent(
    uiState: AlarmsScreenState,
    onRecentAlarmOccurrenceClicked: (String) -> Unit,
    onAlarmClicked: (String) -> Unit,
) {
    Column {
        Text(
            "Your alarms:",
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            uiState.stationAlarmsList.forEach {
                item() {
                    Row(
                        Modifier
                            .padding(10.dp).padding(end = 10.dp)
                            .clickable(onClick = {
                            }),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Menu,
                            "Station",
                            modifier = Modifier.padding(end = 3.dp)
                        )
                        Text(
                            it.stationName,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Show details")
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 10.dp))
                }
                items(it.alarmList) { alarm ->
                    Row(
                        Modifier
                            .padding(top = 5.dp, bottom = 5.dp)
                            .padding(start = 30.dp, end = 10.dp)
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
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            "Show details")
                    }
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                }
            }

        }

        Spacer(Modifier.height(50.dp))

        Text(
            "Recent alarm occurrences:",
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(uiState.recentAlarmOccurrencesList) { alarmOccurrence ->
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
                        "Show details")

                }
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }

        }
        Spacer(Modifier.height(50.dp))

        Text(
            "All alarm occurrences:",
            Modifier
                .fillMaxWidth()
                .padding(10.dp),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(uiState.allAlarmOccurrencesList) { alarmOccurrence ->
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
                        "Show details")
                }
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }

        }
        Spacer(Modifier.height(50.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val uiState = AlarmsScreenState(
        listOf(
            StationAlarms(
                "Test station",
                listOf(
                    AlarmSummary(
                        "",
                        "Test alarm",
                        true
                    ),
                    AlarmSummary(
                        "",
                        "Test alarm",
                        true
                    ),
                    AlarmSummary(
                        "",
                        "Test alarm",
                        true
                    ),
                )
            ),
            StationAlarms(
                "Test station",
                listOf(
                    AlarmSummary(
                        "",
                        "Test alarm",
                        true
                    )
                )
            ),
        ),
        listOf(
            RecentAlarmOccurrence(
                "Test alarm",
                ""
            )
        ),
        listOf(
            AlarmOccurrence(
                "Test alarm",
                ""
            )
        )
    )
    GarPomTheme {
        AlarmsScreenContent(uiState, {}, {})
    }
}