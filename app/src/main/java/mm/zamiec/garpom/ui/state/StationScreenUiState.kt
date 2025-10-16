package mm.zamiec.garpom.ui.state

import android.icu.text.SimpleDateFormat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import java.time.Instant
import java.util.Date
import java.util.Locale


open class StationScreenUiState {
    data class StationData(
        val name: String = "",

        //    val charts: List<Chart>,

        val notifications: List<NotificationItemUiState> = emptyList(),
        val measurementList: List<MeasurementSummaryItemUiState> = emptyList(),
    ) : StationScreenUiState()

    data class Error(val msg: String) : StationScreenUiState()

    data object Loading : StationScreenUiState()
}

data class MeasurementSummaryItemUiState (
    val date: Date = Date.from(Instant.now()),
    val measurementId: String = "",
)

open class NotificationItemUiState(
    val message: String = "",
    val icon: ImageVector = Icons.Filled.Notifications,
    val iconDescription: String = "",
) {
    data class ErrorNotification(
        private val msg: String = "",
        val stationId: String = "",
    ) : NotificationItemUiState(msg, Icons.Filled.Warning, "An error occured")

    data class AlarmNotification(
        val measurementId: String = "",
        val measurementDate: Date = Date.from(Instant.now()),
        val alarmName: String = "",
    ) : NotificationItemUiState(
        "Alarm \"$alarmName\" went off on ${
            SimpleDateFormat(
                "MMM dd",
                Locale.getDefault())
                .format(measurementDate)
        }",
        Icons.Filled.Info,
        "An alarm went off")
}