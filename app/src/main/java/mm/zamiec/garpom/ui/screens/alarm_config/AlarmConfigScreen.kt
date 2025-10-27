package mm.zamiec.garpom.ui.screens.alarm_config

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

@OptIn(ExperimentalMaterial3Api::class)
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
            { AlarmConfigLoadingScreen() }
        is AlarmConfigUiState.ConfigData -> {
            AlarmConfigContent(
                uiState as AlarmConfigUiState.ConfigData,
                onBack,
                alarmConfigViewModel::saveStates)
        }
        is AlarmConfigUiState.Error ->
            { AlarmConfigErrorScreen(uiState as AlarmConfigUiState.Error) }
    }

}

@Composable
private fun AlarmConfigErrorScreen(uiState: AlarmConfigUiState.Error) {
    Text("Error: ${uiState.message}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmConfigLoadingScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmConfigContent(
    uiState: AlarmConfigUiState.ConfigData,
    onBack: () -> Unit,
    onSave: (Boolean, String, String, TimePickerState, TimePickerState, SnapshotStateList<StationChoice>,
             Map<String, MutableState<ClosedFloatingPointRange<Float>>>) -> Unit,
    ) {
    val alarmActiveState = remember { mutableStateOf(uiState.alarmEnabled) }

    val alarmNameState = remember { mutableStateOf(uiState.alarmName) }

    val alarmDescriptionState = remember { mutableStateOf(uiState.alarmDescription) }

    val startTimePickerState: TimePickerState = rememberTimePickerState(
        initialHour = uiState.alarmStart.get(Calendar.HOUR_OF_DAY),
        initialMinute = uiState.alarmStart.get(Calendar.MINUTE),
        is24Hour = true,
    )
    val endTimePickerState: TimePickerState = rememberTimePickerState(
        initialHour = uiState.alarmEnd.get(Calendar.HOUR_OF_DAY),
        initialMinute = uiState.alarmEnd.get(Calendar.MINUTE),
        is24Hour = true,
    )

    val stationUsingThisAlarmState: SnapshotStateList<StationChoice> = remember(uiState.userStations) {
        mutableStateListOf<StationChoice>().apply { addAll(uiState.userStations.filter { it.hasThisAlarm }) }
    }

    var sliderPositionsState: Map<String, MutableState<ClosedFloatingPointRange<Float>>> =
        remember {
            cardsToMap(uiState)
        }

    Scaffold (
        topBar = {
            TitleBar(onBack, alarmNameState.value)
        },
        floatingActionButton = {
            FloatingActionButton({
                onSave(
                    alarmActiveState.value,
                    alarmNameState.value,
                    alarmDescriptionState.value,
                    startTimePickerState,
                    endTimePickerState,
                    stationUsingThisAlarmState,
                    sliderPositionsState,
                )
            }) {
                Icon(Icons.Filled.Done, "Save the alarm.")
            }
        }
    ) { paddingValues ->
        LazyColumn (Modifier.padding(paddingValues)) {
            item {
                AlarmEnabledWidget(alarmActiveState, uiState)
                HorizontalDivider()
                AlarmNameWidget(alarmNameState, uiState)
                HorizontalDivider()
                AlarmDescriptionWidget(alarmDescriptionState, uiState)
                Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
            }
            item {
                ActiveTimePicker(
                    startTimePickerState,
                    endTimePickerState
                )
            }
            item {
                HorizontalDivider()
                StationUsingThisAlarmTitle()
                HorizontalDivider()
            }
            items(stationUsingThisAlarmState) { stationChoice ->
                StationUsingThisAlarmItem(stationUsingThisAlarmState, stationChoice)
                HorizontalDivider(Modifier.padding(horizontal = 10.dp))
            }
            item {
                AddToStationRow(stationUsingThisAlarmState, uiState)
                Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
            }
            item {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp)
                ) {
                    Text(
                        "Select desired ranges:",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = {
                        cardsToMap(uiState).forEach { t, u ->
                            sliderPositionsState[t]?.value = u.value
                        }
                    }) {
                        Text("Reset")
                    }
                }
            }
            items(uiState.cards) { card: ParameterRangeCard ->
                ParameterCardContent(sliderPositionsState, card)
            }
        }

    }
}

private fun cardsToMap(uiState: AlarmConfigUiState.ConfigData): Map<String, MutableState<ClosedFloatingPointRange<Float>>> =
    uiState.cards.associate { card ->
        card.title to mutableStateOf(card.startValue.toFloat()..card.endValue.toFloat())
    }

@Composable
private fun SelectRagesTitle() {
    Row {

    }
}

@Composable
private fun AddToStationRow(
    stationUsingThisAlarmState: SnapshotStateList<StationChoice>,
    uiState: AlarmConfigUiState.ConfigData
) {
    var addStationDialogShown by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val warningText =
            if (stationUsingThisAlarmState.any { it.hasThisAlarm }) "" else "This alarm will never activate, add a station"
        Text(warningText, color = Color.Red)
        SmallFloatingActionButton(onClick = {
            addStationDialogShown = true
        }) {
            Icon(Icons.Filled.Add, "Add this alarm to another station.")
        }
    }
    if (addStationDialogShown)
        SelectAdditionalStationDialog(
            uiState.userStations
                .filter { choice -> !choice.hasThisAlarm && !stationUsingThisAlarmState.any { it.stationId == choice.stationId } },
            { selections ->
                stationUsingThisAlarmState.addAll(
                    selections
                        .filter { it.hasThisAlarm }
                )
                addStationDialogShown = false
            },
            { addStationDialogShown = false })
}

@Composable
fun StationUsingThisAlarmItem(stationUsingThisAlarmState: SnapshotStateList<StationChoice>, stationChoice: StationChoice) {
    Row (
        Modifier.padding(horizontal = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(stationChoice.stationName, Modifier.weight(1f))

        Switch(
            checked = stationUsingThisAlarmState
                .find { it.stationId == stationChoice.stationId }
                ?.hasThisAlarm ?: return,
            onCheckedChange = { isChecked ->
                val index = stationUsingThisAlarmState.indexOfFirst { it.stationId == stationChoice.stationId }
                if (index != -1) {
                    stationUsingThisAlarmState[index] = stationChoice.copy(hasThisAlarm = isChecked)
                }
            }
        )
    }
}

@Composable
private fun StationUsingThisAlarmTitle() {
    Text(
        "Stations that use this alarm:",
        style = MaterialTheme.typography.headlineSmall,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(10.dp),
    )
}

@Composable
fun AlarmDescriptionWidget(alarmDescriptionState: MutableState<String>, uiState: AlarmConfigUiState.ConfigData) {
    Row (
        Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = alarmDescriptionState.value,
            label = { Text("Alarm description") },
            onValueChange = {
                alarmDescriptionState.value = it
            },
            modifier = Modifier.weight(1f)
                .padding(5.dp)
        )
        Button(onClick = {
            alarmDescriptionState.value = uiState.alarmDescription
        }) {
            Text("Reset")
        }
    }
}

@Composable
private fun AlarmNameWidget(
    alarmNameState: MutableState<String>,
    uiState: AlarmConfigUiState.ConfigData
) {
    Row (
        Modifier.padding(top = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = alarmNameState.value,
            onValueChange = {
                alarmNameState.value = it
            },
            label = { Text("Alarm name") },
            modifier = Modifier
                .weight(1f)
                .padding(5.dp)
        )
        Button(onClick = {
            alarmNameState.value = uiState.alarmName
        }) {
            Text("Reset")
        }
    }
}

@Composable
private fun AlarmEnabledWidget(
    alarmActiveState: MutableState<Boolean>,
    uiState: AlarmConfigUiState.ConfigData
) {
    Row(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .padding(horizontal = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val activeFieldText = if (alarmActiveState.value == uiState.alarmEnabled) {
            if (alarmActiveState.value) "This alarm is enabled" else "This alarm is disabled"
        } else
            if (alarmActiveState.value) "This alarm will be enabled" else "This alarm will be disabled"
        Text(
            activeFieldText,
            color = MaterialTheme.colorScheme.onSurface
        )
        Switch(
            checked = alarmActiveState.value,
            onCheckedChange = {
                alarmActiveState.value = it
            }
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun Preview() {
    val uiState = AlarmConfigUiState.ConfigData(
        alarmId = "",
        createAlarm = false,
        alarmEnabled = true,
        alarmName = "Test alarm",
        alarmDescription = "Test description",
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
        alarmStart = Calendar.Builder().setInstant(Instant.now().epochSecond).build(),
        alarmEnd = Calendar.Builder().setInstant(Instant.now().epochSecond).build(),
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
    AlarmConfigContent(uiState, {}, {_, _, _, _, _, _, _ ->})
}