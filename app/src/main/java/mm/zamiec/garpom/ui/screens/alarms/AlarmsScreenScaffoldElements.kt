package mm.zamiec.garpom.ui.screens.alarms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import mm.zamiec.garpom.ui.ScaffoldElements

@Composable
fun alarmsScreenScaffoldElements(
    onCreateAlarmClicked: () -> Unit,
): ScaffoldElements
{
    return ScaffoldElements (
        fab = {
            FloatingActionButton({
                onCreateAlarmClicked()
            }) {
                Icon(Icons.Filled.Add, "Create an alarm.")
            }
        }
    )

}