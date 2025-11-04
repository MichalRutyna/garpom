package mm.zamiec.garpom.ui.screens.measurement

import android.icu.text.SimpleDateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.measurement.components.FireCard
import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementCard
import mm.zamiec.garpom.ui.screens.measurement.components.MeasurementCardFactory
import java.time.Instant
import java.util.Date
import java.util.Locale

@Composable
fun MeasurementScreen(
    measurementId: String,
    measurementViewModel: MeasurementScreenViewModel = hiltViewModel(
        creationCallback = { factory: MeasurementScreenViewModel.Factory ->
            factory.create(measurementId)
        }
    ),
    onAlarmClick: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState: MeasurementScreenState by measurementViewModel.uiState.collectAsState()

    when (uiState) {
        is MeasurementScreenState.Loading ->
            MeasurementLoadingScreen()
        is MeasurementScreenState.MeasurementData -> {
            MeasurementDataScreen(
                uiState as MeasurementScreenState.MeasurementData,
                onAlarmClick,
                measurementViewModel.fabMenuExpanded.collectAsState().value,
                measurementViewModel::toggleFabMenu
            )
        }
        is MeasurementScreenState.Error ->
            MeasurementErrorScreen(uiState as MeasurementScreenState.Error)
    }
}

@Composable
private fun MeasurementErrorScreen(uiState: MeasurementScreenState.Error) {
    Text("Error: ${uiState.message}")
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MeasurementDataScreen(
    uiState: MeasurementScreenState.MeasurementData,
    onAlarmClick: (String) -> Unit,
    fabExpanded: Boolean,
    toggleFab: () -> Unit,
) {
    BackHandler(fabExpanded) { toggleFab() }

    LazyColumn{
        item {
            DateSubtitle(uiState)
            Spacer(Modifier.padding(top = 10.dp))
        }
        items(uiState.cards) { card ->
            MeasurementCard(card, onAlarmClick)
            Spacer(Modifier.padding(top = 10.dp))
        }
        item {
            FireCard(uiState)
        }
    }
}

@Composable
private fun FireCard(uiState: MeasurementScreenState.MeasurementData) {
    HorizontalDivider()
    if (uiState.fire.value) {
        Box(
            modifier = Modifier
                .background(Color.Red.copy(alpha = 0.4f))
                .fillMaxWidth()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Warning,
                    "FIRE!"
                )
                Text("Station detected a fire!")
            }
        }
    } else {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxWidth()
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    "All good"
                )
                Text("No fire detected! ")
            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun MeasurementCard(
    card: MeasurementCard,
    onAlarmClick: (String) -> Unit
) {
    HorizontalDivider()
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(10.dp)
    ) {
        Column {
            Text(
                card.title,
                color = MaterialTheme.colorScheme.onSurface
            )
            Box(contentAlignment = Alignment.CenterStart) {
                if (card.triggeredAlarms.isEmpty()) {
                    Icon(
                        Icons.Rounded.Check,
                        "Ok",
                        modifier = Modifier.padding(start = 15.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Column {
                        for (alarm in card.triggeredAlarms) {
                            Row(
                                Modifier.clickable(onClick = {
                                    onAlarmClick(alarm.alarmId)
                                }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Rounded.Warning,
                                    "Alarm",
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    alarm.alarmName,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }

                Text(
                    "" + card.value + card.unit,
                    Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )

            }
        }
    }
    HorizontalDivider()
}

@Composable
private fun DateSubtitle(uiState: MeasurementScreenState.MeasurementData) {
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 10.dp)
    ) {
        Text(
            text = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale.getDefault())
                .format(uiState.date)
                .replaceFirstChar {
                    it.uppercaseChar()
                },
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MeasurementLoadingScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()) {
        LoadingIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val uistate = MeasurementScreenState.MeasurementData(
        stationName = "Test test test station",
        date = Date.from(Instant.now()),
        cards = listOf(
            MeasurementCardFactory.create(
                Parameter.TEMPERATURE,
                21.3,
                listOf(
                    TriggeredAlarm(
                        "", "Test alarm"
                    ),
                    TriggeredAlarm(
                        "", "Test alarm2"
                    ),
                    TriggeredAlarm(
                        "", "Test alarm3"
                    ),
                    TriggeredAlarm(
                        "", "Test alarm4"
                    ),
                )
            ),
            MeasurementCardFactory.create(
                Parameter.AIR_HUMIDITY,
                12.1,
                listOf(
                )
            )
        ),
        fire = FireCard(true)
    )
    MeasurementDataScreen(
        uistate,
        {},
        false,
        {}
    )
//    MeasurementLoadingScreen()
}