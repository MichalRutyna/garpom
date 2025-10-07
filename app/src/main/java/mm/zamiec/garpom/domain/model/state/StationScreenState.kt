package mm.zamiec.garpom.domain.model.state

import android.icu.text.SimpleDateFormat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import mm.zamiec.garpom.ui.navigation.MeasurementScreen
import java.util.Locale


open class StationScreenState {
    data class StationData(
        val name: String = "",

        //    val charts: List<Chart>,

        val notifications: List<Notification> = emptyList(),
        val measurementList: List<MeasurementSummary> = emptyList(),
    ) : StationScreenState()

    data class Error(val msg: String) : StationScreenState()

    data object Loading : StationScreenState()
}

data class MeasurementSummary (
    val date: Timestamp = Timestamp.now(),
    val measurementId: String = "",
)

open class Notification(
    val message: String = "",
    val icon: ImageVector = Icons.Filled.Notifications,
    val iconDescription: String = "",
) {
    data class ErrorNotification(
        private val msg: String = "",
        val stationId: String = "",
    ) : Notification(msg, Icons.Filled.Warning, "An error occured")

    data class AlarmNotification(
        val measurementId: String = "",
        val measurementDate: Timestamp = Timestamp.now(),
        val alarmName: String = "",
    ) : Notification(
        "Alarm \"$alarmName\" went off on ${
            SimpleDateFormat(
                "MMM dd",
                Locale.getDefault())
                .format(measurementDate.toDate())
        }",
        Icons.Filled.Info,
        "An alarm went off")
}