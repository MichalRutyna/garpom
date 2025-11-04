package mm.zamiec.garpom.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf

data class ScaffoldElements(
    val topBar: @Composable (() -> Unit)? = null,
    val fab: @Composable (() -> Unit)? = null
)
