package mm.zamiec.garpom.ui.screens.alarm_config

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.Parameter
import mm.zamiec.garpom.ui.screens.alarm_config.components.ActiveTimePicker
import mm.zamiec.garpom.ui.screens.alarm_config.components.ParameterCardContent
import mm.zamiec.garpom.ui.screens.alarm_config.components.SelectAdditionalStationDialog
import mm.zamiec.garpom.ui.screens.alarm_config.components.TitleBar
import java.time.Instant
import java.util.Calendar
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AlarmConfigScreen(
    alarmId: String,
    alarmConfigViewModel: AlarmConfigScreenViewModel = hiltViewModel(
        creationCallback = { factory: AlarmConfigScreenViewModel.Factory ->
            factory.create(alarmId)
        }
    ),
    onBack: () -> Unit,
) {
    val uiState: AlarmConfigUiState by alarmConfigViewModel.uiState.collectAsState()

    when (uiState) {
        is AlarmConfigUiState.Loading ->
        {}
        is AlarmConfigUiState.ConfigData -> {
            AlarmConfigContent(
                uiState as AlarmConfigUiState.ConfigData,
                onBack,
                {}) //TODO
        }
        is AlarmConfigUiState.Error ->
        {}
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmConfigContent(
    uiState: AlarmConfigUiState.ConfigData,
    onBack: () -> Unit,
    onSave: () -> Unit,
    ) {
    // state to save
    var alarmActiveState by remember { mutableStateOf(uiState.alarmActive) }

    val startTimePickerState = rememberTimePickerState(
        initialHour = Calendar.Builder().setInstant(uiState.alarmStart).build().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.Builder().setInstant(uiState.alarmStart).build().get(Calendar.MINUTE),
        is24Hour = true,
    )
    val endTimePickerState = rememberTimePickerState(
        initialHour = Calendar.Builder().setInstant(uiState.alarmEnd).build().get(Calendar.HOUR_OF_DAY),
        initialMinute = Calendar.Builder().setInstant(uiState.alarmEnd).build().get(Calendar.MINUTE),
        is24Hour = true,
    )

    val stationThatUseThisAlarmState = remember(uiState.userStations) {
        mutableStateListOf<StationChoice>().apply { addAll(uiState.userStations.filter { it.hasThisAlarm }) }
    }

    val sliderPositionsState: Map<String, MutableState<ClosedFloatingPointRange<Float>>> = remember {
        uiState.cards.associate { card ->
            card.title to mutableStateOf(card.startValue.toFloat()..card.endValue.toFloat())
        }
    }

    Scaffold (
        topBar = {
            TitleBar(onBack, uiState)
        },
        floatingActionButton = {
            FloatingActionButton({
                // TODO
            }) {
                Icon(Icons.Filled.Done, "Save the alarm.")
            }
        }
    ) { paddingValues ->
        LazyColumn (Modifier.padding(paddingValues)) {
            item() {
                Row(
                    modifier = Modifier.Companion
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeFieldText = if (alarmActiveState == uiState.alarmActive) {
                        if (alarmActiveState) "This alarm is enabled" else "This alarm is disabled"
                    } else
                        if (alarmActiveState) "This alarm will be enabled" else "This alarm will be disabled"
                    Text(
                        activeFieldText,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = alarmActiveState,
                        onCheckedChange = {
                            alarmActiveState = it
                        },
    //                    modifier = Modifier.scale(0.5f)
                    )
                }
                HorizontalDivider()
                Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
            }
            item() {
                ActiveTimePicker(
                    startTimePickerState,
                    endTimePickerState
                )
            }
            item() {
                HorizontalDivider()
                Column (
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Stations that use this alarm:",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                HorizontalDivider()
            }
            items(stationThatUseThisAlarmState) { stationChoice ->
                Row (
                    Modifier.padding(horizontal = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stationChoice.stationName, Modifier.weight(1f))

                    Switch(
                        checked = stationThatUseThisAlarmState
                            .find { it.stationId == stationChoice.stationId }
                            ?.hasThisAlarm ?: return@items,
                        onCheckedChange = { isChecked ->
                            val index = stationThatUseThisAlarmState.indexOfFirst { it.stationId == stationChoice.stationId }
                            if (index != -1) {
                                stationThatUseThisAlarmState[index] = stationChoice.copy(hasThisAlarm = isChecked)
                            }
                        }
                    )
                }
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }
            item() {
                var addStationDialogShown by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    SmallFloatingActionButton(onClick = {
                        addStationDialogShown = true
                    }) {
                        Icon(Icons.Filled.Add, "Add this alarm to another station.")
                    }
                }
                if (addStationDialogShown)
                    SelectAdditionalStationDialog(uiState.userStations
                        .filter {choice -> !choice.hasThisAlarm && !stationThatUseThisAlarmState.any{it.stationId == choice.stationId}},
                        { selections ->
                            stationThatUseThisAlarmState.addAll(
                                selections
                                    .filter { it.hasThisAlarm }
                            )
                            addStationDialogShown = false
                        },
                        {addStationDialogShown = false})
                Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
            }
            item() {
                Text("Select desired ranges:",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center)
            }
            items(uiState.cards) { card: ParameterRangeCard ->
                ParameterCardContent(sliderPositionsState, card)
            }
        }

    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    val uiState = AlarmConfigUiState.ConfigData(
        alarmId = "",
        createAlarm = false,
        alarmActive = true,
        alarmName = "Test alarm",
        userStations = listOf(
            StationChoice(
                stationId = "1",
                stationName = "Test station",
                hasThisAlarm = true
            ),
            StationChoice(
                stationId = "2",
                stationName = "Test station2",
                hasThisAlarm = true
            ),
            StationChoice(
                stationId = "3",
                stationName = "Test station 3",
                hasThisAlarm = false
            ),
        ),
        alarmStart = Date.from(Instant.now()),
        alarmEnd = Date.from(Instant.now()),
        cards = listOf(
            ParameterCardFactory.create(
                Parameter.TEMPERATURE,
                15.0, 20.0
            ),
            ParameterCardFactory.create(
                Parameter.PRESSURE,
                920.0, Double.POSITIVE_INFINITY,
            ),
            ParameterCardFactory.create(
                Parameter.PH,
                Double.NEGATIVE_INFINITY, 6.0
            ),
            ParameterCardFactory.create(
                Parameter.AIR_HUMIDITY,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY
            ),
        )
    )
    AlarmConfigContent(uiState, {}, {})
}