package mm.zamiec.garpom.ui.screens.alarm_config.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import mm.zamiec.garpom.ui.screens.alarm_config.StationChoice

@Composable
fun SelectAdditionalStationDialog(
    choices: List<StationChoice>,
    onConfirmation: (List<StationChoice>) -> Unit,
    onDismiss: () -> Unit,
) {
    val choicesState = remember { mutableStateListOf<StationChoice>().apply { addAll(choices) } }
    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(15.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (choices.isNotEmpty()) {
                    Text(
                        text = "Add this alarm to additional stations:",
                        modifier = Modifier.padding(16.dp),
                    )
                    LazyColumn (
                        Modifier.heightIn(max = 300.dp)
                    ) {
                        items(choicesState) { choice ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(choice.stationName)
                                Checkbox(
                                    choice.hasThisAlarm,
                                    onCheckedChange = { isChecked ->
                                        val index = choicesState.indexOfFirst { it.stationId == choice.stationId }
                                        if (index != -1) {
                                            choicesState[index] = choice.copy(hasThisAlarm = isChecked)
                                        }
                                    }
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Cancel")
                        }
                        TextButton(
                            onClick = { onConfirmation(choicesState) },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Add selected")
                        }
                    }
                }
                else {
                    Text(
                        text = "You have no stations without this alarm",
                        modifier = Modifier.padding(16.dp),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        TextButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Ok")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    SelectAdditionalStationDialog(listOf(
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
        {}, {})
}