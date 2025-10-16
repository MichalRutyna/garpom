package mm.zamiec.garpom.ui.screens.station

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.ui.state.MeasurementSummaryItemUiState
import mm.zamiec.garpom.ui.state.NotificationItemUiState
import mm.zamiec.garpom.ui.state.StationScreenUiState
import java.util.Locale

@Composable
fun StationScreen(
    stationId: String,
    stationViewModel: StationViewModel = hiltViewModel(
        creationCallback = { factory: StationViewModel.Factory ->
            factory.create(stationId)
        }
    ),
    onMeasurementClicked: (String) -> Unit,
    onErrorClicked: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState: StationScreenUiState by stationViewModel.uiState.collectAsState(StationScreenUiState())

    when(uiState) {
        is StationScreenUiState.StationData ->
            StationScreenContent(
                uiState as StationScreenUiState.StationData,
                onMeasurementClicked,
                onErrorClicked,
                onBack
            )
        is StationScreenUiState.Error ->
            StationErrorScreen(
                uiState as StationScreenUiState.Error
            )
        is StationScreenUiState.Loading ->
            StationLoadingScreen()
    }
}

@Composable
private fun StationScreenContent(
    uiState: StationScreenUiState.StationData,
    onMeasurementClicked: (String) -> Unit,
    onErrorClicked: (String) -> Unit,
    onBack: () -> Unit,
) {
    Text(uiState.name, style = MaterialTheme.typography.headlineLarge)
    Column {
        Box (
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .fillMaxWidth()
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                "Show details",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.CenterStart).scale(1.3f)
                    .clickable(onClick = onBack)
            )
            BasicText(
                text = uiState.name,
                style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                maxLines = 1,
                modifier = Modifier.padding(start = 5.dp, top = 5.dp, end = 5.dp),
                autoSize = TextAutoSize.StepBased(minFontSize = 10.sp, maxFontSize = 40.sp, stepSize = 2.sp),
            )

        }
        HorizontalDivider(color = MaterialTheme.colorScheme.inversePrimary)

        Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))

        HorizontalDivider()
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column (
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Notifications:",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                HorizontalDivider()
                LazyColumn {
                    items (uiState.notifications) { notification ->
                        Row (
                            modifier = Modifier.padding(10.dp)
                                .clickable(onClick = {
                                    when (notification) {
                                        is NotificationItemUiState.AlarmNotification ->
                                            onMeasurementClicked(notification.measurementId)
                                        is NotificationItemUiState.ErrorNotification ->
                                            onErrorClicked(notification.stationId)
                                    }
                                }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(notification.icon,
                                notification.iconDescription,
                                modifier = Modifier.padding(end = 5.dp))
                            Text(notification.message,
                                modifier = Modifier.weight(1f))
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                "Show details")
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.padding(10.dp))

        HorizontalDivider()
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column (
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Chart here",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                HorizontalDivider()
            }
        }
        HorizontalDivider()

        Spacer(modifier = Modifier.padding(10.dp))

        HorizontalDivider()
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Column (
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Measurements:",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally))
                HorizontalDivider()
                LazyColumn {
                    items (uiState.measurementList) { measurement ->
                        Row (
                            modifier = Modifier.padding(10.dp)
                                .clickable(onClick = {
                                    onMeasurementClicked(measurement.measurementId)
                                }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(SimpleDateFormat(
                                    "d MMMM, HH:mm",
                                    Locale.getDefault())
                                    .format(measurement.date),
                                modifier = Modifier.weight(1f))
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                "Show details")
                        }
                        HorizontalDivider()
                    }
                }
            }
        }
        HorizontalDivider()
    }
}

@Composable
private fun StationErrorScreen(uiState: StationScreenUiState.Error) {
    Text("Error: ${uiState.msg}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StationLoadingScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        CircularProgressIndicator()
    }
}


@Preview(showBackground = true)
@Composable
private fun Preview() {
    val uiState = StationScreenUiState.StationData(
        name = "Test station",
        notifications = listOf(
            NotificationItemUiState.AlarmNotification(alarmName = "Test alarm"),
            NotificationItemUiState.ErrorNotification(
                msg = "Station \"Test station\" is not responding"
            )
        ),
        measurementList = listOf(
            MeasurementSummaryItemUiState(),
            MeasurementSummaryItemUiState(),
            MeasurementSummaryItemUiState(),
            MeasurementSummaryItemUiState(),
        ),
    )
    StationScreenContent(uiState, {}, {}, {})
}