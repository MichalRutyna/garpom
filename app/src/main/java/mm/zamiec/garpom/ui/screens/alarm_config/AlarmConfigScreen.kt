package mm.zamiec.garpom.ui.screens.alarm_config

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import mm.zamiec.garpom.domain.model.Parameter
import java.time.Instant
import java.util.Date

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
                {} //TODO
            )
        }
        is AlarmConfigUiState.Error ->
        {}
    }

}

@Composable
fun AlarmConfigContent(
    uiState: AlarmConfigUiState.ConfigData,
    onBack: () -> Unit,
    onSave: () -> Unit,
    ) {

    LazyColumn {
        item() {
            Box(
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
                    text = uiState.alarmName,
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

            Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 10.dp))
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
                    "Active between:",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    Text("start")
                    Text(" : ")
                    Text("end")
                }
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 3.dp))
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
                    "Stations:",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                }
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
        }
        item() {
            Text("Select desired ranges:",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)
        }
        items(uiState.cards) { card: ParameterRangeCard ->
            var sliderPosition by remember {
                mutableStateOf(
                    card.startValue.toFloat()..card.endValue.toFloat()
                ) }

            HorizontalDivider()
            Column (
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.CenterStart) {
                    Column {
                        Text(
                            card.title,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "",
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        RangeSlider (
                            value = sliderPosition,
//                            steps = 20,
                            onValueChange = { range -> sliderPosition = range },
                            valueRange = card.minValue.toFloat()..card.maxValue.toFloat(),
                            onValueChangeFinished = {}

                        )
                        var text = ""
                        if (sliderPosition.start !in listOf(Float.NEGATIVE_INFINITY, card.minValue.toFloat())) {
                            if (sliderPosition.endInclusive !in listOf(Float.POSITIVE_INFINITY, card.maxValue.toFloat())) {
                                // both
                                text += "This alarm will go off for ${card.descriptionParameterName}" +
                                        " below ${String.format("%.1f", sliderPosition.start)}${card.unit}," +
                                        " or above ${String.format("%.1f", sliderPosition.endInclusive)}${card.unit}"
                            }
                            else {
                                //only below
                                text += "This alarm will go off for ${card.descriptionParameterName}" +
                                        " below ${String.format("%.1f", sliderPosition.start)}${card.unit}"
                            }
                        }
                        else {
                            if (sliderPosition.endInclusive !in listOf(Float.POSITIVE_INFINITY, card.maxValue.toFloat())) {
                                // only above
                                text += "This alarm will go off for ${card.descriptionParameterName}" +
                                        " above ${String.format("%.1f", sliderPosition.endInclusive)}${card.unit}"
                            }
                            else {
                                // none
                                text += "This alarm will not measure ${card.descriptionParameterName}"
                            }
                        }


                        Text(
                            text,
                            Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        )
                    }
                }
            }
            HorizontalDivider()
            Spacer(modifier = Modifier.padding(top = 10.dp, bottom = 6.dp))
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
                stationId = "",
                stationName = "Test station",
                hasThisAlarm = true
            ),
            StationChoice(
                stationId = "",
                stationName = "Test station 2",
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