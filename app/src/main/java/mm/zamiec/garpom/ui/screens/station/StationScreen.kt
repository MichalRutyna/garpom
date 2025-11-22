package mm.zamiec.garpom.ui.screens.station

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import mm.zamiec.garpom.R
import mm.zamiec.garpom.domain.model.IconType
import mm.zamiec.garpom.domain.model.Parameter
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
    val uiState: StationScreenUiState by stationViewModel.uiState.collectAsStateWithLifecycle(StationScreenUiState.Loading)
    val graphData: GraphData by stationViewModel.graphdata.collectAsStateWithLifecycle()

    when(uiState) {
        is StationScreenUiState.StationData ->
            StationScreenContent(
                uiState as StationScreenUiState.StationData,
                graphData,
                onMeasurementClicked,
                onErrorClicked,
                onBack,
                stationViewModel::changeLineSelection,
                stationViewModel::onRangeChange,
                stationViewModel::onRangeChangeFinished,
                stationViewModel::onChartPeriodChecked
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
    graphData: GraphData,
    onMeasurementClicked: (String) -> Unit,
    onErrorClicked: (String) -> Unit,
    onBack: () -> Unit,
    onChipSelected: (ParameterChipData) -> Unit,
    onChartTimeRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onChartTimeRangeChangeFinished: () -> Unit,
    onChartPeriodChecked: (PeriodSelection) -> Unit,
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
            ChartSection(
                graphData,
                onChipSelected,
                onChartTimeRangeChange,
                onChartTimeRangeChangeFinished,
                onChartPeriodChecked,
            )
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
fun ChartSection(
    graphData: GraphData,
    onChipSelected: (ParameterChipData) -> Unit,
    onChartTimeRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
    onChartTimeRangeChangeFinished: () -> Unit,
    onChartPeriodChecked: (PeriodSelection) -> Unit,
) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .padding(bottom = 10.dp)
            .heightIn(max = 480.dp)
    ) {
        Column{
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
            ) {
                items(graphData.graphChips) { parameterChipData ->
                    ParameterChip(
                        parameterChipData,
                        onChipSelected,
                    )
                }
            }
            LineChart(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 22.dp),
                data = graphData.lines,
                animationMode = AnimationMode.Together(delayBuilder = {
                    it * 200L
                }),
                indicatorProperties = HorizontalIndicatorProperties(
                    textStyle = TextStyle.Default.copy(fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurface)
                ),
                labelHelperProperties = LabelHelperProperties(
                    textStyle = TextStyle.Default.copy(fontSize = 12.sp, textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurface)
                ),
                labelProperties = LabelProperties(
                    enabled = true,
                    textStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface),
                    labels = graphData.graphTimeLabels,
                ),
                dotsProperties = DotProperties(
                    enabled = true,
                    radius = 1.dp,
//                    color = SolidColor(Color.Red),
                    strokeColor = SolidColor(MaterialTheme.colorScheme.primary),
                ),
                zeroLineProperties = ZeroLineProperties(
                    enabled = true,
                )
            )
            ChartPeriodSelector(
                graphData,
                onChartPeriodChecked
            )
            ChartDateRangeSelector(
                graphData,
                onChartTimeRangeChange,
                onChartTimeRangeChangeFinished,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ChartPeriodSelector(
    graphData: GraphData,
    onChartPeriodChecked: (PeriodSelection) -> Unit,
) {
    Row (
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween, alignment = Alignment.CenterHorizontally)
    ) {
        graphData.periodSelections.forEachIndexed { index, period ->
            ToggleButton(
                checked = period.selected,
                onCheckedChange = { onChartPeriodChecked(period) },
                colors = ToggleButtonDefaults.toggleButtonColors().copy(containerColor = MaterialTheme.colorScheme.primaryContainer, ),
                shapes =
                    when (index) {
                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                        graphData.periodSelections.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
            ) {
                Text(period.name)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartDateRangeSelector(
        graphData: GraphData,
        onChartTimeRangeChange: (ClosedFloatingPointRange<Float>) -> Unit,
        onChartTimeRangeChangeFinished: () -> Unit,
    ) {
        RangeSlider(
            value = graphData.graphActiveTimeRange,
            valueRange = graphData.graphTimeRange,
            onValueChange = onChartTimeRangeChange,
            onValueChangeFinished = onChartTimeRangeChangeFinished,
            steps = graphData.timeRangeSteps,
        )
}

@Composable
fun ParameterChip(
    parameterChipData: ParameterChipData,
    onChipSelected: (ParameterChipData) -> Unit,
) {
    FilterChip(
        selected = parameterChipData.enabled,
        onClick = { onChipSelected(parameterChipData) },
        label = { Text(parameterChipData.parameter.title) },
        leadingIcon = if (parameterChipData.enabled) {
            { Icon(
                imageVector = Icons.Default.Done,
                contentDescription = parameterChipData.parameter.descriptionText
            ) }
        } else {
            {
                Icon(
                    imageVector = iconFor(parameterChipData.parameter.icon),
                    contentDescription = parameterChipData.parameter.descriptionText
                )
            }
        },
        modifier = Modifier
            .padding(horizontal = 2.dp)
    )
}

@Composable
private fun StationErrorScreen(uiState: StationScreenUiState.Error) {
    Text("Error: ${uiState.msg}")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StationLoadingScreen() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        LoadingIndicator()
    }
}
@Composable
fun iconFor(type: IconType): ImageVector {
    return when (type) {
        IconType.Dataset -> ImageVector.vectorResource(R.drawable.dataset_24)
    }
}

@SuppressLint("UnrememberedMutableState")
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
    val graphData = GraphData(
        graphChips = listOf(
            ParameterChipData(
                Parameter.TEMPERATURE,
                Line(values = emptyList(),
                    label = Parameter.TEMPERATURE.title,
                    color = SolidColor(Color(0xFF23af92))
                ),
                enabled = true
            ),
            ParameterChipData(
                Parameter.AIR_HUMIDITY,
                Line(values = emptyList(),
                    label = Parameter.AIR_HUMIDITY.title,
                    color = SolidColor(Color(0xFF23af92))
                ),
                enabled = false
            ),
            ParameterChipData(
                Parameter.AIR_HUMIDITY,
                Line(values = emptyList(),
                    label = Parameter.AIR_HUMIDITY.title,
                    color = SolidColor(Color(0xFF23af92))
                ),
                enabled = false
            ),
            ParameterChipData(
                Parameter.AIR_HUMIDITY,
                Line(values = emptyList(),
                    label = Parameter.AIR_HUMIDITY.title,
                    color = SolidColor(Color(0xFF23af92))
                ),
                enabled = false
            ),
        ),
        periodSelections = listOf(
            PeriodSelection(
                "Last week", true,
            ),
            PeriodSelection(
                "Last month",
            ),
        ),

    )
    StationScreenContent(
        uiState,
        graphData,
        {},
        {},
        {},
        {},
        {},
        {},
        {_ ->}
    )
}