package mm.zamiec.garpom.ui.screens.alarm_config.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import mm.zamiec.garpom.R
import java.util.Locale

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ActiveTimePicker(
    startTimePickerState: TimePickerState,
    endTimePickerState: TimePickerState
) {
    var showTimeDialog by remember { mutableStateOf(false) }
    var lastClickedOnEndTime by remember { mutableStateOf(false) }
    HorizontalDivider()
    Column(
        modifier = Modifier.Companion
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .fillMaxWidth()
            .padding(10.dp),
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Text(
            "Active only between:",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            verticalAlignment = Alignment.Companion.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                value = "${startTimePickerState.hour}:${String.format(Locale.getDefault(), "%02d", startTimePickerState.minute)}",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        lastClickedOnEndTime = false
                        showTimeDialog = !showTimeDialog
                    }) {
                        Icon(
                            imageVector = ImageVector.Companion.vectorResource(R.drawable.schedule_24px),
                            contentDescription = "Select date",
                        )
                    }
                },
                modifier = Modifier.Companion.weight(1f)
            )
            Text(" : ")
            OutlinedTextField(
                value = "${endTimePickerState.hour}:${String.format(Locale.getDefault(), "%02d", endTimePickerState.minute)}",
                onValueChange = { },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = {
                        lastClickedOnEndTime = true
                        showTimeDialog = !showTimeDialog
                    }) {
                        Icon(
                            imageVector = ImageVector.Companion.vectorResource(R.drawable.schedule_24px),
                            contentDescription = "Select date"
                        )
                    }
                },
                modifier = Modifier.Companion.weight(1f)
            )
        }
        if (showTimeDialog) {
            AdvancedTimePickerDialog(
                onDismiss = { showTimeDialog = false },
                onConfirm = {
//                        onConfirm(timePickerState)
                    showTimeDialog = false
                }
            ) {
                val state =
                    if (lastClickedOnEndTime) endTimePickerState else startTimePickerState
                TimePicker(state)
            }
        }


    }
    HorizontalDivider()
    Spacer(modifier = Modifier.Companion.padding(top = 10.dp, bottom = 3.dp))
}