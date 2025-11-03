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
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                onBack
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
    onBack: () -> Unit,
) {
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    Scaffold (
        topBar = { TopBar(onBack, uiState) },
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = fabMenuExpanded,
                button = {
                    ToggleFloatingActionButton(
                        modifier =
                            Modifier.animateFloatingActionButton(
                                    visible = true,
                                    alignment = Alignment.BottomEnd,
                                ),
                        checked = fabMenuExpanded,
                        onCheckedChange = { fabMenuExpanded = !fabMenuExpanded },
                    ) {
                        val imageVector by remember {
                            derivedStateOf {
                                if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.KeyboardArrowUp
                            }
                        }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = null,
                            modifier = Modifier.animateIcon({ checkedProgress }),
                        )
                    }
                }) {
                FloatingActionButtonMenuItem(
                    onClick = {},
                    text = { Text("Delete") },
                    icon = { Icon(Icons.Filled.Delete, contentDescription = null) }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn (
            modifier = Modifier.padding(paddingValues)
        ) {
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

@Composable
private fun TopBar(
    onBack: () -> Unit,
    uiState: MeasurementScreenState.MeasurementData
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer)
            .fillMaxWidth()
    ) {
        Icon(
            Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
            "Show details",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .scale(1.3f)
                .clickable(onClick = onBack)
        )
        BasicText(
            text = uiState.stationName + " measurement",
            style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
            maxLines = 1,
            modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp),
            autoSize = TextAutoSize.StepBased(
                minFontSize = 10.sp,
                maxFontSize = 40.sp,
                stepSize = 2.sp
            ),
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.inversePrimary)
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
    MeasurementDataScreen(uistate, {}, {})
//    MeasurementLoadingScreen()
}