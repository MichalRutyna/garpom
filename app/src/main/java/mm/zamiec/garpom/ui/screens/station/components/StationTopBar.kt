package mm.zamiec.garpom.ui.screens.station.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mm.zamiec.garpom.ui.screens.station.StationScreenUiState

@Composable
fun StationTopBar(
    onBack: () -> Unit,
    uiState: StationScreenUiState.StationData
) {
    Column {
        Box(
            contentAlignment = Alignment.Companion.Center,
            modifier = Modifier.Companion
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
                .padding(top = 20.dp)
                .fillMaxWidth()
        ) {
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                "Go back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.Companion
                    .align(Alignment.Companion.CenterStart)
                    .scale(1.3f)
                    .clickable(onClick = onBack)
            )
            BasicText(
                text = uiState.name,
                style = MaterialTheme.typography.headlineLarge.copy(color = MaterialTheme.colorScheme.primary),
                maxLines = 1,
                modifier = Modifier.Companion.padding(start = 5.dp, top = 5.dp, end = 5.dp),
                autoSize = TextAutoSize.Companion.StepBased(
                    minFontSize = 10.sp,
                    maxFontSize = 40.sp,
                    stepSize = 2.sp
                ),
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.inversePrimary)
    }
}