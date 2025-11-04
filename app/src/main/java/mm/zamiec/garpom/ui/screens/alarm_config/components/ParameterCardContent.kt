package mm.zamiec.garpom.ui.screens.alarm_config.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mm.zamiec.garpom.ui.screens.alarm_config.ParameterRangeCard
import java.util.Locale

@Composable
fun ParameterCardContent(
    sliderPositions: SnapshotStateMap<String, ClosedFloatingPointRange<Float>>,
    card: ParameterRangeCard
) {
    var sliderPosition = sliderPositions[card.title] ?: return

    HorizontalDivider()
    Column(
        modifier = Modifier.Companion
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Box(contentAlignment = Alignment.Companion.CenterStart) {
            Column {
                Text(
                    card.title,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "",
                    Modifier.Companion.fillMaxWidth(),
                    textAlign = TextAlign.Companion.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                RangeSlider(
                    value = sliderPosition,
//                            steps = 20,
                    onValueChange = { sliderPosition = it
                        sliderPositions[card.title] = sliderPosition },
                    valueRange = card.minValue.toFloat()..card.maxValue.toFloat(),
                    onValueChangeFinished = {
                    }

                )
                var text = ""
                if (sliderPosition.start !in listOf(
                        Float.NEGATIVE_INFINITY,
                        card.minValue.toFloat()
                    )
                ) {
                    if (sliderPosition.endInclusive !in listOf(
                            Float.POSITIVE_INFINITY,
                            card.maxValue.toFloat()
                        )
                    ) {
                        // both
                        text += "This alarm will go off for ${card.descriptionParameterName}" +
                                " below ${
                                    String.format(Locale.getDefault(),"%.1f",sliderPosition.start)
                                }${card.unit}," +
                                " or above ${
                                    String.format(Locale.getDefault(),"%.1f",sliderPosition.endInclusive)
                                }${card.unit}"
                    } else {
                        //only below
                        text += "This alarm will go off for ${card.descriptionParameterName}" +
                                " below ${String.format(Locale.getDefault(),"%.1f", sliderPosition.start)}${card.unit}"
                    }
                } else {
                    if (sliderPosition.endInclusive !in listOf(
                            Float.POSITIVE_INFINITY,
                            card.maxValue.toFloat()
                        )
                    ) {
                        // only above
                        text += "This alarm will go off for ${card.descriptionParameterName}" +
                                " above ${
                                    String.format(Locale.getDefault(),"%.1f", sliderPosition.endInclusive)
                                }${card.unit}"
                    } else {
                        // none
                        text += "This alarm will not measure ${card.descriptionParameterName}"
                    }
                }


                Text(
                    text,
                    Modifier.Companion.fillMaxWidth(),
                    textAlign = TextAlign.Companion.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }
        }
    }
    HorizontalDivider()
    Spacer(modifier = Modifier.Companion.padding(top = 10.dp, bottom = 6.dp))
}