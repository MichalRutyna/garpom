package mm.zamiec.garpom.ui.screens.station

import android.icu.text.SimpleDateFormat
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.Line
import mm.zamiec.garpom.ui.screens.station.components.StationTopBar
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
    LazyColumn {
        item {
            Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
            HorizontalDivider()
            Text(
                "Notifications:",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .padding(10.dp)
            )
        }
        items(uiState.notifications) { notification ->
            Column (
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 10.dp))
                Row(
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
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
                    Icon(
                        notification.icon,
                        notification.iconDescription,
                        modifier = Modifier.padding(end = 5.dp)
                    )
                    Text(
                        notification.message,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        "Show details"
                    )
                }
            }
        }
        item {
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(10.dp))


            HorizontalDivider()
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .padding(10.dp)
                    .heightIn(max = 300.dp)
            ) {
//                    Text("Chart here",
//                        style = MaterialTheme.typography.headlineMedium,
//                    )
                LineChart(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp),
                    data = remember {
                        listOf(
                            Line(
                                label = "Windows",
                                values = listOf(28.0, 41.0, 5.0, 10.0, 35.0),
                                color = SolidColor(Color(0xFF23af92)),
                                firstGradientFillColor = Color(0xFF2BC0A1).copy(alpha = .5f),
                                secondGradientFillColor = Color.Transparent,
                                strokeAnimationSpec = tween(2000, easing = EaseInOutCubic),
                                gradientAnimationDelay = 1000,
                                drawStyle = DrawStyle.Stroke(width = 2.dp),
                            )
                        )
                    },
                    animationMode = AnimationMode.Together(delayBuilder = {
                        it * 500L
                    }),
                )
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(10.dp))
        }
        item {
            HorizontalDivider()
            Text(
                "Measurements:",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .padding(10.dp),
                textAlign = TextAlign.Center

            )
            HorizontalDivider()
        }

        items(uiState.measurementList) { measurement ->
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(10.dp)
                    .clickable(onClick = {
                        onMeasurementClicked(measurement.measurementId)
                    }),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    SimpleDateFormat(
                        "d MMMM, HH:mm",
                        Locale.getDefault()
                    )
                        .format(measurement.date),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    "Show details"
                )
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun StationErrorScreen(uiState: StationScreenUiState.Error) {
    Text("Error: ${uiState.msg}")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StationLoadingScreen() {
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()) {
        LoadingIndicator()
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