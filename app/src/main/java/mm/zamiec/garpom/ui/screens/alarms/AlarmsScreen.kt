package mm.zamiec.garpom.ui.screens.alarms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.state.AlarmsScreenState
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.ui.screens.measurement.MeasurementScreenViewModel

@Composable
fun AlarmsScreen(
     alarmsScreenViewModel: AlarmsScreenViewModel = hiltViewModel(),
     onRecentAlarmOccurrenceClicked: (String) -> Unit,
) {
    val uiState: AlarmsScreenState by alarmsScreenViewModel.uiState.collectAsState(AlarmsScreenState())


    Column {
        Text("Your alarms:", style = MaterialTheme.typography.headlineLarge)
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            uiState.stationAlarmsList.forEach {
                item() {
                    Text(it.stationName, style = MaterialTheme.typography.titleSmall)
                }
                items(it.alarmList) { alarm ->
                    Text(alarm.name)
                    HorizontalDivider(Modifier.padding(horizontal = 20.dp))
                }
            }

        }
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))

        Spacer(Modifier.height(50.dp))

        Text("Recent alarm occurrences:", style = MaterialTheme.typography.headlineLarge)
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(uiState.recentAlarmOccurrencesList) { alarmOccurrence ->
                Row(
                    Modifier.padding(10.dp).clickable(onClick = {
                        onRecentAlarmOccurrenceClicked(alarmOccurrence.measurementId)
                    }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(alarmOccurrence.alarmName, style = MaterialTheme.typography.titleSmall)

                }
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }

        }
        Spacer(Modifier.height(50.dp))

        Text("All alarm occurrences:", style = MaterialTheme.typography.headlineLarge)
        HorizontalDivider(Modifier.padding(horizontal = 10.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(uiState.allAlarmOccurrencesList) { alarmOccurrence ->
                Text(alarmOccurrence.alarmName, style = MaterialTheme.typography.titleSmall)

                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }

        }
        Spacer(Modifier.height(50.dp))
    }
}