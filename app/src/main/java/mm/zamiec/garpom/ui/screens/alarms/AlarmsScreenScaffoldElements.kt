package mm.zamiec.garpom.ui.screens.alarms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mm.zamiec.garpom.ui.ScaffoldElements

@Composable
fun alarmsScreenScaffoldElements(
    onCreateAlarmClicked: () -> Unit,
): ScaffoldElements
{
    return ScaffoldElements (
        topBar = {
            Row(
                verticalAlignment = Alignment.Companion.CenterVertically,
                modifier = Modifier.Companion
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(top = 20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Your alarms:",
                    Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
            }
        },
        fab = {
            FloatingActionButton({
                onCreateAlarmClicked()
            }) {
                Icon(Icons.Filled.Add, "Create an alarm.")
            }
        }
    )

}