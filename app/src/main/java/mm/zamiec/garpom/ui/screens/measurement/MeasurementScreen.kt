package mm.zamiec.garpom.ui.screens.measurement

import android.icu.text.SimpleDateFormat
import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.google.firebase.Timestamp
import com.google.type.DateTime
import mm.zamiec.garpom.domain.model.state.MeasurementScreenState
import mm.zamiec.garpom.domain.model.state.StationScreenState
import mm.zamiec.garpom.ui.screens.auth.AuthUiState
import mm.zamiec.garpom.ui.screens.station.StationViewModel
import androidx.compose.material3.CircularProgressIndicator
import mm.zamiec.garpom.domain.model.state.FireCard
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
) {
    val uiState: MeasurementScreenState by measurementViewModel.uiState.collectAsState()

    when (uiState) {
        is MeasurementScreenState.Loading ->
            MeasurementLoadingScreen()
        is MeasurementScreenState.MeasurementData -> {
            val uiState = uiState as MeasurementScreenState.MeasurementData
            MeasurementDataScreen(uiState, onAlarmClick)
        }
        is MeasurementScreenState.Error ->
            MeasurementErrorScreen(uiState)
    }
}

@Composable
private fun MeasurementErrorScreen(uiState: MeasurementScreenState) {
    Text("Error: ${(uiState as MeasurementScreenState.Error).message}")
}

@Composable
private fun MeasurementDataScreen(
    uiState: MeasurementScreenState.MeasurementData,
    onAlarmClick: (String) -> Unit,
) {
    Column {
        Row (verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                "Show details",
                )
            BasicText(
                text = uiState.stationName + " measurement",
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 1,
                modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp),
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 40.sp, stepSize = 2.sp)
            )
        }

        Row (horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth().padding(end = 10.dp)) {
            Text(
                text = SimpleDateFormat("EEEE, d MMMM yyyy, HH:mm", Locale.getDefault())
                    .format(uiState.date.toDate())
                    .replaceFirstChar {
                        it.uppercaseChar()
                    },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }


        LazyColumn (
            verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
            items(uiState.cards) { card ->
                HorizontalDivider()
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainer)
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    Column {
                        Text(card.title)
                        Box(contentAlignment = Alignment.CenterStart) {
                            if (card.triggeredAlarms.isEmpty()) {
                                Icon(
                                    Icons.Rounded.Check,
                                    "Ok",
                                    modifier = Modifier.padding(start = 15.dp)
                                )
                            } else {
                                for (alarm in card.triggeredAlarms) {
                                    Column(Modifier.clickable(onClick = {
                                        onAlarmClick(alarm.alarmId)
                                    })) {
                                        Icon(
                                            Icons.Rounded.Warning,
                                            "Alarm",
                                            modifier = Modifier.padding(start = 15.dp)
                                        )
                                        Text(alarm.alarmName)
                                    }
                                }
                            }

                            Text(
                                "" + card.value + card.unit,
                                Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )

                        }
                    }
                }
                HorizontalDivider()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MeasurementLoadingScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    val uistate = MeasurementScreenState.MeasurementData(
        stationName = "Test test test station",
        date = Timestamp.now(),
        cards = emptyList(),
        fire = FireCard(false)
    )
    MeasurementDataScreen(uistate, {})
//    MeasurementLoadingScreen()
}